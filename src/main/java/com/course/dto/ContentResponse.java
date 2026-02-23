package com.course.dto;

import com.course.enums.ContentType;

public class ContentResponse {

    private Long id;
    private String title;
    private ContentType contentType;
    private String contentUrl;
    private String textContent;
    private Integer displayOrder;
    private Long topicId;
    private String description;

    public ContentResponse(Long id, String title, ContentType contentType,
                           String contentUrl, String textContent,
                           Integer displayOrder, Long topicId,String description) {
        this.id = id;
        this.title = title;
        this.contentType = contentType;
        this.contentUrl = contentUrl;
        this.textContent = textContent;
        this.displayOrder = displayOrder;
        this.topicId = topicId;
        this.description  = description;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public ContentType getContentType() { return contentType; }
    public void setContentType(ContentType contentType) { this.contentType = contentType; }

    public String getContentUrl() { return contentUrl; }
    public void setContentUrl(String contentUrl) { this.contentUrl = contentUrl; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
