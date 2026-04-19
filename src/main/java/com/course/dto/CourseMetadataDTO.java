package com.course.dto;

public class CourseMetadataDTO {
    private Long id;
    private String title;
    private String thumbnailUrl;

    public CourseMetadataDTO(Long id, String title, String thumbnailUrl) {
        this.id = id;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getThumbnailUrl() { return thumbnailUrl; }
}