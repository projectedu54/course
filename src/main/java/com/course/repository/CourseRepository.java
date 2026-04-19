package com.course.repository;

import com.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface CourseRepository extends JpaRepository<Course, Long> {


    @Query("""
        SELECT c, p FROM Course c
        LEFT JOIN PriceCatalog p ON c.id = p.entityId AND p.entityType = 'COURSE' and p.isActive=true
        WHERE c.status = 'PUBLISHED'
        AND (
            LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (:tags IS NULL OR EXISTS (SELECT 1 FROM c.tags t WHERE t.name IN :tags))
    """)
    Page<Object[]> searchPublishedCoursesWithPricing(
            String keyword,
            Set<String> tags,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT c FROM Course c
        LEFT JOIN c.tags t
        WHERE c.status = 'PUBLISHED'
        AND (
            LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (:tags IS NULL OR t.name IN :tags)
    """)
    Page<Course> searchPublishedCourses(
            String keyword,
            Set<String> tags,
            Pageable pageable
    );
    boolean existsByTitleIgnoreCaseAndCreatedBy(String title, Long createdBy);
}
