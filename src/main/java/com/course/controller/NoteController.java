package com.course.controller;

import com.course.api.ApiResponse;
import com.course.dto.NoteSummaryResponse;
import com.course.entity.Note;
import com.course.entity.NoteRequest;
import com.course.enums.LevelType;
import com.course.service.NoteService;
import com.course.service.NoteSummaryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "Notes API", description = "APIs for managing user notes")
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService service;
    private final NoteSummaryService noteSummaryService;

    public NoteController(NoteService service, NoteSummaryService noteSummaryService) {
        this.service = service;
        this.noteSummaryService = noteSummaryService;
    }

    @Operation(summary = "Create a new note", description = "Creates a new note for a user at any learning level")
    @PostMapping
    public ResponseEntity<ApiResponse<Note>> create(
            @RequestHeader("X-USER-ID") Long authenticatedUserId,
            @Valid @RequestBody
            @Parameter(description = "Note details", required = true) NoteRequest request) {
        Note createdNote = service.create(request, authenticatedUserId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Note created successfully", createdNote));
    }

    @Operation(summary = "Get all notes for a user", description = "Retrieves all notes created by a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Note>>> getUserNotes(
            @RequestHeader("X-USER-ID") Long authenticatedUserId,
            @Parameter(description = "ID of the user", required = true)
            @PathVariable Long userId) {
        List<Note> notes = service.getUserNotes(userId, authenticatedUserId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notes retrieved successfully", notes));
    }

    @Operation(summary = "Get notes by level", description = "Retrieves all notes for a specific learning level")
    @GetMapping("/level")
    public ResponseEntity<ApiResponse<List<Note>>> getNotesByLevel(
            @RequestHeader("X-USER-ID") Long authenticatedUserId,
            @Parameter(description = "ID of the user", required = true) @RequestParam Long userId,
            @Parameter(description = "Type of learning level", required = true) @RequestParam LevelType levelType,
            @Parameter(description = "ID of the level entity", required = true) @RequestParam Long levelId) {
        List<Note> notes = service.getNotesByLevel(userId, levelType, levelId, authenticatedUserId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notes retrieved for level: " + levelType, notes));
    }

    @Operation(summary = "Get hierarchical notes summary", description = "Builds a roll-up summary for a user's notes at course, parent, topic, or content level")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<NoteSummaryResponse>> getSummary(
            @RequestHeader("X-USER-ID") Long authenticatedUserId,
            @Parameter(description = "ID of the user", required = true) @RequestParam Long userId,
            @Parameter(description = "Type of learning level", required = true) @RequestParam LevelType levelType,
            @Parameter(description = "ID of the level entity", required = true) @RequestParam Long levelId) {
        NoteSummaryResponse summary = noteSummaryService.getSummary(userId, levelType, levelId, authenticatedUserId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notes summary retrieved successfully", summary));
    }

    @Operation(summary = "Update a note", description = "Updates the text content of an existing note")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Note>> update(
            @RequestHeader("X-USER-ID") Long authenticatedUserId,
            @Parameter(description = "ID of the note to update", required = true)
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request) {
        Note updatedNote = service.update(id, request, authenticatedUserId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Note updated successfully", updatedNote));
    }

    @Operation(summary = "Delete a note", description = "Deletes a note by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-USER-ID") Long authenticatedUserId,
            @Parameter(description = "ID of the note to delete", required = true)
            @PathVariable Long id) {
        service.delete(id, authenticatedUserId);
        return ResponseEntity.noContent().build();
    }
}
