package com.course.dto;

public class ModuleActionResponse {

    private String message;
    private Long courseId;
    private ModuleResponse module;

    public ModuleActionResponse(String message, Long courseId, ModuleResponse module) {
        this.message = message;
        this.courseId = courseId;
        this.module = module;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public ModuleResponse getModule() { return module; }
    public void setModule(ModuleResponse module) { this.module = module; }
}
