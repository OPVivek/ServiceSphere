package com.servicesphere.sla;

import com.servicesphere.cases.CasePriority;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sla_policies")
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private CasePriority priority;

    @Column(name = "first_response_hours", nullable = false)
    private int firstResponseHours;

    @Column(name = "resolution_hours", nullable = false)
    private int resolutionHours;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SlaPolicy() {}

    public SlaPolicy(CasePriority priority, int firstResponseHours, int resolutionHours, String description) {
        this.priority = priority;
        this.firstResponseHours = firstResponseHours;
        this.resolutionHours = resolutionHours;
        this.description = description;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CasePriority getPriority() {
        return priority;
    }

    public void setPriority(CasePriority priority) {
        this.priority = priority;
    }

    public int getFirstResponseHours() {
        return firstResponseHours;
    }

    public void setFirstResponseHours(int firstResponseHours) {
        this.firstResponseHours = firstResponseHours;
    }

    public int getResolutionHours() {
        return resolutionHours;
    }

    public void setResolutionHours(int resolutionHours) {
        this.resolutionHours = resolutionHours;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
