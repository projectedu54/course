package com.course.service;

import com.course.client.CatalogClient;
import com.course.dto.CourseRequest;
import com.course.entity.Course;
import com.course.entity.Tag;
import com.course.enums.CourseStatus;
import com.course.exception.customException.CourseServiceException;
import com.course.repository.CourseRepository;
import com.course.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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

    // =============================
    // CREATE COURSE
    // =============================
    @Transactional
    public Course createCourse(CourseRequest request, Long userId) {

        // Validate catalog
        Boolean exists;
        try {
            exists = catalogClient.exists(String.valueOf(request.getCatalogId()));
        } catch (Exception e) {
            throw new CourseServiceException(
                    "Cannot create course because Catalog service is unavailable", HttpStatus.SERVICE_UNAVAILABLE.value()
            );
        }

        if (!Boolean.TRUE.equals(exists)) {
            throw new CourseServiceException(
                    "Catalog ID " + request.getCatalogId() + " does not exist"
            );
        }

        // Duplicate check per user
        boolean duplicate = courseRepository
                .existsByTitleIgnoreCaseAndCreatedBy(request.getTitle(), userId);

        if (duplicate) {
            throw new CourseServiceException(
                    "You already created a course with this title"
            );
        }

        // Handle tags
        Set<Tag> tags = new HashSet<>();
        if (request.getTags() != null) {
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)));
                tags.add(tag);
            }
        }

        // Create course
        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCourseType(request.getCourseType());
        course.setCatalogId(Long.valueOf(request.getCatalogId()));
        course.setTags(tags);
        course.setCourseStructure(request.getCourseStructure());

        if (request.getStatus() == null || request.getStatus().isBlank()) {
            course.setStatus(CourseStatus.DRAFT);
        } else {
            course.setStatus(CourseStatus.valueOf(request.getStatus()));
        }

        course.onCreate(userId);

        return courseRepository.save(course);
    }

    // =============================
    // GET BY ID
    // =============================
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new CourseServiceException("Course not found"));
    }

    // =============================
    // GET ALL
    // =============================
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // =============================
    // UPDATE COURSE
    // =============================
    @Transactional
    public Course updateCourse(Long id, CourseRequest request, Long userId) {

        Course course = getCourseById(id);

        // Ownership validation
        if (!course.getCreatedBy().equals(userId)) {
            throw new CourseServiceException("You are not allowed to update this course");
        }

        // Validate catalog if changed
        if (request.getCatalogId() != null) {
            Boolean exists;
            try {
                exists = catalogClient.exists(String.valueOf(request.getCatalogId()));
            } catch (Exception e) {
                // Log the issue if you have a logger
                // logger.warn("Catalog service unavailable", e);
                throw new CourseServiceException(
                        "Catalog service is currently unavailable. Please try again later.",
                        HttpStatus.SERVICE_UNAVAILABLE.value() // HTTP status 503 Service Unavailable
                );
            }

            if (!Boolean.TRUE.equals(exists)) {
                throw new CourseServiceException("Invalid catalog id", 400);
            }

            course.setCatalogId(Long.valueOf(request.getCatalogId()));
        }

        // Prevent structure change
        if (request.getCourseStructure() != null &&
                request.getCourseStructure() != course.getCourseStructure()) {
            throw new CourseServiceException("Course structure cannot be changed once created");
        }

        // Prevent duplicate title for the same user
        if (request.getTitle() != null && !request.getTitle().equalsIgnoreCase(course.getTitle())) {
            boolean duplicate = courseRepository
                    .existsByTitleIgnoreCaseAndCreatedBy(request.getTitle(), userId);
            if (duplicate) {
                throw new CourseServiceException(
                        "You already have another course with this title",
                        HttpStatus.BAD_REQUEST.value()
                );
            }
            course.setTitle(request.getTitle());
        }


        if (request.getDescription() != null)
            course.setDescription(request.getDescription());

        if (request.getCourseType() != null)
            course.setCourseType(request.getCourseType());

        if (request.getStatus() != null)
            course.setStatus(CourseStatus.valueOf(request.getStatus()));

        // Update tags
        if (request.getTags() != null) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)));
                tags.add(tag);
            }
            course.setTags(tags);
        }

        course.onUpdate(userId);

        return courseRepository.save(course);
    }

    // =============================
    // DELETE COURSE
    // =============================
    @Transactional
    public void deleteCourse(Long id, Long userId) {

        Course course = getCourseById(id);

        if (!course.getCreatedBy().equals(userId)) {
            throw new CourseServiceException("You are not allowed to delete this course");
        }

        courseRepository.delete(course);
    }

    // =============================
    // SEARCH PUBLISHED
    // =============================
    public Page<Course> searchPublishedCourses(String keyword,
                                               Set<String> tags,
                                               Pageable pageable) {

        if (keyword == null) keyword = "";

        return courseRepository.searchPublishedCourses(
                keyword,
                tags == null || tags.isEmpty() ? null : tags,
                pageable
        );
    }
}