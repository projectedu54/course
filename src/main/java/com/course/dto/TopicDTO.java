package com.course.dto;
import java.util.List;

public record TopicDTO(Long id, String title, List<ContentDTO> contents) {}