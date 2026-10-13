package com.servicesphere.analytics.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsDto {
    private long totalCases;
    private long openCases;
    private long inProgressCases;
    private long waitingCustomerCases;
    private long resolvedCases;
    private long closedCases;
    private long overdueCases;
    private double slaComplianceRate;
    private double avgFirstResponseHours;
    private double avgResolutionHours;
    private double reopeningRate;

    private Map<String, Long> casesByPriority;
    private Map<String, Long> casesByCategory;
    private List<AgentWorkloadDto> agentWorkloads;
    private List<TimeSeriesPoint> volumeOverTime;

    public AnalyticsDto() {}

    public long getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(long totalCases) {
        this.totalCases = totalCases;
    }

    public long getOpenCases() {
        return openCases;
    }

    public void setOpenCases(long openCases) {
        this.openCases = openCases;
    }

    public long getInProgressCases() {
        return inProgressCases;
    }

    public void setInProgressCases(long inProgressCases) {
        this.inProgressCases = inProgressCases;
    }

    public long getWaitingCustomerCases() {
        return waitingCustomerCases;
    }

    public void setWaitingCustomerCases(long waitingCustomerCases) {
        this.waitingCustomerCases = waitingCustomerCases;
    }

    public long getResolvedCases() {
        return resolvedCases;
    }

    public void setResolvedCases(long resolvedCases) {
        this.resolvedCases = resolvedCases;
    }

    public long getClosedCases() {
        return closedCases;
    }

    public void setClosedCases(long closedCases) {
        this.closedCases = closedCases;
    }

    public long getOverdueCases() {
        return overdueCases;
    }

    public void setOverdueCases(long overdueCases) {
        this.overdueCases = overdueCases;
    }

    public double getSlaComplianceRate() {
        return slaComplianceRate;
    }

    public void setSlaComplianceRate(double slaComplianceRate) {
        this.slaComplianceRate = slaComplianceRate;
    }

    public double getAvgFirstResponseHours() {
        return avgFirstResponseHours;
    }

    public void setAvgFirstResponseHours(double avgFirstResponseHours) {
        this.avgFirstResponseHours = avgFirstResponseHours;
    }

    public double getAvgResolutionHours() {
        return avgResolutionHours;
    }

    public void setAvgResolutionHours(double avgResolutionHours) {
        this.avgResolutionHours = avgResolutionHours;
    }

    public double getReopeningRate() {
        return reopeningRate;
    }

    public void setReopeningRate(double reopeningRate) {
        this.reopeningRate = reopeningRate;
    }

    public Map<String, Long> getCasesByPriority() {
        return casesByPriority;
    }

    public void setCasesByPriority(Map<String, Long> casesByPriority) {
        this.casesByPriority = casesByPriority;
    }

    public Map<String, Long> getCasesByCategory() {
        return casesByCategory;
    }

    public void setCasesByCategory(Map<String, Long> casesByCategory) {
        this.casesByCategory = casesByCategory;
    }

    public List<AgentWorkloadDto> getAgentWorkloads() {
        return agentWorkloads;
    }

    public void setAgentWorkloads(List<AgentWorkloadDto> agentWorkloads) {
        this.agentWorkloads = agentWorkloads;
    }

    public List<TimeSeriesPoint> getVolumeOverTime() {
        return volumeOverTime;
    }

    public void setVolumeOverTime(List<TimeSeriesPoint> volumeOverTime) {
        this.volumeOverTime = volumeOverTime;
    }

    public record AgentWorkloadDto(String agentName, int currentCases, int maxCapacity, double utilizationRate) {}
    public record TimeSeriesPoint(String label, long count) {}
}
