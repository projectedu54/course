package com.course.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ContentReorderRequest {

    @NotEmpty(message = "orderedContentIds cannot be empty")
    private List<Long> orderedContentIds;

    public List<Long> getOrderedContentIds() {
        return orderedContentIds;
    }

    public void setOrderedContentIds(List<Long> orderedContentIds) {
        this.orderedContentIds = orderedContentIds;
    }
}
