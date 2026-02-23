package com.course.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Course content structure type",
        enumAsRef = true
)
public enum CourseStructure {

    @Schema(description = "Course contains modules")
    MODULE,

    @Schema(description = "Course contains chapters")
    CHAPTER,

    @Schema(description = "Course contains sections")
    SECTION;

    @JsonCreator
    public static CourseStructure from(String value) {
        return CourseStructure.valueOf(value.toUpperCase());
    }
}
