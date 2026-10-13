package com.servicesphere.cases.dto;

import jakarta.validation.constraints.NotNull;

public class CaseAssignmentRequest {

    @NotNull(message = "Agent ID is required")
    private Long agentId;

    private String note;

    public CaseAssignmentRequest() {}

    public CaseAssignmentRequest(Long agentId, String note) {
        this.agentId = agentId;
        this.note = note;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
