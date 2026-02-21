package com.course.repository;

import com.course.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByCourseId(Long courseId);
    List<Topic> findByModuleId(Long moduleId);
    List<Topic> findByChapterId(Long chapterId);
    List<Topic> findBySectionId(Long sectionId);

    @Query("SELECT MAX(t.displayOrder) FROM Topic t WHERE t.course.id = :courseId")
    Integer findMaxDisplayOrderByCourseId(Long courseId);

    @Query("SELECT MAX(t.displayOrder) FROM Topic t WHERE t.module.id = :moduleId")
    Integer findMaxDisplayOrderByModuleId(Long moduleId);

    @Query("SELECT MAX(t.displayOrder) FROM Topic t WHERE t.chapter.id = :chapterId")
    Integer findMaxDisplayOrderByChapterId(Long chapterId);

    @Query("SELECT MAX(t.displayOrder) FROM Topic t WHERE t.section.id = :sectionId")
    Integer findMaxDisplayOrderBySectionId(Long sectionId);
}

