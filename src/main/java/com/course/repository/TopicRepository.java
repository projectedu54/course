package com.course.repository;

import com.course.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    // ========== FIND TOPICS BY PARENT ==========
    List<Topic> findByCourseId(Long courseId);
    List<Topic> findByModuleId(Long moduleId);
    List<Topic> findByChapterId(Long chapterId);
    List<Topic> findBySectionId(Long sectionId);

    // ========== MAX DISPLAY ORDER ==========
    @Query("SELECT MAX(t.displayOrder) FROM Topic t WHERE t.course.id = :courseId")
    Integer findMaxDisplayOrderByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT MAX(t.displayOrder) FROM Topic t WHERE t.module.id = :moduleId")
    Integer findMaxDisplayOrderByModuleId(@Param("moduleId") Long moduleId);

    @Query("SELECT MAX(t.displayOrder) FROM Topic t WHERE t.chapter.id = :chapterId")
    Integer findMaxDisplayOrderByChapterId(@Param("chapterId") Long chapterId);

    @Query("SELECT MAX(t.displayOrder) FROM Topic t WHERE t.section.id = :sectionId")
    Integer findMaxDisplayOrderBySectionId(@Param("sectionId") Long sectionId);

    // ========== DUPLICATE TITLE CHECKS ==========
    boolean existsByTitleAndCourseId(String title, Long courseId);
    boolean existsByTitleAndModuleId(String title, Long moduleId);
    boolean existsByTitleAndChapterId(String title, Long chapterId);
    boolean existsByTitleAndSectionId(String title, Long sectionId);

    // For update (exclude current topic)
    boolean existsByTitleAndCourseIdAndIdNot(String title, Long courseId, Long id);
    boolean existsByTitleAndModuleIdAndIdNot(String title, Long moduleId, Long id);
    boolean existsByTitleAndChapterIdAndIdNot(String title, Long chapterId, Long id);
    boolean existsByTitleAndSectionIdAndIdNot(String title, Long sectionId, Long id);
}