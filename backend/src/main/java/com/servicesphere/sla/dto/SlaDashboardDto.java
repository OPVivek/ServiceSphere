package com.servicesphere.sla.dto;

public class SlaDashboardDto {
    private long totalActiveCases;
    private long withinSlaCases;
    private long approachingBreachCases;
    private long breachedCases;
    private double compliancePercentage;

    public SlaDashboardDto() {}

    public SlaDashboardDto(long totalActiveCases, long withinSlaCases, long approachingBreachCases, long breachedCases, double compliancePercentage) {
        this.totalActiveCases = totalActiveCases;
        this.withinSlaCases = withinSlaCases;
        this.approachingBreachCases = approachingBreachCases;
        this.breachedCases = breachedCases;
        this.compliancePercentage = compliancePercentage;
    }

    public long getTotalActiveCases() {
        return totalActiveCases;
    }

    public void setTotalActiveCases(long totalActiveCases) {
        this.totalActiveCases = totalActiveCases;
    }

    public long getWithinSlaCases() {
        return withinSlaCases;
    }

    public void setWithinSlaCases(long withinSlaCases) {
        this.withinSlaCases = withinSlaCases;
    }

    public long getApproachingBreachCases() {
        return approachingBreachCases;
    }

    public void setApproachingBreachCases(long approachingBreachCases) {
        this.approachingBreachCases = approachingBreachCases;
    }

    public long getBreachedCases() {
        return breachedCases;
    }

    public void setBreachedCases(long breachedCases) {
        this.breachedCases = breachedCases;
    }

    public double getCompliancePercentage() {
        return compliancePercentage;
    }

    public void setCompliancePercentage(double compliancePercentage) {
        this.compliancePercentage = compliancePercentage;
    }
}
