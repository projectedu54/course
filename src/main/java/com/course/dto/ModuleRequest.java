package com.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create or update a module. displayOrder is auto-generated.")
public class ModuleRequest {

    @NotBlank(message = "Module title is required")
    @Schema(
            description = "Title of the module",
            example = "Module 1: Basics",
            required = true
    )
    private String title;

    @Schema(
            description = "Detailed description of the module",
            example = "This module introduces the fundamental concepts required to understand the course."
    )
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