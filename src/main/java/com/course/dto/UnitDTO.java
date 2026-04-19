package com.course.dto;
import java.util.List;

public record UnitDTO(Long id, String title, List<TopicDTO> topics) {}

