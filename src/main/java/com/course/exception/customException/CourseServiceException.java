package com.course.exception.customException;

public class CourseServiceException extends RuntimeException {

    private final int status;

    public CourseServiceException(String message) {
        super(message);
        this.status = 400; // default BAD_REQUEST
    }

    public CourseServiceException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
