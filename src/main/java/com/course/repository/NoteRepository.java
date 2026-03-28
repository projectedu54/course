package com.course.repository;

import com.course.entity.Note;
import com.course.enums.LevelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserIdOrderByUpdatedAtDescCreatedAtDesc(Long userId);

    List<Note> findByUserIdAndLevelTypeAndLevelIdOrderByUpdatedAtDescCreatedAtDesc(
            Long userId,
            LevelType levelType,
            Long levelId
    );

    List<Note> findByUserIdAndLevelTypeAndLevelIdIn(Long userId, LevelType levelType, Collection<Long> levelIds);

    List<Note> findByUserIdAndLevelTypeInAndLevelIdIn(
            Long userId,
            Collection<LevelType> levelTypes,
            Collection<Long> levelIds
    );

    Optional<Note> findByIdAndUserId(Long id, Long userId);
}
