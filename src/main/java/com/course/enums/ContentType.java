package com.course.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ContentType {
    VIDEO,
    PDF,
    TEXT,
    AUDIO,
    LINK,
    PPT,
    QUIZ,
    IMAGE,
    DOC,
    DOCX;

    @JsonCreator
    public static ContentType from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return ContentType.valueOf(value.trim().toUpperCase());
    }
}