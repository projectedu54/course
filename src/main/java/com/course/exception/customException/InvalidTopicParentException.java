package com.course.exception.customException;

public class InvalidTopicParentException extends RuntimeException {

    public InvalidTopicParentException(String message) {
        super(message);
    }
}
