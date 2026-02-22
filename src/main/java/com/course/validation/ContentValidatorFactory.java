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
                validators.put(ContentType.text, validator);
            } else if (validator instanceof AudioContentValidator) {
                validators.put(ContentType.AUDIO, validator);
                validators.put(ContentType.audio, validator);
            } else if (validator instanceof ImageContentValidator) {
                validators.put(ContentType.IMAGE, validator);
                validators.put(ContentType.image, validator);
            } else if (validator instanceof QuizContentValidator) {
                validators.put(ContentType.QUIZ, validator);
                validators.put(ContentType.quiz, validator);
            }
        }
    }

    public ContentValidator getValidator(ContentType type) {

        ContentValidator validator = validators.get(type);

        if (validator == null) {
            throw new InvalidContentException("Unsupported content type");
        }

        return validator;
    }
}