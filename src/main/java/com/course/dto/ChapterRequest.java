package com.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create or update a chapter. displayOrder is auto-generated.")
public class ChapterRequest {

    @NotBlank(message = "Chapter title is required")
    @Schema(description = "Title of the chapter", example = "Introduction to Java", required = true)
    private String title;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
