package com.course.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_highlights_tbl")
public class UserHighlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "selection_coords", columnDefinition = "TEXT", nullable = false)
    private String selectionCoords;

    @Column(name = "highlighted_text", columnDefinition = "TEXT")
    private String highlightedText;

    @Column(name = "color")
    private String color;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public UserHighlight() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getContentId() { return contentId; }
    public String getSelectionCoords() { return selectionCoords; }
    public String getHighlightedText() { return highlightedText; }
    public String getColor() { return color; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public void setSelectionCoords(String selectionCoords) { this.selectionCoords = selectionCoords; }
    public void setHighlightedText(String highlightedText) { this.highlightedText = highlightedText; }
    public void setColor(String color) { this.color = color; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}