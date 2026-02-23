package com.course.service;

import com.course.dto.TopicActionResponse;
import com.course.dto.TopicRequest;
import com.course.dto.TopicResponse;
import com.course.entity.*;
import com.course.entity.Module;
import com.course.exception.ResourceNotFoundException;
import com.course.exception.customException.DuplicateTopicTitleException;
import com.course.exception.customException.InvalidTopicParentException;
import com.course.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final ChapterRepository chapterRepository;
    private final SectionRepository sectionRepository;

    public TopicService(TopicRepository topicRepository,
                        CourseRepository courseRepository,
                        ModuleRepository moduleRepository,
                        ChapterRepository chapterRepository,
                        SectionRepository sectionRepository) {
        this.topicRepository = topicRepository;
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.chapterRepository = chapterRepository;
        this.sectionRepository = sectionRepository;
    }

    // ================= CREATE =================
    public TopicActionResponse createTopic(Long parentId, String parentType, TopicRequest request) {

        Topic topic = new Topic();
        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());

        // Determine parent
        switch (parentType.toUpperCase()) {
            case "COURSE":
                Course course = courseRepository.findById(parentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

                // Duplicate title check
                if (topicRepository.existsByTitleAndCourseId(request.getTitle(), parentId)) {
                    throw new DuplicateTopicTitleException("Topic title already exists for this course");
                }

                topic.setCourse(course);
                Integer maxOrderCourse = topicRepository.findMaxDisplayOrderByCourseId(parentId);
                topic.setDisplayOrder((maxOrderCourse == null ? 0 : maxOrderCourse) + 1);
                break;

            case "MODULE":
                Module module = moduleRepository.findById(parentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

                if (topicRepository.existsByTitleAndModuleId(request.getTitle(), parentId)) {
                    throw new DuplicateTopicTitleException("Topic title already exists for this module");
                }
                topic.setModule(module);
                Integer maxOrderModule = topicRepository.findMaxDisplayOrderByModuleId(parentId);
                topic.setDisplayOrder((maxOrderModule == null ? 0 : maxOrderModule) + 1);
                break;

            case "CHAPTER":
                Chapter chapter = chapterRepository.findById(parentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

                if (topicRepository.existsByTitleAndChapterId(request.getTitle(), parentId)) {
                    throw new DuplicateTopicTitleException("Topic title already exists for this chapter");
                }
                topic.setChapter(chapter);
                Integer maxOrderChapter = topicRepository.findMaxDisplayOrderByChapterId(parentId);
                topic.setDisplayOrder((maxOrderChapter == null ? 0 : maxOrderChapter) + 1);
                break;

            case "SECTION":
                Section section = sectionRepository.findById(parentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

                if (topicRepository.existsByTitleAndSectionId(request.getTitle(), parentId)) {
                    throw new DuplicateTopicTitleException("Topic title already exists for this section");
                }
                topic.setSection(section);
                Integer maxOrderSection = topicRepository.findMaxDisplayOrderBySectionId(parentId);
                topic.setDisplayOrder((maxOrderSection == null ? 0 : maxOrderSection) + 1);
                break;

            default:
                throw new InvalidTopicParentException("Invalid parent type: " + parentType);
        }

        Topic saved = topicRepository.save(topic);

        return new TopicActionResponse(
                "Topic created successfully",
                saved.getCourse() != null ? saved.getCourse().getId() : null,
                new TopicResponse(
                        saved.getId(),
                        saved.getTitle(),
                        saved.getDisplayOrder(),
                        parentType.toUpperCase(),
                        parentId,
                        saved.getDescription()
                )
        );
    }

    // ================= GET ALL =================
    public List<TopicResponse> getTopics(String parentType, Long parentId) {
        List<Topic> topics;

        switch (parentType.toUpperCase()) {
            case "COURSE":
                topics = topicRepository.findByCourseId(parentId);
                break;
            case "MODULE":
                topics = topicRepository.findByModuleId(parentId);
                break;
            case "CHAPTER":
                topics = topicRepository.findByChapterId(parentId);
                break;
            case "SECTION":
                topics = topicRepository.findBySectionId(parentId);
                break;
            default:
                throw new InvalidTopicParentException("Invalid parent type: " + parentType);
        }

        return topics.stream()
                .map(t -> new TopicResponse(
                        t.getId(),
                        t.getTitle(),
                        t.getDisplayOrder(),
                        parentType.toUpperCase(),
                        parentId,
                        t.getDescription()
                ))
                .collect(Collectors.toList());
    }

    // ================= GET BY ID =================
    public TopicResponse getTopicById(Long parentId, String parentType, Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        boolean belongs;
        switch (parentType.toUpperCase()) {
            case "COURSE":
                belongs = topic.getCourse() != null && topic.getCourse().getId().equals(parentId);
                break;
            case "MODULE":
                belongs = topic.getModule() != null && topic.getModule().getId().equals(parentId);
                break;
            case "CHAPTER":
                belongs = topic.getChapter() != null && topic.getChapter().getId().equals(parentId);
                break;
            case "SECTION":
                belongs = topic.getSection() != null && topic.getSection().getId().equals(parentId);
                break;
            default:
                throw new InvalidTopicParentException("Invalid parent type: " + parentType);
        }

        if (!belongs) throw new ResourceNotFoundException("Topic does not belong to the parent");

        return new TopicResponse(
                topic.getId(),
                topic.getTitle(),
                topic.getDisplayOrder(),
                parentType.toUpperCase(),
                parentId,
                topic.getDescription()
        );
    }

    // ================= UPDATE =================
    public TopicActionResponse updateTopic(Long parentId, String parentType, Long topicId, TopicRequest request) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Validate parent
        getTopicById(parentId, parentType, topicId);

        switch (parentType.toUpperCase()) {
            case "COURSE":
                if (topicRepository.existsByTitleAndCourseIdAndIdNot(request.getTitle(), parentId, topicId)) {
                    throw new DuplicateTopicTitleException("Topic title already exists for this course");
                }
                break;
            case "MODULE":
                if (topicRepository.existsByTitleAndModuleIdAndIdNot(request.getTitle(), parentId, topicId)) {
                    throw new DuplicateTopicTitleException("Topic title already exists for this module");
                }
                break;
            case "CHAPTER":
                if (topicRepository.existsByTitleAndChapterIdAndIdNot(request.getTitle(), parentId, topicId)) {
                    throw new DuplicateTopicTitleException("Topic title already exists for this chapter");
                }
                break;
            case "SECTION":
                if (topicRepository.existsByTitleAndSectionIdAndIdNot(request.getTitle(), parentId, topicId)) {
                    throw new DuplicateTopicTitleException("Topic title already exists for this section");
                }
                break;
            default:
                throw new InvalidTopicParentException("Invalid parent type: " + parentType);
        }

        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());
        Topic updated = topicRepository.save(topic);

        return new TopicActionResponse(
                "Topic updated successfully",
                updated.getCourse() != null ? updated.getCourse().getId() : null,
                new TopicResponse(
                        updated.getId(),
                        updated.getTitle(),
                        updated.getDisplayOrder(),
                        parentType.toUpperCase(),
                        parentId,
                        updated.getDescription()
                )
        );
    }

    // ================= DELETE =================
    public TopicActionResponse deleteTopic(Long parentId, String parentType, Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Validate parent
        getTopicById(parentId, parentType, topicId);

        Long courseId = topic.getCourse() != null ? topic.getCourse().getId() : null;
        topicRepository.delete(topic);

        return new TopicActionResponse(
                "Topic deleted successfully",
                courseId,
                null
        );
    }
}
