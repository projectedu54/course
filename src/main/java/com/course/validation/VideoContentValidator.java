package com.course.validation;

import com.course.dto.ContentRequest;
import com.course.exception.customException.InvalidContentException;
import org.springframework.stereotype.Component;

import static com.course.util.ContentValidationUtil.*;

@Component
public class VideoContentValidator implements ContentValidator {

    @Override
    public void validate(ContentRequest request) {

        validateUrl(request.getContentUrl(), "VIDEO");
        validateVideoFileExtension(request.getContentUrl());

        if (hasText(request.getTextContent())) {
            throw new InvalidContentException(
                    "VIDEO content must not contain textContent"
            );
        }
    }
}