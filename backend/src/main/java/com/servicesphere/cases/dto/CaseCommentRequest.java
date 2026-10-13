package com.servicesphere.cases.dto;

import jakarta.validation.constraints.NotBlank;

public class CaseCommentRequest {

    @NotBlank(message = "Comment message cannot be empty")
    private String message;

    private boolean internal = false;

    public CaseCommentRequest() {}

    public CaseCommentRequest(String message, boolean internal) {
        this.message = message;
        this.internal = internal;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }
}
