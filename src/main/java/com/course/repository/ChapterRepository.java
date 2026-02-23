package com.course.repository;

import com.course.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    List<Chapter> findByCourseId(Long courseId);

    @Query("SELECT MAX(c.displayOrder) FROM Chapter c WHERE c.course.id = :courseId")
    Integer findMaxDisplayOrderByCourseId(@Param("courseId") Long courseId);

    boolean existsByCourseIdAndTitle(Long courseId, String title);

    boolean existsByCourseIdAndTitleAndIdNot(Long courseId, String title, Long id);
}
