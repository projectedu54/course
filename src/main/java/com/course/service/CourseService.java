package com.course.service;

import com.course.client.CatalogClient;
import com.course.dto.*;
import com.course.entity.*;
import com.course.entity.Module;
import com.course.enums.CourseStatus;
import com.course.event.CoursePublishedEvent;
import com.course.exception.customException.CourseServiceException;
import com.course.exception.customException.CourseValidationException;
import com.course.mapper.CourseMapper;
import com.course.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final ChapterRepository chapterRepository;
    private final SectionRepository sectionRepository;
    private final TopicRepository topicRepository;
    private final ContentRepository contentRepository;
    private final CatalogClient catalogClient;
    private final TagRepository tagRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Inject the topic name dynamically from application.properties
    @Value("${kafka.topic.course-published}")
    private String coursePublishedTopic;

    public CourseService(CourseRepository courseRepository,
                         ModuleRepository moduleRepository,
                         ChapterRepository chapterRepository,
                         SectionRepository sectionRepository,
                         TopicRepository topicRepository,
                         ContentRepository contentRepository,
                         CatalogClient catalogClient,
                         TagRepository tagRepository,
                         KafkaTemplate<String, Object> kafkaTemplate) {
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.chapterRepository = chapterRepository;
        this.sectionRepository = sectionRepository;
        this.topicRepository = topicRepository;
        this.contentRepository = contentRepository;
        this.catalogClient = catalogClient;
        this.tagRepository = tagRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // =============================
    // CREATE COURSE
    // =============================
    @Transactional
    public Course createCourse(CourseRequest request, Long userId) {
        Boolean exists;
        try {
            exists = catalogClient.exists(String.valueOf(request.getCatalogId()));
        } catch (Exception e) {
            throw new CourseServiceException(
                    "Cannot create course because Catalog service is unavailable",
                    HttpStatus.SERVICE_UNAVAILABLE.value()
            );
        }

        if (!Boolean.TRUE.equals(exists)) {
            throw new CourseServiceException(
                    "Catalog ID " + request.getCatalogId() + " does not exist"
            );
        }

        boolean duplicate = courseRepository
                .existsByTitleIgnoreCaseAndCreatedBy(request.getTitle(), userId);

        if (duplicate) {
            throw new CourseServiceException(
                    "You already created a course with this title"
            );
        }

        Set<Tag> tags = handleTagsBulk(request.getTags());

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

        if (!course.getCreatedBy().equals(userId)) {
            throw new CourseServiceException("You are not allowed to update this course");
        }

        if (request.getCatalogId() != null) {
            Boolean exists;
            try {
                exists = catalogClient.exists(String.valueOf(request.getCatalogId()));
            } catch (Exception e) {
                throw new CourseServiceException(
                        "Catalog service is currently unavailable. Please try again later.",
                        HttpStatus.SERVICE_UNAVAILABLE.value()
                );
            }

            if (!Boolean.TRUE.equals(exists)) {
                throw new CourseServiceException("Invalid catalog id", 400);
            }

            course.setCatalogId(Long.valueOf(request.getCatalogId()));
        }

        if (request.getCourseStructure() != null &&
                request.getCourseStructure() != course.getCourseStructure()) {
            throw new CourseServiceException("Course structure cannot be changed once created");
        }

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

        if (request.getTags() != null) {
            Set<Tag> tags = handleTagsBulk(request.getTags());
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

    private Set<Tag> handleTagsBulk(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> normalizedNames = tagNames.stream()
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toSet());

        for (String name : normalizedNames) {
            tagRepository.insertIgnore(name);
        }

        return new HashSet<>(tagRepository.findByNameIn(normalizedNames));
    }

    /**
     * PUBLISH COURSE LOGIC
     * Validates ownership, external catalog, and deep content hierarchy.
     * Triggers a Kafka event upon successful status change using the configurable topic name.
     */
    @Transactional
    public Course publishCourse(Long id, Long userId) {
        // 1. Fetch Course
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseServiceException("Course not found", 404));

        // 2. Ownership Validation
        if (!course.getCreatedBy().equals(userId)) {
            throw new CourseServiceException("You are not authorized to publish this course", 403);
        }

        // 3. Status Check
        if (course.getStatus() == CourseStatus.PUBLISHED) {
            throw new CourseServiceException("Course is already published", 400);
        }

        // 4. Metadata Check
        validateMetadata(course);

        // 5. External Catalog Validation
        validateCatalogPresence(course.getCatalogId());

        // 6. Hybrid Hierarchy Validation
        validateHierarchy(course);

        // 7. Transition State
        course.setStatus(CourseStatus.PUBLISHED);
        course.onUpdate(userId);
        Course savedCourse = courseRepository.save(course);

        // 8. PUBLISH KAFKA EVENT
        try {
            CoursePublishedEvent event = new CoursePublishedEvent(
                    savedCourse.getId(),
                    userId,
                    savedCourse.getTitle(),
                    String.valueOf(savedCourse.getCatalogId())
            );

            // Send message using the topic loaded from application.properties
            kafkaTemplate.send(coursePublishedTopic, String.valueOf(savedCourse.getId()), event);
        } catch (Exception e) {
            throw new CourseServiceException("Course published successfully, but failed to queue publication event: " + e.getMessage(), 500);
        }

        return savedCourse;
    }

    private void validateMetadata(Course course) {
        if (course.getDescription() == null || course.getDescription().length() < 100) {
            throw new CourseServiceException("Description must be at least 100 characters for publishing.");
        }
        if (course.getTags() == null || course.getTags().isEmpty()) {
            throw new CourseServiceException("At least one tag is required before publishing.");
        }
    }

    private void validateCatalogPresence(Long catalogId) {
        try {
            Boolean exists = catalogClient.exists(String.valueOf(catalogId));
            if (!Boolean.TRUE.equals(exists)) {
                throw new CourseServiceException("Associated Catalog ID " + catalogId + " is invalid or inactive.");
            }
        } catch (Exception e) {
            throw new CourseServiceException("Catalog service is currently unavailable. Please try again later.", 503);
        }
    }

    private void validateHierarchy(Course course) {
        Long courseId = course.getId();
        var structure = course.getCourseStructure();
        List<String> validationErrors = new ArrayList<>();

        boolean hasUnits = switch (structure) {
            case MODULE -> {
                List<Module> modules = moduleRepository.findByCourseId(courseId);
                modules.forEach(m -> validateUnit(m.getId(), m.getTitle(), "Module", validationErrors));
                yield !modules.isEmpty();
            }
            case CHAPTER -> {
                List<Chapter> chapters = chapterRepository.findByCourseId(courseId);
                chapters.forEach(ch -> validateUnit(ch.getId(), ch.getTitle(), "Chapter", validationErrors));
                yield !chapters.isEmpty();
            }
            case SECTION -> {
                List<Section> sections = sectionRepository.findByCourseId(courseId);
                sections.forEach(s -> validateUnit(s.getId(), s.getTitle(), "Section", validationErrors));
                yield !sections.isEmpty();
            }
        };

        List<String> directEmptyTopics = topicRepository.findEmptyDirectTopicTitles(courseId);
        boolean hasDirectTopics = topicRepository.existsDirectTopicsByCourseId(courseId);

        if (!directEmptyTopics.isEmpty()) {
            validationErrors.add("The following direct topics are missing content: [" + String.join(", ", directEmptyTopics) + "]");
        }

        if (!hasUnits && !hasDirectTopics) {
            validationErrors.add("Course is empty. Please add " + structure.name().toLowerCase() + "s or direct topics.");
        }

        if (!validationErrors.isEmpty()) {
            throw new CourseValidationException(validationErrors);
        }
    }

    private void validateUnit(Long id, String title, String type, List<String> errors) {
        boolean hasTopics = switch (type) {
            case "Module" -> topicRepository.existsByModuleId(id);
            case "Chapter" -> topicRepository.existsByChapterId(id);
            case "Section" -> topicRepository.existsBySectionId(id);
            default -> false;
        };

        if (!hasTopics) {
            errors.add(type + " '" + title + "' is empty (no topics added).");
            return;
        }

        List<String> emptyTopicTitles = switch (type) {
            case "Module" -> topicRepository.findEmptyTopicTitlesInModule(id);
            case "Chapter" -> topicRepository.findEmptyTopicTitlesInChapter(id);
            case "Section" -> topicRepository.findEmptyTopicTitlesInSection(id);
            default -> List.of();
        };

        if (!emptyTopicTitles.isEmpty()) {
            String names = String.join(", ", emptyTopicTitles);
            errors.add("In " + type + " '" + title + "', these topics are missing content: [" + names + "]");
        }
    }

    public Page<CourseResponse> searchPublishedCourses(String keyword, Set<String> tags, Pageable pageable) {
        Page<Object[]> results = courseRepository.searchPublishedCoursesWithPricing(keyword, tags, pageable);

        return results.map(row -> {
            Course course = (Course) row[0];
            PriceCatalog price = (PriceCatalog) row[1];

            CourseResponse response = CourseMapper.toResponse(course);
            response.setPrice(price);
            return response;
        });
    }

    public List<CourseMetadataDTO> getMetadataByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<Course> courses = courseRepository.findAllById(ids);

        return courses.stream()
                .map(c -> new CourseMetadataDTO(
                        c.getId(),
                        c.getTitle(),
                        "thumbnail_placeholder.jpg"
                ))
                .collect(Collectors.toList());
    }

    public SyllabusDTO getSyllabus(Long courseId) {
        Course course = getCourseById(courseId);
        var structure = course.getCourseStructure();

        List<UnitDTO> units = new ArrayList<>();

        if (structure != null) {
            switch (structure) {
                case MODULE -> {
                    moduleRepository.findByCourseIdOrderByDisplayOrderAsc(courseId)
                            .forEach(m -> units.add(new UnitDTO(m.getId(), m.getTitle(), getTopicsForUnit(m.getId(), "MODULE"))));
                }
                case CHAPTER -> {
                    chapterRepository.findByCourseIdOrderByDisplayOrderAsc(courseId)
                            .forEach(ch -> units.add(new UnitDTO(ch.getId(), ch.getTitle(), getTopicsForUnit(ch.getId(), "CHAPTER"))));
                }
                case SECTION -> {
                    sectionRepository.findByCourseIdOrderByDisplayOrderAsc(courseId)
                            .forEach(s -> units.add(new UnitDTO(s.getId(), s.getTitle(), getTopicsForUnit(s.getId(), "SECTION"))));
                }
            }
        }

        List<TopicDTO> rootTopics = topicRepository.findByCourseId(courseId).stream()
                .filter(t -> t.getModule() == null && t.getChapter() == null && t.getSection() == null)
                .sorted((a, b) -> a.getDisplayOrder().compareTo(b.getDisplayOrder()))
                .map(this::mapToTopicDTO)
                .toList();

        return new SyllabusDTO(
                courseId,
                course.getTitle(),
                structure != null ? structure.name() : "NONE",
                units,
                rootTopics
        );
    }

    private List<TopicDTO> getTopicsForUnit(Long unitId, String type) {
        List<Topic> topics = switch (type) {
            case "MODULE" -> topicRepository.findByModuleId(unitId);
            case "CHAPTER" -> topicRepository.findByChapterId(unitId);
            case "SECTION" -> topicRepository.findBySectionId(unitId);
            default -> List.of();
        };
        return topics.stream().map(this::mapToTopicDTO).toList();
    }

    private TopicDTO mapToTopicDTO(Topic t) {
        List<ContentDTO> contents = contentRepository.findByTopicIdOrderByDisplayOrderAsc(t.getId())
                .stream().map(c -> new ContentDTO(c.getId(), c.getTitle(), c.getTextContent(), c.getContentType().name(), c.getContentUrl()))
                .toList();
        return new TopicDTO(t.getId(), t.getTitle(), contents);
    }
}