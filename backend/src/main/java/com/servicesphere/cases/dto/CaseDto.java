package com.servicesphere.cases.dto;

import com.servicesphere.cases.CaseCategory;
import com.servicesphere.cases.CasePriority;
import com.servicesphere.cases.CaseStatus;
import com.servicesphere.cases.SupportCase;

import java.time.Instant;

public class CaseDto {
    private Long id;
    private String caseNumber;
    private String title;
    private String description;
    private CaseCategory category;
    private CasePriority priority;
    private CaseStatus status;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long assignedAgentId;
    private String assignedAgentName;
    private Instant firstResponseDeadline;
    private Instant resolutionDeadline;
    private Instant firstRespondedAt;
    private Instant resolvedAt;
    private Instant closedAt;
    private boolean slaEscalated;
    private int reopenCount;
    private Instant createdAt;
    private Instant updatedAt;

    public CaseDto() {}

    public static CaseDto fromEntity(SupportCase c) {
        CaseDto dto = new CaseDto();
        dto.setId(c.getId());
        dto.setCaseNumber(c.getCaseNumber());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setCategory(c.getCategory());
        dto.setPriority(c.getPriority());
        dto.setStatus(c.getStatus());
        dto.setCustomerId(c.getCustomer().getId());
        dto.setCustomerName(c.getCustomer().getFullName());
        dto.setCustomerEmail(c.getCustomer().getEmail());

        if (c.getAssignedAgent() != null) {
            dto.setAssignedAgentId(c.getAssignedAgent().getId());
            dto.setAssignedAgentName(c.getAssignedAgent().getFullName());
        }

        dto.setFirstResponseDeadline(c.getFirstResponseDeadline());
        dto.setResolutionDeadline(c.getResolutionDeadline());
        dto.setFirstRespondedAt(c.getFirstRespondedAt());
        dto.setResolvedAt(c.getResolvedAt());
        dto.setClosedAt(c.getClosedAt());
        dto.setSlaEscalated(c.isSlaEscalated());
        dto.setReopenCount(c.getReopenCount());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CaseCategory getCategory() {
        return category;
    }

    public void setCategory(CaseCategory category) {
        this.category = category;
    }

    public CasePriority getPriority() {
        return priority;
    }

    public void setPriority(CasePriority priority) {
        this.priority = priority;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus status) {
        this.status = status;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public Long getAssignedAgentId() {
        return assignedAgentId;
    }

    public void setAssignedAgentId(Long assignedAgentId) {
        this.assignedAgentId = assignedAgentId;
    }

    public String getAssignedAgentName() {
        return assignedAgentName;
    }

    public void setAssignedAgentName(String assignedAgentName) {
        this.assignedAgentName = assignedAgentName;
    }

    public Instant getFirstResponseDeadline() {
        return firstResponseDeadline;
    }

    public void setFirstResponseDeadline(Instant firstResponseDeadline) {
        this.firstResponseDeadline = firstResponseDeadline;
    }

    public Instant getResolutionDeadline() {
        return resolutionDeadline;
    }

    public void setResolutionDeadline(Instant resolutionDeadline) {
        this.resolutionDeadline = resolutionDeadline;
    }

    public Instant getFirstRespondedAt() {
        return firstRespondedAt;
    }

    public void setFirstRespondedAt(Instant firstRespondedAt) {
        this.firstRespondedAt = firstRespondedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public boolean isSlaEscalated() {
        return slaEscalated;
    }

    public void setSlaEscalated(boolean slaEscalated) {
        this.slaEscalated = slaEscalated;
    }

    public int getReopenCount() {
        return reopenCount;
    }

    public void setReopenCount(int reopenCount) {
        this.reopenCount = reopenCount;
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
