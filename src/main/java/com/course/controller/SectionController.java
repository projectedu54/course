package com.course.controller;

import com.course.dto.SectionActionResponse;
import com.course.dto.SectionRequest;
import com.course.dto.SectionResponse;
import com.course.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/sections")
@Tag(name = "Section API", description = "Operations for managing sections of a course")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @PostMapping
    @Operation(summary = "Add a new section", description = "Creates a section. displayOrder is auto-incremented by backend.")
    public ResponseEntity<SectionActionResponse> create(
            @PathVariable Long courseId,
            @Valid @RequestBody SectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sectionService.createSection(courseId, request));
    }

    @GetMapping
    @Operation(summary = "Get all sections for a course", description = "Returns all sections with auto-assigned displayOrder.")
    public ResponseEntity<List<SectionResponse>> getAll(@PathVariable Long courseId) {
        return ResponseEntity.ok(sectionService.getSectionsByCourse(courseId));
    }

    @GetMapping("/{sectionId}")
    @Operation(summary = "Get a section by ID", description = "Retrieve a single section by its ID.")
    public ResponseEntity<SectionResponse> getById(
            @PathVariable Long courseId,
            @PathVariable Long sectionId) {
        return ResponseEntity.ok(sectionService.getSectionById(courseId, sectionId));
    }

    @PutMapping("/{sectionId}")
    @Operation(summary = "Update a section", description = "Updates a section title. displayOrder is not updated manually.")
    public ResponseEntity<SectionActionResponse> update(
            @PathVariable Long courseId,
            @PathVariable Long sectionId,
            @Valid @RequestBody SectionRequest request) {
        return ResponseEntity.ok(sectionService.updateSection(courseId, sectionId, request));
    }

    @DeleteMapping("/{sectionId}")
    @Operation(summary = "Delete a section", description = "Deletes a section by its ID.")
    public ResponseEntity<SectionActionResponse> delete(@PathVariable Long sectionId) {
        return ResponseEntity.ok(sectionService.deleteSection(sectionId));
    }
}
