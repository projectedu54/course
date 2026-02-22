package com.course.controller;

import com.course.api.ApiResponse;
import com.course.dto.CourseResponse;
import com.course.mapper.CourseMapper;
import com.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/public/courses")
@Tag(name = "Public Course API")
public class PublicCourseController {

    private final CourseService courseService;

    public PublicCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @Operation(
            summary = "Search published courses",
            description = "Search published courses by title, description, tags with pagination & sorting"
    )
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Set<String> tags,
            Pageable pageable
    ) {
        Page<CourseResponse> result =
                courseService.searchPublishedCourses(keyword, tags, pageable)
                        .map(CourseMapper::toResponse);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Courses fetched successfully", result)
        );
    }
}