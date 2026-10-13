package com.servicesphere.cases.dto;

import com.servicesphere.cases.CaseHistory;
import com.servicesphere.cases.HistoryAction;

import java.time.Instant;

public class CaseHistoryDto {
    private Long id;
    private Long caseId;
    private Long actorId;
    private String actorName;
    private String actorRole;
    private HistoryAction action;
    private String fromValue;
    private String toValue;
    private String note;
    private Instant createdAt;

    public CaseHistoryDto() {}

    public static CaseHistoryDto fromEntity(CaseHistory history) {
        CaseHistoryDto dto = new CaseHistoryDto();
        dto.setId(history.getId());
        dto.setCaseId(history.getSupportCase().getId());
        dto.setActorId(history.getActor().getId());
        dto.setActorName(history.getActor().getFullName());
        dto.setActorRole(history.getActor().getRole().name());
        dto.setAction(history.getAction());
        dto.setFromValue(history.getFromValue());
        dto.setToValue(history.getToValue());
        dto.setNote(history.getNote());
        dto.setCreatedAt(history.getCreatedAt());
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

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public HistoryAction getAction() {
        return action;
    }

    public void setAction(HistoryAction action) {
        this.action = action;
    }

    public String getFromValue() {
        return fromValue;
    }

    public void setFromValue(String fromValue) {
        this.fromValue = fromValue;
    }

    public String getToValue() {
        return toValue;
    }

    public void setToValue(String toValue) {
        this.toValue = toValue;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
