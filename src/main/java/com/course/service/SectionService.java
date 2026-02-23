package com.course.service;

import com.course.dto.SectionActionResponse;
import com.course.dto.SectionRequest;
import com.course.dto.SectionResponse;
import com.course.entity.Course;
import com.course.entity.Section;
import com.course.enums.CourseStructure;
import com.course.exception.ResourceNotFoundException;
import com.course.exception.customException.DuplicateTitleException;
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

    // ================= CREATE =================
    public SectionActionResponse createSection(Long courseId, SectionRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getCourseStructure() == null || course.getCourseStructure() != CourseStructure.SECTION) {
            throw new InvalidCourseStructureException(
                    "Sections are not allowed for this course. Selected structure: " + course.getCourseStructure()
            );
        }

        //  Duplicate title check
        boolean exists = sectionRepository.existsByCourseIdAndTitle(courseId, request.getTitle());
        if (exists) {
            throw new DuplicateTitleException("Section with title '" + request.getTitle() + "' already exists in this course");
        }

        Section section = new Section();
        section.setTitle(request.getTitle());
        section.setDescription(request.getDescription());

        // Auto-increment displayOrder
        Integer maxOrder = sectionRepository.findMaxDisplayOrderByCourseId(courseId);
        section.setDisplayOrder((maxOrder == null ? 0 : maxOrder) + 1);

        section.setCourse(course);
        Section saved = sectionRepository.save(section);

        return new SectionActionResponse(
                "Section added to course successfully",
                courseId,
                new SectionResponse(saved.getId(), saved.getTitle(), saved.getDisplayOrder(), saved.getDescription())
        );
    }

    // ================= GET ALL =================
    public List<SectionResponse> getSectionsByCourse(Long courseId) {
        return sectionRepository.findByCourseId(courseId)
                .stream()
                .map(s -> new SectionResponse(s.getId(), s.getTitle(), s.getDisplayOrder(), s.getDescription()))
                .collect(Collectors.toList());
    }

    // ================= GET BY ID =================
    public SectionResponse getSectionById(Long courseId, Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        if (!section.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Section does not belong to the course");
        }

        return new SectionResponse(section.getId(), section.getTitle(), section.getDisplayOrder(), section.getDescription());
    }

    // ================= UPDATE =================
    public SectionActionResponse updateSection(Long courseId, Long sectionId, SectionRequest request) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        if (!section.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Section does not belong to the course");
        }

        //  Duplicate title check excluding current section
        boolean exists = sectionRepository.existsByCourseIdAndTitleAndIdNot(courseId, request.getTitle(), sectionId);
        if (exists) {
            throw new DuplicateTitleException("Section with title '" + request.getTitle() + "' already exists in this course");
        }

        section.setTitle(request.getTitle());
        section.setDescription(request.getDescription());
        Section updated = sectionRepository.save(section);

        return new SectionActionResponse(
                "Section updated successfully",
                courseId,
                new SectionResponse(updated.getId(), updated.getTitle(), updated.getDisplayOrder(), updated.getDescription())
        );
    }

    // ================= DELETE =================
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