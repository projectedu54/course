package com.course.controller;

import com.course.api.ApiResponse;
import com.course.entity.Note;
import com.course.entity.NoteRequest;
import com.course.enums.LevelType;
import com.course.service.NoteService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private NoteService service;

    @Operation(summary = "Create a new note", description = "Creates a new note for a user at any learning level")
    @PostMapping
    public ResponseEntity<ApiResponse<Note>> create(
            @RequestBody
            @Parameter(description = "Note details", required = true) NoteRequest request) {
        Note createdNote = service.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Note created successfully", createdNote));
    }

    @Operation(summary = "Get all notes for a user", description = "Retrieves all notes created by a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Note>>> getUserNotes(
            @Parameter(description = "ID of the user", required = true)
            @PathVariable Long userId) {
        List<Note> notes = service.getUserNotes(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notes retrieved successfully", notes));
    }

    @Operation(summary = "Get notes by level", description = "Retrieves all notes for a specific learning level")
    @GetMapping("/level")
    public ResponseEntity<ApiResponse<List<Note>>> getNotesByLevel(
            @Parameter(description = "ID of the user", required = true) @RequestParam Long userId,
            @Parameter(description = "Type of learning level", required = true) @RequestParam LevelType levelType,
            @Parameter(description = "ID of the level entity", required = true) @RequestParam Long levelId) {
        List<Note> notes = service.getNotesByLevel(userId, levelType, levelId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notes retrieved for level: " + levelType, notes));
    }

    @Operation(summary = "Update a note", description = "Updates the text content of an existing note")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Note>> update(
            @Parameter(description = "ID of the note to update", required = true)
            @PathVariable Long id,
            @RequestBody NoteRequest request) {
        Note updatedNote = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Note updated successfully", updatedNote));
    }

    @Operation(summary = "Delete a note", description = "Deletes a note by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID of the note to delete", required = true)
            @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Note with ID " + id + " has been deleted successfully", null));
    }
}