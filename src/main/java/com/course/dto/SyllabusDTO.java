package com.course.dto;
import java.util.List;

public record SyllabusDTO(
        Long courseId,
        String title,
        String courseStructure, // e.g., "MODULE", "CHAPTER", "SECTION"
        List<UnitDTO> units,
        List<TopicDTO> rootTopics
) {}