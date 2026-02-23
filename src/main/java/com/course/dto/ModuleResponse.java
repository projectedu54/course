package com.course.dto;

public class ModuleResponse {

    private Long id;
    private String title;
    private Integer displayOrder;
    private String description;

    public ModuleResponse(Long id, String title, Integer displayOrder,String description) {
        this.id = id;
        this.title = title;
        this.displayOrder = displayOrder;
        this.description  = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
