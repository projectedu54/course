package com.course.controller;

import com.course.dto.CourseRequest;
import com.course.entity.Course;
import com.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Course API", description = "Operations related to Course Management")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // Create course
    @PostMapping
    @Operation(
            summary = "Create a new course",
            description = "Creates a course with title, description, type, status, catalog, and tags"
    )
    public ResponseEntity<Course> createCourse(@Valid @RequestBody CourseRequest request) {
        Course course = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }

    // Get course by ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Get course by ID",
            description = "Retrieve a single course by its unique ID"
    )
    public ResponseEntity<Course> getCourse(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    // Get all courses
    @GetMapping
    @Operation(
            summary = "Get all courses",
            description = "Retrieve a list of all courses"
    )
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    // Update course
    @PutMapping("/{id}")
    @Operation(
            summary = "Update course",
            description = "Update course details by ID. Only fields provided in the request will be updated"
    )
    public ResponseEntity<Course> updateCourse(@PathVariable Long id,
                                               @RequestBody CourseRequest request) {
        Course updatedCourse = courseService.updateCourse(id, request);
        return ResponseEntity.ok(updatedCourse);
    }

    // Delete course
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete course",
            description = "Deletes a course by its ID"
    )
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }



}
