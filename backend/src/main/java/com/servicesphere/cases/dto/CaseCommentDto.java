package com.servicesphere.cases.dto;

import com.servicesphere.cases.CaseComment;
import java.time.Instant;

public class CaseCommentDto {
    private Long id;
    private Long caseId;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String message;
    private boolean internal;
    private Instant createdAt;

    public CaseCommentDto() {}

    public static CaseCommentDto fromEntity(CaseComment comment) {
        CaseCommentDto dto = new CaseCommentDto();
        dto.setId(comment.getId());
        dto.setCaseId(comment.getSupportCase().getId());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setAuthorName(comment.getAuthor().getFullName());
        dto.setAuthorRole(comment.getAuthor().getRole().name());
        dto.setMessage(comment.getMessage());
        dto.setInternal(comment.isInternal());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
