package com.course.dto;

import com.course.enums.LevelType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoteSummaryResponse {

    private Long userId;
    private LevelType levelType;
    private Long levelId;
    private String title;
    private String summaryText;
    private Integer noteCount;
    private Integer directNoteCount;
    private Integer descendantNoteCount;
    private Integer parentCount;
    private Integer topicCount;
    private Integer contentCount;
    private LocalDateTime latestNoteAt;
    private List<NoteSummaryHighlightResponse> highlights = new ArrayList<>();

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LevelType getLevelType() {
        return levelType;
    }

    public void setLevelType(LevelType levelType) {
        this.levelType = levelType;
    }

    public Long getLevelId() {
        return levelId;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public Integer getNoteCount() {
        return noteCount;
    }

    public void setNoteCount(Integer noteCount) {
        this.noteCount = noteCount;
    }

    public Integer getDirectNoteCount() {
        return directNoteCount;
    }

    public void setDirectNoteCount(Integer directNoteCount) {
        this.directNoteCount = directNoteCount;
    }

    public Integer getDescendantNoteCount() {
        return descendantNoteCount;
    }

    public void setDescendantNoteCount(Integer descendantNoteCount) {
        this.descendantNoteCount = descendantNoteCount;
    }

    public Integer getParentCount() {
        return parentCount;
    }

    public void setParentCount(Integer parentCount) {
        this.parentCount = parentCount;
    }

    public Integer getTopicCount() {
        return topicCount;
    }

    public void setTopicCount(Integer topicCount) {
        this.topicCount = topicCount;
    }

    public Integer getContentCount() {
        return contentCount;
    }

    public void setContentCount(Integer contentCount) {
        this.contentCount = contentCount;
    }

    public LocalDateTime getLatestNoteAt() {
        return latestNoteAt;
    }

    public void setLatestNoteAt(LocalDateTime latestNoteAt) {
        this.latestNoteAt = latestNoteAt;
    }

    public List<NoteSummaryHighlightResponse> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<NoteSummaryHighlightResponse> highlights) {
        this.highlights = highlights;
    }
}
