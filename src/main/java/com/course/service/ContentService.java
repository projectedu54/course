package com.course.service;

import com.course.dto.*;
import com.course.entity.Content;
import com.course.entity.Topic;
import com.course.entity.UserHighlight;
import com.course.enums.ContentType;
import com.course.exception.customException.InvalidContentException;
import com.course.exception.customException.ResourceNotFoundException;
import com.course.repository.ContentRepository;
import com.course.repository.HighlightRepository;
import com.course.repository.TopicRepository;
import com.course.util.ContentValidationUtil;
import com.course.validation.ContentValidatorFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final TopicRepository topicRepository;
    private final ContentValidatorFactory validatorFactory;
    private static final int MAX_TITLE_LENGTH = 150;
    private final HighlightRepository highlightRepository;
    private final S3StorageService s3StorageService;

    public ContentService(ContentRepository contentRepository,
                          TopicRepository topicRepository,
                          ContentValidatorFactory validatorFactory,
                          HighlightRepository highlightRepository,
                          S3StorageService s3StorageService) {
        this.contentRepository = contentRepository;
        this.topicRepository = topicRepository;
        this.validatorFactory = validatorFactory;
        this.highlightRepository = highlightRepository;
        this.s3StorageService = s3StorageService;
    }

    // ================= CREATE (JSON / URL-based) =================
    public ContentResponse createContent(Long topicId, ContentRequest request) {
        validateContentByType(request);

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        if (contentRepository.existsByTopicIdAndTitle(topicId, request.getTitle())) {
            throw new InvalidContentException(
                    "Content with title '" + request.getTitle() + "' already exists in this topic"
            );
        }

        Content content = new Content();
        content.setTitle(request.getTitle());
        content.setDescription(request.getDescription());
        content.setContentType(request.getContentType());

        if ("TEXT".equalsIgnoreCase(request.getContentType().toString())) {
            content.setTextContent(ContentValidationUtil.sanitize(request.getTextContent()));
            content.setContentUrl(null);
        } else {
            content.setContentUrl(request.getContentUrl());
            content.setTextContent(null);
        }
        content.setTopic(topic);

        Integer maxOrder = contentRepository.findMaxDisplayOrderByTopic(topicId);
        content.setDisplayOrder((maxOrder == null ? 0 : maxOrder) + 1);
        content.setCreatedAt(LocalDateTime.now());
        content.setUpdatedAt(null);

        Content saved = contentRepository.save(content);
        return mapToResponse(saved);
    }

    // ================= CREATE WITH FILE UPLOAD (Multipart) =================
    @Transactional
    public ContentResponse createContentWithFile(Long topicId, String title, String description,
                                                 String contentTypeStr, MultipartFile file, String textContent) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        if (contentRepository.existsByTopicIdAndTitle(topicId, title)) {
            throw new InvalidContentException("Content with title '" + title + "' already exists in this topic");
        }

        Content content = new Content();
        content.setTitle(title);
        content.setDescription(description);

        ContentType contentType = ContentType.valueOf(contentTypeStr.toUpperCase());
        content.setContentType(contentType);

        if ("TEXT".equalsIgnoreCase(contentTypeStr)) {
            content.setTextContent(ContentValidationUtil.sanitize(textContent));
            content.setContentUrl(null);
        } else {
            if (file == null || file.isEmpty()) {
                throw new InvalidContentException("File is required for content type: " + contentTypeStr);
            }

            // 1. Resolve Course ID and Unit Context from Topic Hierarchy
            Long courseId = null;
            Long unitId = null;
            String unitType = "NONE";

            if (topic.getModule() != null) {
                courseId = topic.getModule().getCourse().getId();
                unitId = topic.getModule().getId();
                unitType = "MODULE";
            } else if (topic.getChapter() != null) {
                courseId = topic.getChapter().getCourse().getId();
                unitId = topic.getChapter().getId();
                unitType = "CHAPTER";
            } else if (topic.getSection() != null) {
                courseId = topic.getSection().getCourse().getId();
                unitId = topic.getSection().getId();
                unitType = "SECTION";
            }

            // 2. Generate Structured S3 Key: course_id/unit_type_id/topic_id/filename
            String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String s3FileKey = generateS3Key(courseId, unitType, unitId, topicId, originalFileName);

            // 3. Upload file using the structured key path reference
            s3StorageService.uploadFile(file, s3FileKey); // Calls the overloaded method taking both file and custom key

            content.setContentUrl(s3FileKey);
            content.setTextContent(null);
        }

        content.setTopic(topic);

        Integer maxOrder = contentRepository.findMaxDisplayOrderByTopic(topicId);
        content.setDisplayOrder((maxOrder == null ? 0 : maxOrder) + 1);
        content.setCreatedAt(LocalDateTime.now());

        Content saved = contentRepository.save(content);
        return mapToResponse(saved);
    }

    // ================= S3 HIERARCHICAL KEY BUILDER =================
    /**
     * Generates a structured S3 file key following the format:
     * courses/{courseId}/{unit_folder}/{unitId}/topics/{topicId}/{timestamp}_{filename}
     */
    private String generateS3Key(Long courseId, String unitType, Long unitId, Long topicId, String originalFileName) {
        String cleanFileName = System.currentTimeMillis() + "_" + originalFileName.replaceAll("\\s+", "_");

        if (courseId == null) {
            courseId = 0L; // Fallback if course mapping is direct
        }

        if (unitType == null || unitType.equalsIgnoreCase("NONE") || unitId == null) {
            return String.format("courses/%d/topics/%d/%s", courseId, topicId, cleanFileName);
        }

        String unitFolder = switch (unitType.toUpperCase()) {
            case "MODULE" -> "modules";
            case "CHAPTER" -> "chapters";
            case "SECTION" -> "sections";
            default -> "units";
        };

        return String.format("courses/%d/%s/%d/topics/%d/%s", courseId, unitFolder, unitId, topicId, cleanFileName);
    }

    // ================= GET ALL =================
    public List<ContentResponse> getContentsByTopic(Long topicId) {
        topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        return contentRepository.findByTopicIdOrderByDisplayOrderAsc(topicId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================= GET BY ID =================
    public ContentResponse getContentById(Long topicId, Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        validateTopic(content, topicId);

        return mapToResponse(content);
    }

    // ================= UPDATE =================
    public ContentResponse updateContent(Long topicId, Long contentId, ContentRequest request) {
        validateContentByType(request);

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        if (!content.getTopic().getId().equals(topicId)) {
            throw new ResourceNotFoundException("Content does not belong to the topic");
        }

        if (contentRepository.existsByTopicIdAndTitleAndIdNot(topicId, request.getTitle(), contentId)) {
            throw new InvalidContentException(
                    "Another content with title '" + request.getTitle() + "' already exists in this topic"
            );
        }

        content.setTitle(request.getTitle());
        content.setDescription(request.getDescription());
        content.setContentType(request.getContentType());
        content.setContentUrl(request.getContentUrl());
        content.setTextContent(request.getTextContent());
        content.setUpdatedAt(LocalDateTime.now());

        Content updated = contentRepository.save(content);
        return mapToResponse(updated);
    }

    // ================= DELETE =================
    public void deleteContent(Long topicId, Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        validateTopic(content, topicId);
        contentRepository.delete(content);
    }

    // ================= REORDER (DRAG & DROP) =================
    @Transactional
    public void reorderContents(Long topicId, ContentReorderRequest request) {
        List<Content> contents = contentRepository.findByTopicId(topicId);

        if (contents.isEmpty()) {
            throw new ResourceNotFoundException("No contents found for topic");
        }

        if (contents.size() != request.getOrderedContentIds().size()) {
            throw new IllegalArgumentException(
                    "Reorder list size does not match existing contents"
            );
        }

        Map<Long, Content> contentMap = contents.stream()
                .collect(Collectors.toMap(Content::getId, c -> c));

        int order = 1;
        for (Long contentId : request.getOrderedContentIds()) {
            Content content = contentMap.get(contentId);
            if (content == null) {
                throw new IllegalArgumentException(
                        "Invalid content id for this topic: " + contentId
                );
            }
            content.setDisplayOrder(order++);
            content.setUpdatedAt(LocalDateTime.now());
        }

        contentRepository.saveAll(contents);
    }

    // ================= HELPERS =================
    private void validateTopic(Content content, Long topicId) {
        if (!content.getTopic().getId().equals(topicId)) {
            throw new ResourceNotFoundException("Content does not belong to the topic");
        }
    }

    private ContentResponse mapToResponse(Content c) {
        return new ContentResponse(
                c.getId(),
                c.getTitle(),
                c.getContentType(),
                c.getContentUrl(),
                c.getTextContent(),
                c.getDisplayOrder(),
                c.getTopic().getId(),
                c.getDescription()
        );
    }

    // ================= VALIDATION =================
    private void validateContentByType(ContentRequest request) {
        if (request == null) {
            throw new InvalidContentException("Content request cannot be null");
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new InvalidContentException("Title is required");
        }

        if (request.getTitle().length() > MAX_TITLE_LENGTH) {
            throw new InvalidContentException(
                    "Title cannot exceed " + MAX_TITLE_LENGTH + " characters"
            );
        }

        if (request.getContentType() == null) {
            throw new InvalidContentException("Content type is required");
        }

        validatorFactory.getValidator(request.getContentType()).validate(request);
    }

    @Transactional
    public HighlightResponse saveHighlight(Long userId, Long contentId, HighlightRequest request) {
        if (!contentRepository.existsById(contentId)) {
            throw new ResourceNotFoundException("Content not found");
        }

        List<UserHighlight> existing = highlightRepository.findByUserIdAndContentId(userId, contentId);
        UserHighlight highlightToSave = null;

        for (UserHighlight h : existing) {
            if (h.getSelectionCoords().equals(request.getSelectionData())) {
                highlightToSave = h;
                break;
            }
        }

        if (highlightToSave == null) {
            highlightToSave = new UserHighlight();
            highlightToSave.setUserId(userId);
            highlightToSave.setContentId(contentId);
            highlightToSave.setCreatedAt(LocalDateTime.now());
        }

        highlightToSave.setSelectionCoords(request.getSelectionData());
        highlightToSave.setHighlightedText(request.getHighlightedText());
        highlightToSave.setColor(request.getColor());

        UserHighlight saved = highlightRepository.save(highlightToSave);

        return new HighlightResponse(
                saved.getId(),
                saved.getSelectionCoords(),
                saved.getHighlightedText(),
                saved.getColor()
        );
    }

    public List<HighlightResponse> getHighlights(Long userId, Long contentId) {
        return highlightRepository.findByUserIdAndContentId(userId, contentId)
                .stream()
                .map(h -> new HighlightResponse(h.getId(), h.getSelectionCoords(), h.getHighlightedText(), h.getColor()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteHighlight(Long userId, Long highlightId) {
        UserHighlight highlight = highlightRepository.findById(highlightId)
                .orElseThrow(() -> new ResourceNotFoundException("Highlight not found"));

        if (!highlight.getUserId().equals(userId)) {
            throw new InvalidContentException("You do not have permission to delete this highlight");
        }

        highlightRepository.delete(highlight);
    }
}