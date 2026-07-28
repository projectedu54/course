package com.course.validation;

import com.course.dto.ContentRequest;
import com.course.exception.customException.InvalidContentException;
import org.springframework.stereotype.Component;

import static com.course.util.ContentValidationUtil.*;

@Component
public class DocumentContentValidator implements ContentValidator {

    @Override
    public void validate(ContentRequest request) {
        // If content URL is provided, validate extension or structure
        if (request.getContentUrl() != null && !request.getContentUrl().isBlank()) {
            validateUrl(request.getContentUrl(), "DOCUMENT");
        }

        if (hasText(request.getTextContent())) {
            throw new InvalidContentException(
                    "Document content (PDF, PPT, DOC, DOCX) must not contain textContent directly"
            );
        }
    }
}