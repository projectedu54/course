package com.course.exception.customException;

public class InvalidCourseStructureException extends RuntimeException {

    public InvalidCourseStructureException(String message) {
        super(message);
    }
}