package com.servicesphere.assignment.dto;

import com.servicesphere.assignment.AgentProfile;

public class AgentDto {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String skills;
    private int maxActiveCases;
    private int currentActiveCases;
    private boolean available;

    public AgentDto() {}

    public static AgentDto fromEntity(AgentProfile profile) {
        AgentDto dto = new AgentDto();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUser().getId());
        dto.setName(profile.getUser().getFullName());
        dto.setEmail(profile.getUser().getEmail());
        dto.setSkills(profile.getSkills());
        dto.setMaxActiveCases(profile.getMaxActiveCases());
        dto.setCurrentActiveCases(profile.getCurrentActiveCases());
        dto.setAvailable(profile.isAvailable());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public int getMaxActiveCases() {
        return maxActiveCases;
    }

    public void setMaxActiveCases(int maxActiveCases) {
        this.maxActiveCases = maxActiveCases;
    }

    public int getCurrentActiveCases() {
        return currentActiveCases;
    }

    public void setCurrentActiveCases(int currentActiveCases) {
        this.currentActiveCases = currentActiveCases;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
