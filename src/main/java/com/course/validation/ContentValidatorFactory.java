package com.course.validation;

import com.course.enums.ContentType;
import com.course.exception.customException.InvalidContentException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ContentValidatorFactory {

    private final Map<ContentType, ContentValidator> validators =
            new EnumMap<>(ContentType.class);

    public ContentValidatorFactory(List<ContentValidator> validatorList) {

        for (ContentValidator validator : validatorList) {

            if (validator instanceof TextContentValidator) {
                validators.put(ContentType.TEXT, validator);
            } else if (validator instanceof AudioContentValidator) {
                validators.put(ContentType.AUDIO, validator);
            } else if (validator instanceof ImageContentValidator) {
                validators.put(ContentType.IMAGE, validator);
            } else if (validator instanceof QuizContentValidator) {
                validators.put(ContentType.QUIZ, validator);
            } else if (validator instanceof VideoContentValidator) {
                validators.put(ContentType.VIDEO, validator);
            } else if (validator instanceof DocumentContentValidator) {
                // Handles PDF, PPT, DOC, DOCX
                validators.put(ContentType.PDF, validator);
                validators.put(ContentType.PPT, validator);
                validators.put(ContentType.DOC, validator);
                validators.put(ContentType.DOCX, validator);
            } else if (validator instanceof LinkContentValidator) {
                validators.put(ContentType.LINK, validator);
            }
        }
    }

    public ContentValidator getValidator(ContentType type) {
        ContentValidator validator = validators.get(type);

        if (validator == null) {
            throw new InvalidContentException("Unsupported content type: " + type);
        }

        return validator;
    }
}