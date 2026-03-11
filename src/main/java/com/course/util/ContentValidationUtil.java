package com.course.util;

import com.course.exception.customException.InvalidContentException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.util.List;

public final class ContentValidationUtil {

    private ContentValidationUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static void validateUrl(String url, String type) {
        if (!hasText(url)) {
            throw new InvalidContentException(type + " content requires contentUrl");
        }

        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("www.")) {
            throw new InvalidContentException("Invalid URL format");
        }
    }

    public static void validateAudioFileExtension(String url) {
        if (!url.matches("(?i).*\\.(mp3|wav|ogg)$")) {
            throw new InvalidContentException("Only mp3, wav, or ogg audio files are allowed");
        }
    }

    public static void validateImageFileExtension(String url) {
        if (!url.matches("(?i).*\\.(jpg|jpeg|png|webp)$")) {
            throw new InvalidContentException("Only jpg, jpeg, png, or webp image files are allowed");
        }
    }

    public static void validateVideoFileExtension(String url) {
        if (!url.matches("(?i).*\\.(mp4|avi|mov|mkv|webm)$")) {
            throw new InvalidContentException("Only mp4, avi, mov, mkv, or webm video files are allowed");
        }
    }

    private static final List<String> BANNED_WORDS = List.of("badword1", "badword2", "abuse");

    public static boolean containsProfanity(String text) {
        if (!hasText(text)) return false;
        String lower = text.toLowerCase();
        return BANNED_WORDS.stream().anyMatch(lower::contains);
    }

    public static Safelist getCustomSafelist() {
        return Safelist.relaxed()
                .addTags("pre", "code", "span", "hr", "br", "iframe", "u")
                .addAttributes(":all", "style", "class")
                .addAttributes("iframe", "src", "width", "height", "frameborder", "allowfullscreen")
                // Added common YouTube/Vimeo variations
                .addProtocols("iframe", "src", "https://www.youtube.com", "https://youtube.com", "https://www.youtube-nocookie.com", "https://player.vimeo.com")
                .addAttributes("a", "href", "target", "title")
                .addAttributes("img", "src", "alt", "title", "width", "height")
                .addProtocols("img", "src", "http", "https", "data");
    }

    public static String sanitize(String rawHtml) {
        if (rawHtml == null) return null;
        // prettyPrint(false) prevents Jsoup from adding unwanted newlines in <pre> tags
        Document.OutputSettings settings = new Document.OutputSettings().prettyPrint(false);
        return Jsoup.clean(rawHtml, "", getCustomSafelist(), settings);
    }
}