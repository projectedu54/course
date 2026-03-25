package com.course.service;

import com.course.entity.Note;
import com.course.entity.NoteRequest;
import com.course.enums.LevelType;
import com.course.exception.customException.BadRequestException;
import com.course.exception.customException.ForbiddenException;
import com.course.repository.NoteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    private NoteService noteService;

    @BeforeEach
    void setUp() {
        noteService = new NoteService(noteRepository, new ObjectMapper());
    }

    @Test
    void createParsesMetadataJsonStringAndUsesAuthenticatedUser() {
        NoteRequest request = buildRequest(7L, LevelType.VIDEO, 101L, "Important concept");
        request.setMetadata("{\"videoTimestamp\":204}");

        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note created = noteService.create(request, 7L);

        assertEquals(7L, created.getUserId());
        assertEquals(LevelType.VIDEO, created.getLevelType());
        assertEquals(101L, created.getLevelId());
        assertEquals("Important concept", created.getNoteText());
        assertEquals(204, created.getMetadata().get("videoTimestamp"));
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void createRejectsMismatchedAuthenticatedUser() {
        NoteRequest request = buildRequest(7L, LevelType.CHAPTER, 12L, "Need to revise");

        assertThrows(ForbiddenException.class, () -> noteService.create(request, 8L));

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void getUserNotesRejectsAccessToAnotherUsersNotes() {
        assertThrows(ForbiddenException.class, () -> noteService.getUserNotes(11L, 7L));
        verifyNoInteractions(noteRepository);
    }

    @Test
    void updateRejectsWhenCallerDoesNotOwnNote() {
        NoteRequest request = buildRequest(7L, LevelType.TOPIC, 25L, "Updated text");

        Note note = new Note();
        note.setId(9L);
        note.setUserId(8L);
        note.setLevelType(LevelType.TOPIC);
        note.setLevelId(25L);
        note.setNoteText("Original");

        when(noteRepository.findByIdAndUserId(9L, 7L)).thenReturn(Optional.empty());
        when(noteRepository.findById(9L)).thenReturn(Optional.of(note));

        assertThrows(ForbiddenException.class, () -> noteService.update(9L, request, 7L));

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void updateChangesTextAndMetadataForOwnedNote() {
        NoteRequest request = buildRequest(7L, LevelType.VIDEO, 101L, "Updated note");
        request.setMetadata(Map.of("videoTimestamp", 240));

        Note note = new Note();
        note.setId(9L);
        note.setUserId(7L);
        note.setLevelType(LevelType.VIDEO);
        note.setLevelId(101L);
        note.setNoteText("Original note");
        note.setMetadata(null);

        when(noteRepository.findByIdAndUserId(9L, 7L)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note updated = noteService.update(9L, request, 7L);

        assertEquals("Updated note", updated.getNoteText());
        assertEquals(240, updated.getMetadata().get("videoTimestamp"));
        verify(noteRepository).save(note);
    }

    @Test
    void createRejectsInvalidMetadataString() {
        NoteRequest request = buildRequest(7L, LevelType.COURSE, 1L, "Course note");
        request.setMetadata("{invalid-json}");

        assertThrows(BadRequestException.class, () -> noteService.create(request, 7L));

        verify(noteRepository, never()).save(any(Note.class));
    }

    private NoteRequest buildRequest(Long userId, LevelType levelType, Long levelId, String noteText) {
        NoteRequest request = new NoteRequest();
        request.setUserId(userId);
        request.setLevelType(levelType);
        request.setLevelId(levelId);
        request.setNoteText(noteText);
        return request;
    }
}
