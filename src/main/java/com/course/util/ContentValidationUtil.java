package com.course.util;

import com.course.exception.customException.InvalidContentException;

import java.util.List;

public final class ContentValidationUtil {

    // Private constructor → prevents instantiation
    private ContentValidationUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static void validateUrl(String url, String type) {
        if (url == null || url.isBlank()) {
            throw new InvalidContentException(type + " content requires contentUrl");
        }

        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("www.")) {
            throw new InvalidContentException("Invalid URL format");
        }
    }

    public static void validateAudioFileExtension(String url) {
        if (!url.matches(".*\\.(mp3|wav|ogg)$")) {
            throw new InvalidContentException(
                    "Only mp3, wav, or ogg audio files are allowed"
            );
        }
    }

    public static void validateImageFileExtension(String url) {
        if (!url.matches(".*\\.(jpg|jpeg|png|webp)$")) {
            throw new InvalidContentException(
                    "Only jpg, jpeg, png, or webp image files are allowed"
            );
        }
    }

    private static final List<String> BANNED_WORDS = List.of(
            "badword1",
            "badword2",
            "abuse"
    );

    public static boolean containsProfanity(String text) {
        String lower = text.toLowerCase();
        return BANNED_WORDS.stream()
                .anyMatch(lower::contains);
    }
}