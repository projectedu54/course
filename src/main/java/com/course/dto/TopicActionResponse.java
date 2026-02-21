package com.course.dto;

public class TopicActionResponse {

    private String message;
    private Long courseId;
    private TopicResponse topic;

    public TopicActionResponse(String message, Long courseId, TopicResponse topic) {
        this.message = message;
        this.courseId = courseId;
        this.topic = topic;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public TopicResponse getTopic() { return topic; }
    public void setTopic(TopicResponse topic) { this.topic = topic; }
}
