package com.servicesphere.cases;

import com.servicesphere.assignment.AgentAssignmentService;
import com.servicesphere.assignment.AssignmentResult;
import com.servicesphere.cases.dto.*;
import com.servicesphere.common.PagedResponse;
import com.servicesphere.common.exception.BadRequestException;
import com.servicesphere.common.exception.InvalidStateTransitionException;
import com.servicesphere.common.exception.ResourceNotFoundException;
import com.servicesphere.notification.NotificationService;
import com.servicesphere.notification.NotificationType;
import com.servicesphere.security.Role;
import com.servicesphere.security.UserPrincipal;
import com.servicesphere.sla.SlaService;
import com.servicesphere.user.User;
import com.servicesphere.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CaseService {

    private static final Logger log = LoggerFactory.getLogger(CaseService.class);

    private final CaseRepository caseRepository;
    private final CaseCommentRepository commentRepository;
    private final CaseHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final AgentAssignmentService assignmentService;
    private final SlaService slaService;
    private final NotificationService notificationService;

    public CaseService(
            CaseRepository caseRepository,
            CaseCommentRepository commentRepository,
            CaseHistoryRepository historyRepository,
            UserRepository userRepository,
            AgentAssignmentService assignmentService,
            SlaService slaService,
            NotificationService notificationService
    ) {
        this.caseRepository = caseRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.assignmentService = assignmentService;
        this.slaService = slaService;
        this.notificationService = notificationService;
    }

    @Transactional
    public synchronized String generateCaseNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = caseRepository.count() + 1;
        String candidate = String.format("CASE-%s-%04d", datePrefix, count);
        while (caseRepository.existsByCaseNumber(candidate)) {
            count++;
            candidate = String.format("CASE-%s-%04d", datePrefix, count);
        }
        return candidate;
    }

    @Transactional
    public CaseDto createCase(CaseCreateRequest request, UserPrincipal principal) {
        User actor = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        User customer;
        if (principal.getRole() == Role.CUSTOMER) {
            customer = actor;
        } else {
            if (request.getCustomerId() != null) {
                customer = userRepository.findById(request.getCustomerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));
            } else {
                customer = actor;
            }
        }

        Instant now = Instant.now();
        SlaService.Deadlines deadlines = slaService.calculateDeadlines(request.getPriority(), now);

        SupportCase supportCase = new SupportCase();
        supportCase.setCaseNumber(generateCaseNumber());
        supportCase.setTitle(request.getTitle().trim());
        supportCase.setDescription(request.getDescription().trim());
        supportCase.setCategory(request.getCategory());
        supportCase.setPriority(request.getPriority());
        supportCase.setStatus(CaseStatus.OPEN);
        supportCase.setCustomer(customer);
        supportCase.setFirstResponseDeadline(deadlines.firstResponseDeadline());
        supportCase.setResolutionDeadline(deadlines.resolutionDeadline());

        // Rule-based automated agent assignment
        AssignmentResult assignmentResult = assignmentService.assignAgent(request.getCategory(), request.getPriority());
        if (assignmentResult.isAssigned()) {
            supportCase.setAssignedAgent(assignmentResult.getAgent());
        }

        supportCase = caseRepository.save(supportCase);

        // Record creation history
        CaseHistory creationHistory = new CaseHistory(
                supportCase,
                actor,
                HistoryAction.CREATED,
                null,
                CaseStatus.OPEN.name(),
                "Support case created"
        );
        historyRepository.save(creationHistory);

        // Record assignment history
        if (assignmentResult.isAssigned()) {
            CaseHistory assignmentHistory = new CaseHistory(
                    supportCase,
                    actor,
                    HistoryAction.ASSIGNED,
                    "UNASSIGNED",
                    assignmentResult.getAgent().getFullName(),
                    assignmentResult.getReason()
            );
            historyRepository.save(assignmentHistory);

            // Notify assigned agent
            notificationService.createNotification(
                    assignmentResult.getAgent(),
                    "New Case Assigned: " + supportCase.getCaseNumber(),
                    "You have been assigned to case '" + supportCase.getTitle() + "' (" + supportCase.getPriority() + ")",
                    NotificationType.CASE_ASSIGNMENT,
                    supportCase.getId(),
                    "CASE"
            );
        }

        log.info("Case created successfully: {} by {}", supportCase.getCaseNumber(), customer.getEmail());
        return CaseDto.fromEntity(supportCase);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CaseDto> getCases(
            CaseStatus status,
            CasePriority priority,
            CaseCategory category,
            String query,
            Long customerId,
            Long agentId,
            UserPrincipal principal,
            int page,
            int size
    ) {
        Long targetCustomerId = customerId;
        Long targetAgentId = agentId;

        // Customer tenant isolation
        if (principal.getRole() == Role.CUSTOMER) {
            targetCustomerId = principal.getId();
        }

        Page<SupportCase> cases = caseRepository.filterCases(
                targetCustomerId,
                targetAgentId,
                status,
                priority,
                category,
                query,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return PagedResponse.of(cases.map(CaseDto::fromEntity));
    }

    @Transactional(readOnly = true)
    public CaseDetailDto getCaseDetail(Long caseId, UserPrincipal principal) {
        SupportCase supportCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Support case", "id", caseId));

        // Security check: customer can only view own cases
        if (principal.getRole() == Role.CUSTOMER && !supportCase.getCustomer().getId().equals(principal.getId())) {
            throw new AccessDeniedException("You do not have permission to view this case");
        }

        // Fetch comments respecting role visibility
        List<CaseComment> comments;
        if (principal.getRole() == Role.CUSTOMER) {
            comments = commentRepository.findPublicByCaseIdCustomer(caseId);
        } else {
            comments = commentRepository.findAllByCaseIdStaff(caseId);
        }

        List<CaseHistory> history = historyRepository.findBySupportCaseIdOrderByCreatedAtAsc(caseId);

        CaseDto caseDto = CaseDto.fromEntity(supportCase);
        List<CaseCommentDto> commentDtos = comments.stream().map(CaseCommentDto::fromEntity).collect(Collectors.toList());
        List<CaseHistoryDto> historyDtos = history.stream().map(CaseHistoryDto::fromEntity).collect(Collectors.toList());

        return new CaseDetailDto(caseDto, commentDtos, historyDtos);
    }

    @Transactional
    public CaseDto updateStatus(Long caseId, CaseStatusUpdateRequest request, UserPrincipal principal) {
        SupportCase supportCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Support case", "id", caseId));

        User actor = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        // Customer permissions check
        if (principal.getRole() == Role.CUSTOMER) {
            if (!supportCase.getCustomer().getId().equals(principal.getId())) {
                throw new AccessDeniedException("You can only modify your own cases");
            }
            if (request.getStatus() != CaseStatus.CLOSED && request.getStatus() != CaseStatus.OPEN) {
                throw new AccessDeniedException("Customers can only close or reopen their cases");
            }
        }

        CaseStatus oldStatus = supportCase.getStatus();
        CaseStatus newStatus = request.getStatus();

        if (oldStatus == newStatus) {
            return CaseDto.fromEntity(supportCase);
        }

        validateStatusTransition(oldStatus, newStatus);

        supportCase.setStatus(newStatus);
        Instant now = Instant.now();

        if (newStatus == CaseStatus.RESOLVED) {
            supportCase.setResolvedAt(now);
            assignmentService.decrementAgentLoad(supportCase.getAssignedAgent());

            notificationService.createNotification(
                    supportCase.getCustomer(),
                    "Case Resolved: " + supportCase.getCaseNumber(),
                    "Your case has been resolved. You may verify and close it or reply if further help is needed.",
                    NotificationType.STATUS_CHANGE,
                    supportCase.getId(),
                    "CASE"
            );
        } else if (newStatus == CaseStatus.CLOSED) {
            supportCase.setClosedAt(now);
            if (oldStatus != CaseStatus.RESOLVED) {
                assignmentService.decrementAgentLoad(supportCase.getAssignedAgent());
            }
        } else if (oldStatus == CaseStatus.RESOLVED && newStatus == CaseStatus.OPEN) {
            // Case reopening workflow
            supportCase.setReopenCount(supportCase.getReopenCount() + 1);
            supportCase.setResolvedAt(null);
            assignmentService.incrementAgentLoad(supportCase.getAssignedAgent());

            if (supportCase.getAssignedAgent() != null) {
                notificationService.createNotification(
                        supportCase.getAssignedAgent(),
                        "Case Reopened: " + supportCase.getCaseNumber(),
                        "Case '" + supportCase.getTitle() + "' was reopened. Reason: " + request.getReason(),
                        NotificationType.STATUS_CHANGE,
                        supportCase.getId(),
                        "CASE"
                );
            }
        }

        supportCase = caseRepository.save(supportCase);

        HistoryAction action = (oldStatus == CaseStatus.RESOLVED && newStatus == CaseStatus.OPEN)
                ? HistoryAction.REOPENED
                : (newStatus == CaseStatus.RESOLVED ? HistoryAction.RESOLVED
                : (newStatus == CaseStatus.CLOSED ? HistoryAction.CLOSED : HistoryAction.STATUS_CHANGED));

        CaseHistory history = new CaseHistory(
                supportCase,
                actor,
                action,
                oldStatus.name(),
                newStatus.name(),
                request.getReason() != null ? request.getReason() : "Status updated to " + newStatus
        );
        historyRepository.save(history);

        log.info("Case {} status updated: {} -> {} by {}", supportCase.getCaseNumber(), oldStatus, newStatus, actor.getEmail());
        return CaseDto.fromEntity(supportCase);
    }

    public void validateStatusTransition(CaseStatus from, CaseStatus to) {
        if (from == CaseStatus.CLOSED) {
            throw new InvalidStateTransitionException("Closed cases cannot transition to any other status. Please create a new case.");
        }
        if (from == CaseStatus.RESOLVED && to != CaseStatus.CLOSED && to != CaseStatus.OPEN) {
            throw new InvalidStateTransitionException("Resolved cases can only be closed or reopened to OPEN.");
        }
    }

    @Transactional
    public CaseDto assignCase(Long caseId, CaseAssignmentRequest request, UserPrincipal principal) {
        SupportCase supportCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Support case", "id", caseId));

        User actor = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        User newAgent = userRepository.findById(request.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent", "id", request.getAgentId()));

        if (newAgent.getRole() != Role.AGENT && newAgent.getRole() != Role.ADMIN) {
            throw new BadRequestException("Target user is not an agent or administrator");
        }

        User oldAgent = supportCase.getAssignedAgent();
        if (oldAgent != null && oldAgent.getId().equals(newAgent.getId())) {
            return CaseDto.fromEntity(supportCase);
        }

        assignmentService.decrementAgentLoad(oldAgent);
        assignmentService.incrementAgentLoad(newAgent);

        supportCase.setAssignedAgent(newAgent);
        supportCase = caseRepository.save(supportCase);

        String fromAgentName = oldAgent != null ? oldAgent.getFullName() : "UNASSIGNED";
        CaseHistory history = new CaseHistory(
                supportCase,
                actor,
                HistoryAction.REASSIGNED,
                fromAgentName,
                newAgent.getFullName(),
                request.getNote() != null ? request.getNote() : "Reassigned to " + newAgent.getFullName()
        );
        historyRepository.save(history);

        notificationService.createNotification(
                newAgent,
                "Case Assigned: " + supportCase.getCaseNumber(),
                "You have been assigned case '" + supportCase.getTitle() + "'",
                NotificationType.CASE_ASSIGNMENT,
                supportCase.getId(),
                "CASE"
        );

        return CaseDto.fromEntity(supportCase);
    }

    @Transactional
    public CaseCommentDto addComment(Long caseId, CaseCommentRequest request, UserPrincipal principal) {
        SupportCase supportCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Support case", "id", caseId));

        User author = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        boolean isCustomer = (principal.getRole() == Role.CUSTOMER);

        if (isCustomer) {
            if (!supportCase.getCustomer().getId().equals(principal.getId())) {
                throw new AccessDeniedException("You cannot comment on this case");
            }
            if (request.isInternal()) {
                throw new BadRequestException("Customers cannot post internal staff notes");
            }
        }

        CaseComment comment = new CaseComment(
                supportCase,
                author,
                request.getMessage().trim(),
                !isCustomer && request.isInternal()
        );
        comment = commentRepository.save(comment);

        // Record history
        CaseHistory history = new CaseHistory(
                supportCase,
                author,
                HistoryAction.COMMENT_ADDED,
                null,
                comment.isInternal() ? "INTERNAL_NOTE" : "PUBLIC_REPLY",
                "New comment added by " + author.getFullName()
        );
        historyRepository.save(history);

        // Track first response time for SLA
        if (!isCustomer && !comment.isInternal() && supportCase.getFirstRespondedAt() == null) {
            supportCase.setFirstRespondedAt(Instant.now());
            caseRepository.save(supportCase);
        }

        // Customer reply workflow: if waiting for customer, move to in-progress
        if (isCustomer && supportCase.getStatus() == CaseStatus.WAITING_FOR_CUSTOMER) {
            supportCase.setStatus(CaseStatus.IN_PROGRESS);
            caseRepository.save(supportCase);
            historyRepository.save(new CaseHistory(
                    supportCase,
                    author,
                    HistoryAction.STATUS_CHANGED,
                    CaseStatus.WAITING_FOR_CUSTOMER.name(),
                    CaseStatus.IN_PROGRESS.name(),
                    "Status moved to IN_PROGRESS upon customer reply"
            ));
        }

        // Notifications
        if (isCustomer) {
            if (supportCase.getAssignedAgent() != null) {
                notificationService.createNotification(
                        supportCase.getAssignedAgent(),
                        "Customer Replied: " + supportCase.getCaseNumber(),
                        "Customer " + author.getFullName() + " replied to case: " + supportCase.getTitle(),
                        NotificationType.CUSTOMER_REPLY,
                        supportCase.getId(),
                        "CASE"
                );
            }
        } else if (!comment.isInternal()) {
            notificationService.createNotification(
                    supportCase.getCustomer(),
                    "Agent Response: " + supportCase.getCaseNumber(),
                    "Agent " + author.getFullName() + " replied to your support case",
                    NotificationType.AGENT_REPLY,
                    supportCase.getId(),
                    "CASE"
            );
        }

        return CaseCommentDto.fromEntity(comment);
    }
}
