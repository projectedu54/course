package com.course.exception.customException;

import java.util.List;

public class CourseValidationException extends RuntimeException {
    private final List<String> errors;

    public CourseValidationException(List<String> errors) {
        super("Course validation failed");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}