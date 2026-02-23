package com.course.controller;

import com.course.dto.ContentReorderRequest;
import com.course.dto.ContentRequest;
import com.course.dto.ContentResponse;
import com.course.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/topics/{topicId}/contents")
@Tag(name = "Content API", description = "Operations for managing contents of a topic")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    // ================= CREATE =================
    @PostMapping
    @Operation(summary = "Add new content to a topic",
            description = "Creates content under a topic. Display order is auto-incremented.")
    public ResponseEntity<ContentResponse> createContent(
            @PathVariable Long topicId,
            @Valid @RequestBody ContentRequest request) {

        ContentResponse response = contentService.createContent(topicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ================= GET ALL =================
    @GetMapping
    @Operation(summary = "Get all contents of a topic",
            description = "Returns all contents sorted by display order.")
    public ResponseEntity<List<ContentResponse>> getContents(@PathVariable Long topicId) {
        List<ContentResponse> contents = contentService.getContentsByTopic(topicId);
        return ResponseEntity.ok(contents);
    }

    // ================= GET BY ID =================
    @GetMapping("/{contentId}")
    @Operation(summary = "Get content by ID", description = "Retrieve a single content by its ID")
    public ResponseEntity<ContentResponse> getContentById(
            @PathVariable Long topicId,
            @PathVariable Long contentId) {

        ContentResponse content = contentService.getContentById(topicId, contentId);
        return ResponseEntity.ok(content);
    }

    // ================= UPDATE =================
    @PutMapping("/{contentId}")
    @Operation(summary = "Update content", description = "Updates content title, type, URL, text or description")
    public ResponseEntity<ContentResponse> updateContent(
            @PathVariable Long topicId,
            @PathVariable Long contentId,
            @Valid @RequestBody ContentRequest request) {

        ContentResponse updated = contentService.updateContent(topicId, contentId, request);
        return ResponseEntity.ok(updated);
    }

    // ================= DELETE =================
    @DeleteMapping("/{contentId}")
    @Operation(summary = "Delete content", description = "Deletes content by its ID")
    public ResponseEntity<Void> deleteContent(
            @PathVariable Long topicId,
            @PathVariable Long contentId) {

        contentService.deleteContent(topicId, contentId);
        return ResponseEntity.noContent().build();
    }

    // ================= REORDER =================
    @PutMapping("/reorder")
    @Operation(summary = "Reorder contents (Drag & Drop)",
            description = "Reorders contents based on UI drag-and-drop order")
    public ResponseEntity<Void> reorderContents(
            @PathVariable Long topicId,
            @Valid @RequestBody ContentReorderRequest request) {

        contentService.reorderContents(topicId, request);
        return ResponseEntity.ok().build();
    }
}