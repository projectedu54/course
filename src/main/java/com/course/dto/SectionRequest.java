package com.course.dto;

import jakarta.validation.constraints.NotBlank;

public class SectionRequest {

    @NotBlank(message = "Section title is required")
    private String title;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
