package com.course.mapper;

import com.course.dto.CourseResponse;
import com.course.entity.Course;

import java.util.stream.Collectors;

public class CourseMapper {

    public static CourseResponse toResponse(Course course) {
        CourseResponse dto = new CourseResponse();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setCourseType(course.getCourseType());
        dto.setStatus(course.getStatus());
        dto.setCatalogId(course.getCatalogId());
        dto.setCreatedAt(course.getCreatedAt());

        dto.setTags(
                course.getTags()
                        .stream()
                        .map(tag -> tag.getName())
                        .collect(Collectors.toSet())
        );

        return dto;
    }
}
