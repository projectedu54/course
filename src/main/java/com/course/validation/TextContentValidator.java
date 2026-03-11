package com.course.validation;

import com.course.dto.ContentRequest;
import com.course.exception.customException.InvalidContentException;
import com.course.util.ContentValidationUtil;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import static com.course.util.ContentValidationUtil.*;

@Component
public class TextContentValidator implements ContentValidator {

    private static final int MAX_HTML_LENGTH = 5_000_000; // 5MB

    @Override
    public void validate(ContentRequest request) {
        String rawHtml = request.getTextContent();

        // 1. Basic presence check
        if (!hasText(rawHtml)) {
            throw new InvalidContentException("TEXT content requires textContent");
        }

        // 2. Size validation
        if (rawHtml.length() > MAX_HTML_LENGTH) {
            throw new InvalidContentException(
                    "HTML content cannot exceed " + (MAX_HTML_LENGTH / 1_000_000) + " MB"
            );
        }

        // 3. SANITIZATION (The Core Security Step)
        // Instead of blocking with isValid(), we clean the HTML.
        // This removes <script> but allows <pre>, <code>, etc.
        String cleanHtml = ContentValidationUtil.sanitize(rawHtml);

        // 4. Profanity check on the cleaned text
        String plainText = Jsoup.parse(cleanHtml).text();
        if (containsProfanity(plainText)) {
            throw new InvalidContentException("Text content contains inappropriate language");
        }

        // 5. Logical Integrity check
        if (hasText(request.getContentUrl())) {
            throw new InvalidContentException("TEXT content must not have contentUrl");
        }

        // 6. UPDATE REQUEST (Crucial!)
        // By setting the clean version back into the request,
        // your Service will save the safe HTML to the database.
        request.setTextContent(cleanHtml);
    }
}