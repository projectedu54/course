package com.course.dto;

public class TopicResponse {

    private Long id;
    private String title;
    private Integer displayOrder;
    private String parentType;
    private Long parentId;

    public TopicResponse(Long id, String title, Integer displayOrder, String parentType, Long parentId) {
        this.id = id;
        this.title = title;
        this.displayOrder = displayOrder;
        this.parentType = parentType;
        this.parentId = parentId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getParentType() { return parentType; }
    public void setParentType(String parentType) { this.parentType = parentType; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}
