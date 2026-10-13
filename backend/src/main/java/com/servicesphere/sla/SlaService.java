package com.servicesphere.sla;

import com.servicesphere.cases.*;
import com.servicesphere.notification.NotificationService;
import com.servicesphere.notification.NotificationType;
import com.servicesphere.user.User;
import com.servicesphere.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class SlaService {

    private static final Logger log = LoggerFactory.getLogger(SlaService.class);

    private final SlaPolicyRepository slaPolicyRepository;
    private final CaseRepository caseRepository;
    private final CaseHistoryRepository caseHistoryRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public SlaService(
            SlaPolicyRepository slaPolicyRepository,
            CaseRepository caseRepository,
            CaseHistoryRepository caseHistoryRepository,
            NotificationService notificationService,
            UserRepository userRepository
    ) {
        this.slaPolicyRepository = slaPolicyRepository;
        this.caseRepository = caseRepository;
        this.caseHistoryRepository = caseHistoryRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    public record Deadlines(Instant firstResponseDeadline, Instant resolutionDeadline) {}

    public Deadlines calculateDeadlines(CasePriority priority, Instant createdAt) {
        Optional<SlaPolicy> policyOpt = slaPolicyRepository.findByPriority(priority);

        int responseHours = 8;
        int resolutionHours = 24;

        if (policyOpt.isPresent()) {
            SlaPolicy policy = policyOpt.get();
            responseHours = policy.getFirstResponseHours();
            resolutionHours = policy.getResolutionHours();
        } else {
            // Standard fallback defaults
            switch (priority) {
                case CRITICAL -> { responseHours = 1; resolutionHours = 4; }
                case HIGH -> { responseHours = 4; resolutionHours = 12; }
                case MEDIUM -> { responseHours = 8; resolutionHours = 24; }
                case LOW -> { responseHours = 24; resolutionHours = 72; }
            }
        }

        Instant firstResponseDeadline = createdAt.plus(Duration.ofHours(responseHours));
        Instant resolutionDeadline = createdAt.plus(Duration.ofHours(resolutionHours));

        return new Deadlines(firstResponseDeadline, resolutionDeadline);
    }

    /**
     * Scheduled SLA monitoring task. Runs every 60 seconds.
     * Evaluates active cases, sends early warning for approaching breaches,
     * and escalates breached cases with strict idempotency.
     */
    @Scheduled(fixedDelayString = "${app.sla.check-interval-ms:60000}")
    @Transactional
    public void monitorSlaDeadlines() {
        Instant now = Instant.now();
        List<CaseStatus> terminalStatuses = List.of(CaseStatus.RESOLVED, CaseStatus.CLOSED);

        // 1. Process breached cases
        List<SupportCase> breachedCases = caseRepository.findBreachedCases(now, terminalStatuses);
        for (SupportCase c : breachedCases) {
            escalateCase(c, now);
        }

        // 2. Process approaching breach warnings (within 2 hours)
        Instant approachingThreshold = now.plus(Duration.ofHours(2));
        List<SupportCase> approaching = caseRepository.findCasesApproachingDeadline(now, approachingThreshold, terminalStatuses);
        for (SupportCase c : approaching) {
            if (c.getAssignedAgent() != null) {
                notificationService.createNotification(
                        c.getAssignedAgent(),
                        "SLA Warning: Approaching Deadline",
                        "Case " + c.getCaseNumber() + " is approaching its SLA resolution deadline within 2 hours.",
                        NotificationType.SLA_WARNING,
                        c.getId(),
                        "CASE"
                );
            }
        }
    }

    @Transactional
    public void escalateCase(SupportCase c, Instant now) {
        if (c.isSlaEscalated()) {
            return; // Idempotent safeguard: prevent duplicate escalations
        }

        c.setSlaEscalated(true);
        caseRepository.save(c);

        User systemActor = c.getAssignedAgent() != null ? c.getAssignedAgent() : c.getCustomer();

        // Record SLA escalation in case history
        CaseHistory history = new CaseHistory(
                c,
                systemActor,
                HistoryAction.ESCALATED,
                "FALSE",
                "TRUE",
                "Automated SLA Monitor: Resolution deadline breached at " + now.toString()
        );
        caseHistoryRepository.save(history);

        log.warn("SLA breached and escalated for case {} ({})", c.getCaseNumber(), c.getTitle());

        // Notify assigned agent if present
        if (c.getAssignedAgent() != null) {
            notificationService.createNotification(
                    c.getAssignedAgent(),
                    "SLA Breach Escalation",
                    "Case " + c.getCaseNumber() + " has breached SLA resolution deadline and has been escalated.",
                    NotificationType.SLA_BREACH,
                    c.getId(),
                    "CASE"
            );
        }

        // Notify Admins
        List<User> admins = userRepository.findByRole(com.servicesphere.security.Role.ADMIN);
        for (User admin : admins) {
            notificationService.createNotification(
                    admin,
                    "URGENT: SLA Breach Escalation",
                    "Case " + c.getCaseNumber() + " (" + c.getPriority() + ") breached resolution deadline.",
                    NotificationType.SLA_BREACH,
                    c.getId(),
                    "CASE"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<SlaPolicy> getAllPolicies() {
        return slaPolicyRepository.findAll();
    }

    @Transactional
    public SlaPolicy updatePolicy(CasePriority priority, int firstResponseHours, int resolutionHours, String description) {
        SlaPolicy policy = slaPolicyRepository.findByPriority(priority)
                .orElse(new SlaPolicy(priority, firstResponseHours, resolutionHours, description));

        policy.setFirstResponseHours(firstResponseHours);
        policy.setResolutionHours(resolutionHours);
        policy.setDescription(description);
        return slaPolicyRepository.save(policy);
    }
}
