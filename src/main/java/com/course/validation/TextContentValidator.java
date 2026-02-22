package com.course.validation;

import com.course.dto.ContentRequest;
import com.course.exception.customException.InvalidContentException;
import org.springframework.stereotype.Component;

import static com.course.util.ContentValidationUtil.*;

@Component
public class TextContentValidator implements ContentValidator {

    private static final int MAX_TEXT_LENGTH = 10_000;

    @Override
    public void validate(ContentRequest request) {

        if (!hasText(request.getTextContent())) {
            throw new InvalidContentException("TEXT content requires textContent");
        }

        if (request.getTextContent().length() > MAX_TEXT_LENGTH) {
            throw new InvalidContentException(
                    "Text content cannot exceed " + MAX_TEXT_LENGTH + " characters"
            );
        }

        if (containsProfanity(request.getTextContent())) {
            throw new InvalidContentException(
                    "Text content contains inappropriate language"
            );
        }

        if (hasText(request.getContentUrl())) {
            throw new InvalidContentException(
                    "TEXT content must not have contentUrl"
            );
        }
    }
}