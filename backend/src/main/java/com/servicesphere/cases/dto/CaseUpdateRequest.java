package com.servicesphere.cases.dto;

import com.servicesphere.cases.CaseCategory;
import com.servicesphere.cases.CasePriority;

public class CaseUpdateRequest {
    private String title;
    private String description;
    private CaseCategory category;
    private CasePriority priority;

    public CaseUpdateRequest() {}

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
}
