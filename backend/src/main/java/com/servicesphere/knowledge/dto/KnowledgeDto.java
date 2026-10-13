package com.servicesphere.knowledge.dto;

import com.servicesphere.knowledge.KnowledgeArticle;
import java.time.Instant;

public class KnowledgeDto {
    private Long id;
    private String title;
    private String summary;
    private String body;
    private String category;
    private String tags;
    private boolean published;
    private Long authorId;
    private String authorName;
    private int viewCount;
    private int helpfulCount;
    private int notHelpfulCount;
    private Instant createdAt;
    private Instant updatedAt;

    public KnowledgeDto() {}

    public static KnowledgeDto fromEntity(KnowledgeArticle article) {
        KnowledgeDto dto = new KnowledgeDto();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setSummary(article.getSummary());
        dto.setBody(article.getBody());
        dto.setCategory(article.getCategory());
        dto.setTags(article.getTags());
        dto.setPublished(article.isPublished());
        dto.setAuthorId(article.getAuthor().getId());
        dto.setAuthorName(article.getAuthor().getFullName());
        dto.setViewCount(article.getViewCount());
        dto.setHelpfulCount(article.getHelpfulCount());
        dto.setNotHelpfulCount(article.getNotHelpfulCount());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
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

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(int helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public int getNotHelpfulCount() {
        return notHelpfulCount;
    }

    public void setNotHelpfulCount(int notHelpfulCount) {
        this.notHelpfulCount = notHelpfulCount;
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
