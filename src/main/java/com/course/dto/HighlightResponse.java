package com.course.dto;

public class HighlightResponse {
    private Long id;
    private String selectionData;
    private String highlightedText;
    private String color;

    public HighlightResponse(Long id, String selectionData, String highlightedText, String color) {
        this.id = id;
        this.selectionData = selectionData;
        this.highlightedText = highlightedText;
        this.color = color;
    }

    public Long getId() { return id; }
    public String getSelectionData() { return selectionData; }
    public String getHighlightedText() { return highlightedText; }
    public String getColor() { return color; }
}