package com.course.repository;

import com.course.entity.Note;
import com.course.enums.LevelType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserId(Long userId);

    List<Note> findByUserIdAndLevelTypeAndLevelId(Long userId, LevelType levelType, Long levelId);
}