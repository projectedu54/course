package com.course.validation;

import com.course.dto.ContentRequest;
import com.course.exception.customException.InvalidContentException;
import org.springframework.stereotype.Component;

import static com.course.util.ContentValidationUtil.*;

@Component
public class ImageContentValidator implements ContentValidator {

    @Override
    public void validate(ContentRequest request) {

        validateUrl(request.getContentUrl(), "IMAGE");
        validateImageFileExtension(request.getContentUrl());

        if (hasText(request.getTextContent())) {
            throw new InvalidContentException(
                    "IMAGE content must not contain textContent"
            );
        }
    }
}