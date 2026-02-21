package com.course.controller;

import com.course.dto.ChapterActionResponse;
import com.course.dto.ChapterRequest;
import com.course.dto.ChapterResponse;
import com.course.service.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/chapters")
@Tag(name = "Chapter API", description = "Operations for managing chapters of a course")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @PostMapping
    @Operation(summary = "Add a new chapter", description = "Creates a chapter. displayOrder is auto-incremented by backend.")
    public ResponseEntity<ChapterActionResponse> create(
            @PathVariable Long courseId,
            @Valid @RequestBody ChapterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chapterService.createChapter(courseId, request));
    }

    @GetMapping
    @Operation(summary = "Get all chapters for a course", description = "Returns all chapters with auto-assigned displayOrder.")
    public ResponseEntity<List<ChapterResponse>> getAll(@PathVariable Long courseId) {
        return ResponseEntity.ok(chapterService.getChaptersByCourse(courseId));
    }

    @GetMapping("/{chapterId}")
    @Operation(summary = "Get a chapter by ID", description = "Retrieve a single chapter by its ID.")
    public ResponseEntity<ChapterResponse> getById(
            @PathVariable Long courseId,
            @PathVariable Long chapterId) {
        return ResponseEntity.ok(chapterService.getChapterById(courseId, chapterId));
    }

    @PutMapping("/{chapterId}")
    @Operation(summary = "Update a chapter", description = "Updates a chapter title. displayOrder is not updated manually.")
    public ResponseEntity<ChapterActionResponse> update(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @Valid @RequestBody ChapterRequest request) {
        return ResponseEntity.ok(chapterService.updateChapter(courseId, chapterId, request));
    }

    @DeleteMapping("/{chapterId}")
    @Operation(summary = "Delete a chapter", description = "Deletes a chapter by its ID.")
    public ResponseEntity<ChapterActionResponse> delete(@PathVariable Long chapterId) {
        return ResponseEntity.ok(chapterService.deleteChapter(chapterId));
    }
}
