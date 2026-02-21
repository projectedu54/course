package com.course.dto;

import jakarta.validation.constraints.NotBlank;

public class TopicRequest {

    @NotBlank(message = "Topic title is required")
    private String title;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
