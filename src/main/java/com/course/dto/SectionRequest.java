package com.course.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to create or update a section. displayOrder is auto-generated.")
public class SectionRequest {

    @NotBlank(message = "Section title is required")
    @Schema(description = "Title of the section", example = "Getting Started", required = true)
    private String title;

    @Schema(description = "Description of the section", example = "This section covers the basics of the course.")
    private String description;



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}