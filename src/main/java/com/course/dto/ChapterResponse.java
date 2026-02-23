package com.course.dto;

public class ChapterResponse {

    private Long id;
    private String title;
    private String description;
    private Integer displayOrder;

    public ChapterResponse(Long id, String title, Integer displayOrder,String description) {
        this.id = id;
        this.title = title;
        this.displayOrder = displayOrder;
        this.description  = description;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Integer getDisplayOrder() { return displayOrder; }
    public String getDescription() { return description; }
}
