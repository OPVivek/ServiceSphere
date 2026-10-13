package com.servicesphere.analytics;

import com.servicesphere.analytics.dto.AnalyticsDto;
import com.servicesphere.assignment.AgentProfile;
import com.servicesphere.assignment.AgentProfileRepository;
import com.servicesphere.cases.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final CaseRepository caseRepository;
    private final AgentProfileRepository agentProfileRepository;

    public AnalyticsService(CaseRepository caseRepository, AgentProfileRepository agentProfileRepository) {
        this.caseRepository = caseRepository;
        this.agentProfileRepository = agentProfileRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsDto getSystemAnalytics() {
        List<SupportCase> allCases = caseRepository.findAll();
        long totalCases = allCases.size();

        long open = allCases.stream().filter(c -> c.getStatus() == CaseStatus.OPEN).count();
        long inProgress = allCases.stream().filter(c -> c.getStatus() == CaseStatus.IN_PROGRESS).count();
        long waiting = allCases.stream().filter(c -> c.getStatus() == CaseStatus.WAITING_FOR_CUSTOMER).count();
        long resolved = allCases.stream().filter(c -> c.getStatus() == CaseStatus.RESOLVED).count();
        long closed = allCases.stream().filter(c -> c.getStatus() == CaseStatus.CLOSED).count();

        Instant now = Instant.now();
        List<CaseStatus> terminalStatuses = List.of(CaseStatus.RESOLVED, CaseStatus.CLOSED);
        long overdue = caseRepository.countBreachedCases(now, terminalStatuses);

        // SLA compliance rate
        long breachedOrEscalated = allCases.stream().filter(SupportCase::isSlaEscalated).count();
        double slaCompliance = totalCases > 0
                ? Math.round(((double) (totalCases - breachedOrEscalated) / totalCases) * 1000.0) / 10.0
                : 100.0;

        // Avg first response time (hours)
        List<Double> responseHoursList = allCases.stream()
                .filter(c -> c.getFirstRespondedAt() != null)
                .map(c -> Duration.between(c.getCreatedAt(), c.getFirstRespondedAt()).toMinutes() / 60.0)
                .toList();

        double avgResponseHours = responseHoursList.isEmpty() ? 0.0 :
                Math.round(responseHoursList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0) * 10.0) / 10.0;

        // Avg resolution time (hours)
        List<Double> resolutionHoursList = allCases.stream()
                .filter(c -> c.getResolvedAt() != null)
                .map(c -> Duration.between(c.getCreatedAt(), c.getResolvedAt()).toMinutes() / 60.0)
                .toList();

        double avgResolutionHours = resolutionHoursList.isEmpty() ? 0.0 :
                Math.round(resolutionHoursList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0) * 10.0) / 10.0;

        // Reopening rate
        long reopenedCases = allCases.stream().filter(c -> c.getReopenCount() > 0).count();
        double reopeningRate = totalCases > 0
                ? Math.round(((double) reopenedCases / totalCases) * 1000.0) / 10.0
                : 0.0;

        // Cases by priority
        Map<String, Long> casesByPriority = new LinkedHashMap<>();
        for (CasePriority p : CasePriority.values()) {
            casesByPriority.put(p.name(), allCases.stream().filter(c -> c.getPriority() == p).count());
        }

        // Cases by category
        Map<String, Long> casesByCategory = new LinkedHashMap<>();
        for (CaseCategory cat : CaseCategory.values()) {
            casesByCategory.put(cat.name(), allCases.stream().filter(c -> c.getCategory() == cat).count());
        }

        // Agent workloads
        List<AgentProfile> profiles = agentProfileRepository.findAllWithUser();
        List<AnalyticsDto.AgentWorkloadDto> agentWorkloads = profiles.stream()
                .map(p -> new AnalyticsDto.AgentWorkloadDto(
                        p.getUser().getFullName(),
                        p.getCurrentActiveCases(),
                        p.getMaxActiveCases(),
                        Math.round(((double) p.getCurrentActiveCases() / p.getMaxActiveCases()) * 1000.0) / 10.0
                ))
                .collect(Collectors.toList());

        // Volume over time (grouped by day)
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MMM dd").withZone(ZoneId.of("UTC"));
        Map<String, Long> dayCounts = allCases.stream()
                .collect(Collectors.groupingBy(c -> dayFormatter.format(c.getCreatedAt()), Collectors.counting()));

        List<AnalyticsDto.TimeSeriesPoint> volumeOverTime = dayCounts.entrySet().stream()
                .map(e -> new AnalyticsDto.TimeSeriesPoint(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        AnalyticsDto dto = new AnalyticsDto();
        dto.setTotalCases(totalCases);
        dto.setOpenCases(open);
        dto.setInProgressCases(inProgress);
        dto.setWaitingCustomerCases(waiting);
        dto.setResolvedCases(resolved);
        dto.setClosedCases(closed);
        dto.setOverdueCases(overdue);
        dto.setSlaComplianceRate(slaCompliance);
        dto.setAvgFirstResponseHours(avgResponseHours);
        dto.setAvgResolutionHours(avgResolutionHours);
        dto.setReopeningRate(reopeningRate);
        dto.setCasesByPriority(casesByPriority);
        dto.setCasesByCategory(casesByCategory);
        dto.setAgentWorkloads(agentWorkloads);
        dto.setVolumeOverTime(volumeOverTime);

        return dto;
    }
}
