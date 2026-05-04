package com.course.repository;

import com.course.entity.UserHighlight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HighlightRepository extends JpaRepository<UserHighlight, Long> {
    List<UserHighlight> findByUserIdAndContentId(Long userId, Long contentId);
}