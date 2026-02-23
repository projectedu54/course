package com.course.exception.customException;

import org.springframework.http.HttpStatus;

public class DuplicateTopicTitleException extends RuntimeException {

    private final HttpStatus status;

    public DuplicateTopicTitleException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST; // 400 for duplicates
    }

    public HttpStatus getStatus() {
        return status;
    }
}