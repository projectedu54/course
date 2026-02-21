package com.course.service;

import com.course.dto.ContentReorderRequest;
import com.course.dto.ContentRequest;
import com.course.dto.ContentResponse;
import com.course.entity.Content;
import com.course.entity.Topic;
import com.course.exception.ResourceNotFoundException;
import com.course.exception.customException.InvalidContentException;
import com.course.repository.ContentRepository;
import com.course.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.course.entity.ContentType.IMAGE;

@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final TopicRepository topicRepository;

    public ContentService(ContentRepository contentRepository, TopicRepository topicRepository) {
        this.contentRepository = contentRepository;
        this.topicRepository = topicRepository;
    }

    // ================= CREATE =================
    public ContentResponse createContent(Long topicId, ContentRequest request) {

        validateContentByType(request);   //

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        Content content = new Content();
        content.setTitle(request.getTitle());
        content.setContentType(request.getContentType());
        content.setContentUrl(request.getContentUrl());
        content.setTextContent(request.getTextContent());
        content.setTopic(topic);

        Integer maxOrder = contentRepository.findMaxDisplayOrderByTopic(topicId);
        content.setDisplayOrder((maxOrder == null ? 0 : maxOrder) + 1);

        content.setCreatedAt(LocalDateTime.now());
        content.setUpdatedAt(LocalDateTime.now());

        Content saved = contentRepository.save(content);
        return mapToResponse(saved);
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

        validateContentByType(request);   //

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        if (!content.getTopic().getId().equals(topicId)) {
            throw new ResourceNotFoundException("Content does not belong to the topic");
        }

        content.setTitle(request.getTitle());
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
                c.getTopic().getId()
        );
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


    // TODO : validate text content like abusive, not good, bad word nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
    private void validateContentByType(ContentRequest request) {

        switch (request.getContentType()) {

//
            case AUDIO:
                if (request.getContentUrl() == null || request.getContentUrl().isBlank()) {
                    throw new InvalidContentException(
                            request.getContentType() + " content requires contentUrl"
                    );
                }
                if (request.getTextContent() != null && !request.getTextContent().isBlank()) {
                    throw new InvalidContentException(
                            request.getContentType() + " content must not have textContent"
                    );
                }
                break;

            case TEXT:
                if (request.getTextContent() == null || request.getTextContent().isBlank()) {
                    throw new InvalidContentException("TEXT content requires textContent");
                }
                if (request.getContentUrl() != null && !request.getContentUrl().isBlank()) {
                    throw new InvalidContentException("TEXT content must not have contentUrl");
                }
                break;

            case QUIZ:
                throw new InvalidContentException(
                        "QUIZ content must be created via Quiz API"
                );

            default:
                throw new InvalidContentException("Unsupported content type");
        }
    }


}
