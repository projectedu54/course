package com.course.controller;

import com.course.dto.CourseMetadataDTO;
import com.course.dto.CourseRequest;
import com.course.dto.SyllabusDTO;
import com.course.entity.Course;
import com.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Course API", description = "Operations related to Course Management")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // =============================
    // CREATE COURSE
    // =============================
    @PostMapping
    @Operation(summary = "Create a new course")
    public ResponseEntity<Course> createCourse(
            @RequestHeader("X-USER-ID") Long userId,
            @Valid @RequestBody CourseRequest request) {

        Course course = courseService.createCourse(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }

    // =============================
    // GET COURSE BY ID
    // =============================
    @GetMapping("/{id}")
    @Operation(summary = "Get course by id")
    public ResponseEntity<Course> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    // =============================
    // GET ALL COURSES
    // =============================
    @GetMapping
    @Operation(summary = "Get all course")
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // =============================
    // UPDATE COURSE
    // =============================
    @PutMapping("/{id}")
    @Operation(summary = "Update course")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody CourseRequest request) {

        return ResponseEntity.ok(
                courseService.updateCourse(id, request, userId)
        );
    }

    // =============================
    // DELETE COURSE
    // =============================
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete course using course id and user id")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long id,
            @RequestHeader("X-USER-ID") Long userId) {

        courseService.deleteCourse(id, userId);
        return ResponseEntity.noContent().build();
    }

    // =============================
    // PUBLISH COURSE
    // =============================
    @PatchMapping("/{id}/publish")
    @Operation(summary = "Publish course", description = "Performs ownership, catalog, and hierarchy validation before making course live.")
    public ResponseEntity<Course> publishCourse(
            @PathVariable Long id,
            @RequestHeader("X-USER-ID") Long userId) {

        Course publishedCourse = courseService.publishCourse(id, userId);
        return ResponseEntity.ok(publishedCourse);
    }


// BATCH METADATA
    @PostMapping("/bulk-metadata")
    @Operation(summary = "Get metadata for multiple courses", description = "Optimized for Enrollment dashboard")
    public ResponseEntity<List<CourseMetadataDTO>> getBulkMetadata(@RequestBody Set<Long> ids) {
        List<CourseMetadataDTO> metadata = courseService.getMetadataByIds(ids);
        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/{id}/syllabus")
    @Operation(summary = "Get full course hierarchy")
    public ResponseEntity<SyllabusDTO> getSyllabus(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getSyllabus(id));
    }
}