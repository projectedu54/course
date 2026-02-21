package com.course.dto;

public class ModuleResponse {

    private Long id;
    private String title;
    private Integer displayOrder;

    public ModuleResponse(Long id, String title, Integer displayOrder) {
        this.id = id;
        this.title = title;
        this.displayOrder = displayOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
