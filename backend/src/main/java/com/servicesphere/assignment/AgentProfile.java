package com.servicesphere.assignment;

import com.servicesphere.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "agent_profiles")
public class AgentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 500)
    private String skills; // Comma separated, e.g. "BILLING,TECHNICAL"

    @Column(name = "max_active_cases", nullable = false)
    private int maxActiveCases = 10;

    @Column(name = "current_active_cases", nullable = false)
    private int currentActiveCases = 0;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AgentProfile() {}

    public AgentProfile(User user, String skills, int maxActiveCases) {
        this.user = user;
        this.skills = skills;
        this.maxActiveCases = maxActiveCases;
        this.currentActiveCases = 0;
        this.available = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public List<String> getSkillList() {
        if (skills == null || skills.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    public boolean hasSkill(String category) {
        if (category == null) return false;
        return getSkillList().contains(category.trim().toUpperCase());
    }

    public boolean canAcceptMoreCases() {
        return available && currentActiveCases < maxActiveCases;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
