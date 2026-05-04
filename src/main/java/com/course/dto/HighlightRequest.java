package com.course.dto;

// HighlightRequest.java
public class HighlightRequest {
    private String selectionData;
    private String highlightedText;
    private String color;

    public String getSelectionData() { return selectionData; }
    public void setSelectionData(String selectionData) { this.selectionData = selectionData; }
    public String getHighlightedText() { return highlightedText; }
    public void setHighlightedText(String highlightedText) { this.highlightedText = highlightedText; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}

