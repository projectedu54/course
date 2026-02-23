package com.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create or update a chapter. displayOrder is auto-generated.")
public class ChapterRequest {

    @NotBlank(message = "Chapter title is required")
    private String title;

    @Schema(description = "Description of the chapter")
    private String description;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
