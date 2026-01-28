package com.course.controller;

import com.course.dto.*;
import com.course.service.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/chapters")
@Tag(
        name = "Chapter APIs",
        description = "APIs for managing chapters inside a course"
)
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @Operation(
            summary = "Add chapter to course",
            description = "Creates a new chapter under a course",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Chapter created successfully",
                            content = @Content(schema = @Schema(implementation = ChapterActionResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Course not found"),
                    @ApiResponse(responseCode = "400", description = "Validation error")
            }
    )
    @PostMapping
    public ResponseEntity<ChapterActionResponse> createChapter(
            @Parameter(description = "Course ID", example = "1")
            @PathVariable Long courseId,
            @Valid @RequestBody ChapterRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chapterService.createChapter(courseId, request));
    }

    @Operation(
            summary = "Get all chapters of a course by course id",
            description = "Returns all chapters belonging to a course",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Chapters fetched successfully",
                            content = @Content(schema = @Schema(implementation = ChapterResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Course not found")
            }
    )
    @GetMapping
    public ResponseEntity<List<ChapterResponse>> getChapters(
            @Parameter(description = "Course ID", example = "1")
            @PathVariable Long courseId) {

        return ResponseEntity.ok(chapterService.getChaptersByCourse(courseId));
    }

    @Operation(
            summary = "Get chapter by ID",
            description = "Fetch a specific chapter by its ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Chapter fetched successfully",
                            content = @Content(schema = @Schema(implementation = ChapterResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Chapter not found")
            }
    )
    @GetMapping("/{chapterId}")
    public ResponseEntity<ChapterResponse> getChapterById(
            @Parameter(description = "Course ID", example = "1")
            @PathVariable Long courseId,
            @Parameter(description = "Chapter ID", example = "10")
            @PathVariable Long chapterId) {

        return ResponseEntity.ok(
                chapterService.getChapterById(courseId, chapterId)
        );
    }

    // ================= UPDATE =================
    @Operation(
            summary = "Update chapter",
            description = "Updates chapter title or display order",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Chapter updated successfully",
                            content = @Content(schema = @Schema(implementation = ChapterActionResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Chapter not found")
            }
    )
    @PutMapping("/{chapterId}")
    public ResponseEntity<ChapterActionResponse> updateChapter(
            @Parameter(description = "Course ID", example = "1")
            @PathVariable Long courseId,
            @Parameter(description = "Chapter ID", example = "10")
            @PathVariable Long chapterId,
            @Valid @RequestBody ChapterRequest request) {

        return ResponseEntity.ok(
                chapterService.updateChapter(courseId, chapterId, request)
        );
    }

    // ================= DELETE =================
    @Operation(
            summary = "Delete chapter",
            description = "Deletes a chapter from a course",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Chapter deleted successfully",
                            content = @Content(schema = @Schema(implementation = ChapterActionResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Chapter not found")
            }
    )
    @DeleteMapping("/{chapterId}")
    public ResponseEntity<ChapterActionResponse> deleteChapter(
            @Parameter(description = "Chapter ID", example = "10")
            @PathVariable Long chapterId) {

        return ResponseEntity.ok(
                chapterService.deleteChapter(chapterId)
        );
    }
}
