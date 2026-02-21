package com.course.service;

import com.course.dto.*;
import com.course.entity.Course;
import com.course.entity.Section;
import com.course.enums.CourseStructure;
import com.course.exception.ResourceNotFoundException;
import com.course.exception.customException.InvalidCourseStructureException;
import com.course.repository.CourseRepository;
import com.course.repository.SectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;

    public SectionService(SectionRepository sectionRepository, CourseRepository courseRepository) {
        this.sectionRepository = sectionRepository;
        this.courseRepository = courseRepository;
    }

    public SectionActionResponse createSection(Long courseId, SectionRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getCourseStructure()==null || course.getCourseStructure() != CourseStructure.SECTION) {
            throw new InvalidCourseStructureException(
                    "Sections are not allowed for this course. Selected structure: " + course.getCourseStructure()
            );
        }

        Section section = new Section();
        section.setTitle(request.getTitle());

        // Auto-increment displayOrder
        Integer maxOrder = sectionRepository.findMaxDisplayOrderByCourseId(courseId);
        section.setDisplayOrder((maxOrder == null ? 0 : maxOrder) + 1);

        section.setCourse(course);
        Section saved = sectionRepository.save(section);

        return new SectionActionResponse(
                "Section added to course successfully",
                courseId,
                new SectionResponse(saved.getId(), saved.getTitle(), saved.getDisplayOrder())
        );
    }

    public List<SectionResponse> getSectionsByCourse(Long courseId) {
        return sectionRepository.findByCourseId(courseId)
                .stream()
                .map(s -> new SectionResponse(s.getId(), s.getTitle(), s.getDisplayOrder()))
                .collect(Collectors.toList());
    }

    public SectionResponse getSectionById(Long courseId, Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        if (!section.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Section does not belong to the course");
        }

        return new SectionResponse(section.getId(), section.getTitle(), section.getDisplayOrder());
    }

    public SectionActionResponse updateSection(Long courseId, Long sectionId, SectionRequest request) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        if (!section.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Section does not belong to the course");
        }

        section.setTitle(request.getTitle());
        Section updated = sectionRepository.save(section);

        return new SectionActionResponse(
                "Section updated successfully",
                courseId,
                new SectionResponse(updated.getId(), updated.getTitle(), updated.getDisplayOrder())
        );
    }

    public SectionActionResponse deleteSection(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        Long courseId = section.getCourse().getId();
        sectionRepository.delete(section);

        return new SectionActionResponse(
                "Section deleted successfully",
                courseId,
                null
        );
    }
}
