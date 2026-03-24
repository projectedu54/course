package com.course.service;

import com.course.entity.Note;
import com.course.entity.NoteRequest;
import com.course.enums.LevelType;
import com.course.exception.customException.ResourceNotFoundException;
import com.course.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteService {

    @Autowired
    private NoteRepository repository;

    @Transactional
    public Note create(NoteRequest request) {
        Note note = new Note();
        note.setUserId(request.getUserId());
        note.setLevelType(request.getLevelType());
        note.setLevelId(request.getLevelId());
        note.setNoteText(request.getNoteText());
        note.setMetadata(request.getMetadata());

        // Note: If you use @PrePersist in the Entity, you can remove these two lines:
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        return repository.save(note);
    }

    @Transactional(readOnly = true)
    public List<Note> getUserNotes(Long userId) {
        return repository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Note> getNotesByLevel(Long userId, LevelType levelType, Long levelId) {
        return repository.findByUserIdAndLevelTypeAndLevelId(userId, levelType, levelId);
    }

    @Transactional
    public Note update(Long id, NoteRequest request) {
        // Use a custom exception instead of a generic RuntimeException
        Note note = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));

        if (request.getNoteText() != null && !request.getNoteText().isBlank()) {
            note.setNoteText(request.getNoteText());
        }

        // We don't touch metadata here as per your requirement
        note.setUpdatedAt(LocalDateTime.now());
        return repository.save(note);
    }

    @Transactional
    public void delete(Long id) {
        // Safety check: check existence before deleting to avoid EmptyResultDataAccessException
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete. Note not found with id: " + id);
        }
        repository.deleteById(id);
    }
}