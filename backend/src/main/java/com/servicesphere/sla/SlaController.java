package com.servicesphere.sla;

import com.servicesphere.cases.CasePriority;
import com.servicesphere.cases.CaseRepository;
import com.servicesphere.cases.CaseStatus;
import com.servicesphere.cases.SupportCase;
import com.servicesphere.common.ApiResponse;
import com.servicesphere.sla.dto.SlaDashboardDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sla")
@Tag(name = "SLA Management", description = "SLA policies, compliance dashboard, and targets")
public class SlaController {

    private final SlaService slaService;
    private final CaseRepository caseRepository;

    public SlaController(SlaService slaService, CaseRepository caseRepository) {
        this.slaService = slaService;
        this.caseRepository = caseRepository;
    }

    @GetMapping("/policies")
    @Operation(summary = "Get all configured SLA policies")
    public ResponseEntity<ApiResponse<List<SlaPolicy>>> getPolicies() {
        return ResponseEntity.ok(ApiResponse.success(slaService.getAllPolicies()));
    }

    @PutMapping("/policies/{priority}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update SLA policy targets (Admin only)")
    public ResponseEntity<ApiResponse<SlaPolicy>> updatePolicy(
            @PathVariable CasePriority priority,
            @RequestParam int firstResponseHours,
            @RequestParam int resolutionHours,
            @RequestParam(required = false) String description
    ) {
        SlaPolicy updated = slaService.updatePolicy(priority, firstResponseHours, resolutionHours, description);
        return ResponseEntity.ok(ApiResponse.success("SLA policy updated", updated));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Get SLA compliance dashboard metrics")
    public ResponseEntity<ApiResponse<SlaDashboardDto>> getSlaDashboard() {
        Instant now = Instant.now();
        List<CaseStatus> terminalStatuses = List.of(CaseStatus.RESOLVED, CaseStatus.CLOSED);

        long breachedCount = caseRepository.countBreachedCases(now, terminalStatuses);
        Instant approachingThreshold = now.plus(Duration.ofHours(2));
        List<SupportCase> approachingList = caseRepository.findCasesApproachingDeadline(now, approachingThreshold, terminalStatuses);
        long approachingCount = approachingList.size();

        long openCount = caseRepository.countByStatus(CaseStatus.OPEN);
        long inProgressCount = caseRepository.countByStatus(CaseStatus.IN_PROGRESS);
        long waitingCount = caseRepository.countByStatus(CaseStatus.WAITING_FOR_CUSTOMER);
        long totalActive = openCount + inProgressCount + waitingCount;

        long withinSla = Math.max(0, totalActive - breachedCount);
        double compliance = totalActive > 0 ? ((double) withinSla / totalActive) * 100.0 : 100.0;

        SlaDashboardDto dto = new SlaDashboardDto(
                totalActive,
                withinSla,
                approachingCount,
                breachedCount,
                Math.round(compliance * 10.0) / 10.0
        );
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
