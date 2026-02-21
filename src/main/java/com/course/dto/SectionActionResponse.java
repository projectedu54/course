package com.course.dto;

public class SectionActionResponse {
    private String message;
    private Long courseId;
    private SectionResponse section;

    public SectionActionResponse(String message, Long courseId, SectionResponse section) {
        this.message = message;
        this.courseId = courseId;
        this.section = section;
    }

    public String getMessage() { return message; }
    public Long getCourseId() { return courseId; }
    public SectionResponse getSection() { return section; }
}
