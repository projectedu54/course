package com.course.service;

import com.course.dto.ChapterActionResponse;
import com.course.dto.ChapterRequest;
import com.course.dto.ChapterResponse;
import com.course.entity.Chapter;
import com.course.entity.Course;
import com.course.enums.CourseStructure;
import com.course.exception.ResourceNotFoundException;
import com.course.exception.customException.InvalidCourseStructureException;
import com.course.repository.ChapterRepository;
import com.course.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;

    public ChapterService(ChapterRepository chapterRepository,
                          CourseRepository courseRepository) {
        this.chapterRepository = chapterRepository;
        this.courseRepository = courseRepository;
    }

    public ChapterActionResponse createChapter(Long courseId, ChapterRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getCourseStructure()==null || course.getCourseStructure() != CourseStructure.CHAPTER) {
            throw new InvalidCourseStructureException(
                    "Chapters are not allowed for this course. Selected structure: " + course.getCourseStructure()
            );
        }

        Chapter chapter = new Chapter();
        chapter.setTitle(request.getTitle());

        // ✅ Auto-increment displayOrder
        Integer maxOrder = chapterRepository.findMaxDisplayOrderByCourseId(courseId);
        chapter.setDisplayOrder((maxOrder == null ? 0 : maxOrder) + 1);

        chapter.setCourse(course);
        Chapter saved = chapterRepository.save(chapter);

        return new ChapterActionResponse(
                "Chapter added to course successfully",
                courseId,
                new ChapterResponse(saved.getId(), saved.getTitle(), saved.getDisplayOrder())
        );
    }

    public List<ChapterResponse> getChaptersByCourse(Long courseId) {
        return chapterRepository.findByCourseId(courseId)
                .stream()
                .map(c -> new ChapterResponse(c.getId(), c.getTitle(), c.getDisplayOrder()))
                .collect(Collectors.toList());
    }

    public ChapterResponse getChapterById(Long courseId, Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        if (!chapter.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Chapter does not belong to the course");
        }

        return new ChapterResponse(chapter.getId(), chapter.getTitle(), chapter.getDisplayOrder());
    }

    public ChapterActionResponse updateChapter(Long courseId, Long chapterId, ChapterRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        if (!chapter.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Chapter does not belong to the course");
        }

        chapter.setTitle(request.getTitle());
        Chapter updated = chapterRepository.save(chapter);

        return new ChapterActionResponse(
                "Chapter updated successfully",
                courseId,
                new ChapterResponse(updated.getId(), updated.getTitle(), updated.getDisplayOrder())
        );
    }

    public ChapterActionResponse deleteChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        Long courseId = chapter.getCourse().getId();
        chapterRepository.delete(chapter);

        return new ChapterActionResponse(
                "Chapter deleted successfully",
                courseId,
                null
        );
    }
}
