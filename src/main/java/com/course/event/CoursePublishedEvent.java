package com.course.event;

public record CoursePublishedEvent(
        Long courseId,
        Long userId,
        String title,
        String catalogId
) {}