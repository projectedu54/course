package com.course.validation;

import com.course.dto.ContentRequest;

public interface ContentValidator {
    void validate(ContentRequest request);
}