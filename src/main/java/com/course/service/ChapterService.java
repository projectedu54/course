package com.course.service;

import com.course.dto.*;
import com.course.entity.*;
import com.course.exception.ResourceNotFoundException;
import com.course.repository.*;
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

        Chapter chapter = new Chapter();
        chapter.setTitle(request.getTitle());
        chapter.setDisplayOrder(request.getDisplayOrder());
        chapter.setCourse(course);

        Chapter saved = chapterRepository.save(chapter);

        return new ChapterActionResponse(
                "Chapter added to course successfully",
                courseId,
                new ChapterResponse(saved.getId(), saved.getTitle(), saved.getDisplayOrder())
        );
    }

    // ================= GET ALL =================
    public List<ChapterResponse> getChaptersByCourse(Long courseId) {
        return chapterRepository.findByCourseId(courseId)
                .stream()
                .map(c -> new ChapterResponse(c.getId(), c.getTitle(), c.getDisplayOrder()))
                .collect(Collectors.toList());
    }

    // ================= GET ONE =================
    public ChapterResponse getChapterById(Long courseId, Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        if (!chapter.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Chapter does not belong to the course");
        }

        return new ChapterResponse(chapter.getId(), chapter.getTitle(), chapter.getDisplayOrder());
    }

    // ================= UPDATE =================
    public ChapterActionResponse updateChapter(Long courseId, Long chapterId, ChapterRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        if (!chapter.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Chapter does not belong to the course");
        }

        chapter.setTitle(request.getTitle());
        chapter.setDisplayOrder(request.getDisplayOrder());

        Chapter updated = chapterRepository.save(chapter);

        return new ChapterActionResponse(
                "Chapter updated successfully",
                courseId,
                new ChapterResponse(updated.getId(), updated.getTitle(), updated.getDisplayOrder())
        );
    }

    // ================= DELETE =================
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
