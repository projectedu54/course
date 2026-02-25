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
    IMAGE;

    @JsonCreator
    public static ContentType from(String value) {
        return ContentType.valueOf(value.toUpperCase());
    }
}
