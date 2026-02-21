package com.course.controller;

import com.course.dto.ModuleActionResponse;
import com.course.dto.ModuleRequest;
import com.course.dto.ModuleResponse;
import com.course.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/modules")
@Tag(name = "Module API", description = "Operations for managing modules of a course")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @PostMapping
    @Operation(summary = "Add a new module", description = "Creates a module. displayOrder is auto-incremented by backend.")
    public ResponseEntity<ModuleActionResponse> create(
            @PathVariable Long courseId,
            @Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(moduleService.createModule(courseId, request));
    }

    @GetMapping
    @Operation(summary = "Get all modules for a course", description = "Returns all modules with auto-assigned displayOrder.")
    public ResponseEntity<List<ModuleResponse>> getAll(@PathVariable Long courseId) {
        return ResponseEntity.ok(moduleService.getModulesByCourse(courseId));
    }

    @GetMapping("/{moduleId}")
    @Operation(summary = "Get a module by ID", description = "Retrieve a single module by its ID.")
    public ResponseEntity<ModuleResponse> getById(
            @PathVariable Long courseId,
            @PathVariable Long moduleId) {
        return ResponseEntity.ok(moduleService.getModuleById(courseId, moduleId));
    }

    @PutMapping("/{moduleId}")
    @Operation(summary = "Update a module", description = "Updates a module title. displayOrder is not updated manually.")
    public ResponseEntity<ModuleActionResponse> update(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.ok(moduleService.updateModule(courseId, moduleId, request));
    }

    @DeleteMapping("/{moduleId}")
    @Operation(summary = "Delete a module", description = "Deletes a module by its ID.")
    public ResponseEntity<ModuleActionResponse> delete(@PathVariable Long moduleId) {
        return ResponseEntity.ok(moduleService.deleteModule(moduleId));
    }
}
