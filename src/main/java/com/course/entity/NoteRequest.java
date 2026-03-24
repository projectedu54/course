package com.course.entity;

import com.course.enums.LevelType;
import java.util.Map;

public class NoteRequest {

    private Long userId;
    private LevelType levelType;
    private Long levelId;
    private String noteText;

    private Map<String, Object> metadata;

    public NoteRequest() {}

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LevelType getLevelType() { return levelType; }
    public void setLevelType(LevelType levelType) { this.levelType = levelType; }

    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }

    public String getNoteText() { return noteText; }
    public void setNoteText(String noteText) { this.noteText = noteText; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}