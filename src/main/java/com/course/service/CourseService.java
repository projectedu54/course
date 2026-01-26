package com.course.service;

import com.course.client.CatalogClient;
import com.course.dto.CourseRequest;
import com.course.entity.Course;
import com.course.entity.Tag;
import com.course.exception.customException.CourseServiceException;
import com.course.repository.CourseRepository;
import com.course.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final TagRepository tagRepository;
    private final CatalogClient catalogClient;

    public CourseService(CourseRepository courseRepository,
                         TagRepository tagRepository,
                         CatalogClient catalogClient) {
        this.courseRepository = courseRepository;
        this.tagRepository = tagRepository;
        this.catalogClient = catalogClient;
    }

    @Transactional
    public Course createCourse(CourseRequest request) {
        // Validate Catalog
        Boolean exists;
        try {
            exists = catalogClient.exists(request.getCatalogId());
        } catch (Exception e) {
            throw new RuntimeException("Catalog service unavailable", e);
        }

        if (!Boolean.TRUE.equals(exists)) {
            throw new CourseServiceException("Catalog ID " + request.getCatalogId() + " does not exist");

        }

        // Handle Tags by name
        Set<Tag> tags = new HashSet<>();
        if (request.getTags() != null) {
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)));
                tags.add(tag);
            }
        }

        //  Create Course
        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCourseType(request.getCourseType());

        // Set default status if not provided
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            course.setStatus("DRAFT");
        } else {
            course.setStatus(request.getStatus());
        }

        course.setCatalogId(Long.valueOf(request.getCatalogId()));
        course.setTags(tags);

        return courseRepository.save(course);
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Transactional
    public Course updateCourse(Long id, CourseRequest request) {
        Course course = getCourseById(id);

        // Catalog update
        if (request.getCatalogId() != null) {
            Boolean exists = catalogClient.exists(request.getCatalogId());
            if (!Boolean.TRUE.equals(exists)) {
                throw new IllegalArgumentException("Invalid catalog id");
            }
            course.setCatalogId(Long.valueOf(request.getCatalogId()));
        }

        // Update basic fields
        if (request.getTitle() != null) course.setTitle(request.getTitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getCourseType() != null) course.setCourseType(request.getCourseType());
        if (request.getStatus() != null) course.setStatus(request.getStatus());

        // Update Tags by name
        if (request.getTags() != null) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)));
                tags.add(tag);
            }
            course.setTags(tags);
        }

        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        courseRepository.delete(course);
    }
}
