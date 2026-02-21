package com.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create or update a module. displayOrder is auto-generated.")
public class ModuleRequest {

    @NotBlank(message = "Module title is required")
    @Schema(description = "Title of the module", example = "Module 1: Basics", required = true)
    private String title;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
