package com.course.entity;

import com.course.enums.CourseStructure;
import com.course.enums.CourseStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "course_tbl",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_course_title_user", columnNames = {"title", "created_by"})
        },
        indexes = {
                @Index(name = "idx_course_title", columnList = "title"),
                @Index(name = "idx_course_status", columnList = "status"),
                @Index(name = "idx_course_created_at", columnList = "created_at"),
                @Index(name = "idx_course_created_by", columnList = "created_by")
        }
)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "course_type", nullable = false)
    private String courseType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    @Column(name = "catalog_id", nullable = false)
    private Long catalogId;

    @ManyToMany
    @JoinTable(
            name = "course_tag",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private CourseStructure courseStructure;

    // ===== Audit Fields =====

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    public Course() {}

    // ===== Lifecycle =====

    public void onCreate(Long userId) {
        this.createdAt = LocalDateTime.now();
        this.createdBy = userId;
    }

    public void onUpdate(Long userId) {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }

    // ===== Getters & Setters =====

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getCourseType() { return courseType; }

    public void setCourseType(String courseType) { this.courseType = courseType; }

    public CourseStatus getStatus() { return status; }

    public void setStatus(CourseStatus status) { this.status = status; }

    public Long getCatalogId() { return catalogId; }

    public void setCatalogId(Long catalogId) { this.catalogId = catalogId; }

    public Set<Tag> getTags() { return tags; }

    public void setTags(Set<Tag> tags) { this.tags = tags; }

    public CourseStructure getCourseStructure() { return courseStructure; }

    public void setCourseStructure(CourseStructure courseStructure) { this.courseStructure = courseStructure; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Long getCreatedBy() { return createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public Long getUpdatedBy() { return updatedBy; }
}