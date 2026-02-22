package com.course.repository;

import com.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface CourseRepository extends JpaRepository<Course, Long> {

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
