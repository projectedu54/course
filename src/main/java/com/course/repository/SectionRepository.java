package com.course.repository;

import com.course.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByCourseId(Long courseId);

    @Query("SELECT MAX(s.displayOrder) FROM Section s WHERE s.course.id = :courseId")
    Integer findMaxDisplayOrderByCourseId(@Param("courseId") Long courseId);

    boolean existsByCourseIdAndTitle(Long courseId, String title);

    boolean existsByCourseIdAndTitleAndIdNot(Long courseId, String title, Long id);
}
