package com.course.dto;

public class ChapterActionResponse {

    private String message;
    private Long courseId;
    private ChapterResponse chapter;

    public ChapterActionResponse(String message, Long courseId, ChapterResponse chapter) {
        this.message = message;
        this.courseId = courseId;
        this.chapter = chapter;
    }

    public String getMessage() { return message; }
    public Long getCourseId() { return courseId; }
    public ChapterResponse getChapter() { return chapter; }
}
