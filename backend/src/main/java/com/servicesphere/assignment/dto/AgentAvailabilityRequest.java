package com.servicesphere.assignment.dto;

public class AgentAvailabilityRequest {
    private Boolean available;
    private Integer maxActiveCases;
    private String skills;

    public AgentAvailabilityRequest() {}

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Integer getMaxActiveCases() {
        return maxActiveCases;
    }

    public void setMaxActiveCases(Integer maxActiveCases) {
        this.maxActiveCases = maxActiveCases;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }
}
