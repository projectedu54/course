package com.course.controller;

import com.course.dto.TopicActionResponse;
import com.course.dto.TopicRequest;
import com.course.dto.TopicResponse;
import com.course.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/topics")
@Tag(name = "Topic API", description = "Operations for managing topics under Course, Module, Chapter, or Section")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @PostMapping("/{parentType}/{parentId}")
    @Operation(summary = "Add a new topic",
            description = "Create a topic under a Course, Module, Chapter, or Section. displayOrder is auto-incremented.")
    public ResponseEntity<TopicActionResponse> create(
            @PathVariable String parentType,
            @PathVariable Long parentId,
            @Valid @RequestBody TopicRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(topicService.createTopic(parentId, parentType, request));
    }

    @GetMapping("/{parentType}/{parentId}")
    @Operation(summary = "Get all topics for a parent",
            description = "Get all topics under a given parent type (Course, Module, Chapter, Section).")
    public ResponseEntity<List<TopicResponse>> getAll(
            @PathVariable String parentType,
            @PathVariable Long parentId) {

        return ResponseEntity.ok(topicService.getTopics(parentType, parentId));
    }

    @GetMapping("/{parentType}/{parentId}/{topicId}")
    @Operation(summary = "Get topic by ID",
            description = "Retrieve a single topic by its ID under a parent.")
    public ResponseEntity<TopicResponse> getById(
            @PathVariable String parentType,
            @PathVariable Long parentId,
            @PathVariable Long topicId) {

        return ResponseEntity.ok(topicService.getTopicById(parentId, parentType, topicId));
    }

    @PutMapping("/{parentType}/{parentId}/{topicId}")
    @Operation(summary = "Update a topic",
            description = "Update the topic title. displayOrder is auto-managed.")
    public ResponseEntity<TopicActionResponse> update(
            @PathVariable String parentType,
            @PathVariable Long parentId,
            @PathVariable Long topicId,
            @Valid @RequestBody TopicRequest request) {

        return ResponseEntity.ok(topicService.updateTopic(parentId, parentType, topicId, request));
    }

    @DeleteMapping("/{parentType}/{parentId}/{topicId}")
    @Operation(summary = "Delete a topic",
            description = "Deletes a topic by its ID under a parent.")
    public ResponseEntity<TopicActionResponse> delete(
            @PathVariable String parentType,
            @PathVariable Long parentId,
            @PathVariable Long topicId) {

        return ResponseEntity.ok(topicService.deleteTopic(parentId, parentType, topicId));
    }
}
