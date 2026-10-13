package com.servicesphere.cases.dto;

import com.servicesphere.cases.CaseStatus;
import jakarta.validation.constraints.NotNull;

public class CaseStatusUpdateRequest {

    @NotNull(message = "New status is required")
    private CaseStatus status;

    private String reason;

    public CaseStatusUpdateRequest() {}

    public CaseStatusUpdateRequest(CaseStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
