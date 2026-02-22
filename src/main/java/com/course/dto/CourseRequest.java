package com.course.dto;

import com.course.enums.CourseStructure;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.Set;

public class CourseRequest {

    @NotBlank(message = "Title is mandatory")
    private String title;

    @Size(max = 1000, message = "Description can be at most 1000 characters")
    private String description;

    @NotBlank(message = "Course type is mandatory")
    private String courseType;

    private String status;

    @NotNull(message = "Catalog ID is mandatory")
    @Min(value = 1, message = "Catalog ID must be greater than 0")
    private Integer catalogId;

    private Set<String> tags; // Optional

    @NotNull(message = "Course structure is mandatory")
    @Schema(
            description = "Defines how course content is structured",
            allowableValues = {"MODULE", "CHAPTER", "SECTION"},
            example = "MODULE",
            required = true
    )
    private CourseStructure courseStructure;

    // Getters and Setters
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

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(Integer catalogId) {
        this.catalogId = catalogId;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public CourseStructure getCourseStructure() {return courseStructure;}

    public void setCourseStructure(CourseStructure courseStructure) {this.courseStructure = courseStructure;}
}
