package com.course.service;

import com.course.dto.ModuleActionResponse;
import com.course.dto.ModuleRequest;
import com.course.dto.ModuleResponse;
import com.course.entity.Course;
import com.course.entity.Module;
import com.course.enums.CourseStructure;
import com.course.exception.ResourceNotFoundException;
import com.course.exception.customException.InvalidCourseStructureException;
import com.course.repository.CourseRepository;
import com.course.repository.ModuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;

    public ModuleService(ModuleRepository moduleRepository, CourseRepository courseRepository) {
        this.moduleRepository = moduleRepository;
        this.courseRepository = courseRepository;
    }

    public ModuleActionResponse createModule(Long courseId, ModuleRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getCourseStructure()==null || course.getCourseStructure()!= CourseStructure.MODULE) {
            throw new InvalidCourseStructureException(
                    "Modules are not allowed for this course. Selected structure: " + course.getCourseStructure()
            );
        }

        Module module = new Module();
        module.setTitle(request.getTitle());

        // Auto-increment displayOrder
        Integer maxOrder = moduleRepository.findMaxDisplayOrderByCourseId(courseId);
        module.setDisplayOrder((maxOrder == null ? 0 : maxOrder) + 1);

        module.setCourse(course);
        Module saved = moduleRepository.save(module);

        return new ModuleActionResponse(
                "Module added to course successfully",
                courseId,
                new ModuleResponse(saved.getId(), saved.getTitle(), saved.getDisplayOrder())
        );
    }

    public List<ModuleResponse> getModulesByCourse(Long courseId) {
        return moduleRepository.findByCourseId(courseId)
                .stream()
                .map(m -> new ModuleResponse(m.getId(), m.getTitle(), m.getDisplayOrder()))
                .collect(Collectors.toList());
    }

    public ModuleResponse getModuleById(Long courseId, Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        if (!module.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Module does not belong to the course");
        }

        return new ModuleResponse(module.getId(), module.getTitle(), module.getDisplayOrder());
    }

    public ModuleActionResponse updateModule(Long courseId, Long moduleId, ModuleRequest request) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        if (!module.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Module does not belong to the course");
        }

        module.setTitle(request.getTitle());
        Module updated = moduleRepository.save(module);

        return new ModuleActionResponse(
                "Module updated successfully",
                courseId,
                new ModuleResponse(updated.getId(), updated.getTitle(), updated.getDisplayOrder())
        );
    }

    public ModuleActionResponse deleteModule(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        Long courseId = module.getCourse().getId();
        moduleRepository.delete(module);

        return new ModuleActionResponse(
                "Module deleted successfully",
                courseId,
                null
        );
    }
}
