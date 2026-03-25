package com.course.dto;

import com.course.enums.LevelType;

import java.time.LocalDateTime;

public class NoteSummaryHighlightResponse {

    private Long noteId;
    private String noteText;
    private LevelType noteLevelType;
    private Long noteLevelId;
    private String sourceTitle;
    private String sourcePath;
    private String courseTitle;
    private String parentTitle;
    private String topicTitle;
    private String contentTitle;
    private Integer videoTimestampSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public LevelType getNoteLevelType() {
        return noteLevelType;
    }

    public void setNoteLevelType(LevelType noteLevelType) {
        this.noteLevelType = noteLevelType;
    }

    public Long getNoteLevelId() {
        return noteLevelId;
    }

    public void setNoteLevelId(Long noteLevelId) {
        this.noteLevelId = noteLevelId;
    }

    public String getSourceTitle() {
        return sourceTitle;
    }

    public void setSourceTitle(String sourceTitle) {
        this.sourceTitle = sourceTitle;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getParentTitle() {
        return parentTitle;
    }

    public void setParentTitle(String parentTitle) {
        this.parentTitle = parentTitle;
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String topicTitle) {
        this.topicTitle = topicTitle;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public Integer getVideoTimestampSeconds() {
        return videoTimestampSeconds;
    }

    public void setVideoTimestampSeconds(Integer videoTimestampSeconds) {
        this.videoTimestampSeconds = videoTimestampSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
