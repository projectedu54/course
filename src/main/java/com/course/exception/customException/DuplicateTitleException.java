package com.course.exception.customException;

import org.springframework.http.HttpStatus;

public class DuplicateTitleException extends RuntimeException {

    private final HttpStatus status;

    public DuplicateTitleException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST; // 400 for duplicate
    }

    public HttpStatus getStatus() {
        return status;
    }
}