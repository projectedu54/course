package com.course.repository;

import com.course.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByTopicIdOrderByDisplayOrderAsc(Long topicId);

    List<Content> findByTopicId(Long topicId);

    List<Content> findByTopicIdIn(Collection<Long> topicIds);

    @Query("SELECT MAX(c.displayOrder) FROM Content c WHERE c.topic.id = :topicId")
    Integer findMaxDisplayOrderByTopic(Long topicId);

    // Duplicate title check for create
    boolean existsByTopicIdAndTitle(Long topicId, String title);

    // Duplicate title check for update (exclude current content)
    boolean existsByTopicIdAndTitleAndIdNot(Long topicId, String title, Long id);

    boolean existsByTopicId(Long topicId);
    // Add to ContentRepository.java
    @Query("SELECT COUNT(c) > 0 FROM Content c WHERE c.topic.course.id = :courseId")
    boolean existsByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(c) > 0 FROM Content c WHERE c.topic.course.id = :courseId")
    boolean hasContent(@Param("courseId") Long courseId);
}
