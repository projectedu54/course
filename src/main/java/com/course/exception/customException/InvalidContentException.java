package com.course.exception.customException;

public class InvalidContentException extends RuntimeException {

    public InvalidContentException(String message) {
        super(message);
    }
}
