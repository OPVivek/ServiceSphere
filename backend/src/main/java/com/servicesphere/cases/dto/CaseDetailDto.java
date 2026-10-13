package com.servicesphere.cases.dto;

import java.util.List;

public class CaseDetailDto {
    private CaseDto caseDetails;
    private List<CaseCommentDto> comments;
    private List<CaseHistoryDto> history;

    public CaseDetailDto() {}

    public CaseDetailDto(CaseDto caseDetails, List<CaseCommentDto> comments, List<CaseHistoryDto> history) {
        this.caseDetails = caseDetails;
        this.comments = comments;
        this.history = history;
    }

    public CaseDto getCaseDetails() {
        return caseDetails;
    }

    public void setCaseDetails(CaseDto caseDetails) {
        this.caseDetails = caseDetails;
    }

    public List<CaseCommentDto> getComments() {
        return comments;
    }

    public void setComments(List<CaseCommentDto> comments) {
        this.comments = comments;
    }

    public List<CaseHistoryDto> getHistory() {
        return history;
    }

    public void setHistory(List<CaseHistoryDto> history) {
        this.history = history;
    }
}
