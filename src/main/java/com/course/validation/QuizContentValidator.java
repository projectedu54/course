package com.course.validation;

import com.course.dto.ContentRequest;
import com.course.exception.customException.InvalidContentException;
import org.springframework.stereotype.Component;

@Component
public class QuizContentValidator implements ContentValidator {

    @Override
    public void validate(ContentRequest request) {
        throw new InvalidContentException(
                "QUIZ content must be created via Quiz API"
        );
    }
}