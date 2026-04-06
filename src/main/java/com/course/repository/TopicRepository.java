package com.course.repository;

import com.course.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    // ========== FIND TOPICS BY PARENT ==========
    List<Topic> findByCourseId(Long courseId);
    List<Topic> findByCourseIdOrderByDisplayOrderAsc(Long courseId);
    List<Topic> findByModuleId(Long moduleId);
    List<Topic> findByChapterId(Long chapterId);
    List<Topic> findBySectionId(Long sectionId);
    List<Topic> findByModuleIdIn(Collection<Long> moduleIds);
    List<Topic> findByChapterIdIn(Collection<Long> chapterIds);
    List<Topic> findBySectionIdIn(Collection<Long> sectionIds);

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

    boolean existsByCourseId(Long courseId);


    // Check if a specific Module/Chapter/Section has ANY topics
    boolean existsByModuleId(Long moduleId);
    boolean existsByChapterId(Long chapterId);
    boolean existsBySectionId(Long sectionId);

    // Optimized "Empty Leaf" check for the entire Course
    // This checks: "Are there any topics in this course that have zero content?"
    @Query("SELECT COUNT(t) FROM Topic t WHERE t.course.id = :courseId " +
            "AND (SELECT COUNT(c) FROM Content c WHERE c.topic.id = t.id) = 0")
    long countTopicsWithoutContent(@Param("courseId") Long courseId);

    // Check if any topics under a specific container are missing content
    @Query("SELECT COUNT(t) FROM Topic t WHERE t.module.id = :moduleId " +
            "AND (SELECT COUNT(c) FROM Content c WHERE c.topic.id = t.id) = 0")
    long countTopicsInModuleWithoutContent(@Param("moduleId") Long moduleId);

    @Query("SELECT COUNT(t) FROM Topic t WHERE t.chapter.id = :chapterId " +
            "AND (SELECT COUNT(c) FROM Content c WHERE c.topic.id = t.id) = 0")
    long countTopicsInChapterWithoutContent(@Param("chapterId") Long chapterId);

    @Query("SELECT COUNT(t) FROM Topic t WHERE t.section.id = :sectionId " +
            "AND (SELECT COUNT(c) FROM Content c WHERE c.topic.id = t.id) = 0")
    long countTopicsInSectionWithoutContent(@Param("sectionId") Long sectionId);

    @Query("SELECT t.title FROM Topic t WHERE t.module.id = :moduleId " +
            "AND (SELECT COUNT(c) FROM Content c WHERE c.topic.id = t.id) = 0")
    List<String> findEmptyTopicTitlesInModule(@Param("moduleId") Long moduleId);

    @Query("SELECT t.title FROM Topic t WHERE t.chapter.id = :chapterId " +
            "AND (SELECT COUNT(c) FROM Content c WHERE c.topic.id = t.id) = 0")
    List<String> findEmptyTopicTitlesInChapter(@Param("chapterId") Long chapterId);

    @Query("SELECT t.title FROM Topic t WHERE t.section.id = :sectionId " +
            "AND (SELECT COUNT(c) FROM Content c WHERE c.topic.id = t.id) = 0")
    List<String> findEmptyTopicTitlesInSection(@Param("sectionId") Long sectionId);

    // TopicRepository.java

    // Check if any topics exist directly under the course (not inside a unit)
    @Query("SELECT COUNT(t) > 0 FROM Topic t WHERE t.course.id = :courseId " +
            "AND t.module.id IS NULL AND t.chapter.id IS NULL AND t.section.id IS NULL")
    boolean existsDirectTopicsByCourseId(@Param("courseId") Long courseId);

    // Get titles of direct topics that have no content
    @Query("SELECT t.title FROM Topic t WHERE t.course.id = :courseId " +
            "AND t.module.id IS NULL AND t.chapter.id IS NULL AND t.section.id IS NULL " +
            "AND (SELECT COUNT(c) FROM Content c WHERE c.topic.id = t.id) = 0")
    List<String> findEmptyDirectTopicTitles(@Param("courseId") Long courseId);
}
