package com.course.service;

import com.course.entity.Note;
import com.course.entity.NoteRequest;
import com.course.enums.LevelType;
import com.course.exception.customException.BadRequestException;
import com.course.exception.customException.ForbiddenException;
import com.course.exception.customException.ResourceNotFoundException;
import com.course.repository.NoteRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NoteService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final NoteRepository repository;
    private final ObjectMapper objectMapper;

    public NoteService(NoteRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Note create(NoteRequest request, Long authenticatedUserId) {
        validateUserAccess(request.getUserId(), authenticatedUserId);

        Note note = new Note();
        note.setUserId(authenticatedUserId);
        note.setLevelType(request.getLevelType());
        note.setLevelId(request.getLevelId());
        note.setNoteText(request.getNoteText().trim());
        note.setMetadata(normalizeMetadata(request.getMetadata()));

        // Keep timestamps explicit and deterministic even though entity hooks also exist.
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        return repository.save(note);
    }

    @Transactional(readOnly = true)
    public List<Note> getUserNotes(Long requestedUserId, Long authenticatedUserId) {
        validateUserAccess(requestedUserId, authenticatedUserId);
        return repository.findByUserIdOrderByUpdatedAtDescCreatedAtDesc(authenticatedUserId);
    }

    @Transactional(readOnly = true)
    public List<Note> getNotesByLevel(Long requestedUserId, LevelType levelType, Long levelId, Long authenticatedUserId) {
        validateUserAccess(requestedUserId, authenticatedUserId);
        return repository.findByUserIdAndLevelTypeAndLevelIdOrderByUpdatedAtDescCreatedAtDesc(
                authenticatedUserId,
                levelType,
                levelId
        );
    }

    @Transactional
    public Note update(Long id, NoteRequest request, Long authenticatedUserId) {
        validateUserAccess(request.getUserId(), authenticatedUserId);

        Note note = findOwnedNote(id, authenticatedUserId);

        if (request.getLevelType() != null && request.getLevelType() != note.getLevelType()) {
            throw new BadRequestException("Updating note levelType is not supported", "NOTE_LEVEL_LOCKED");
        }

        if (request.getLevelId() != null && !request.getLevelId().equals(note.getLevelId())) {
            throw new BadRequestException("Updating note levelId is not supported", "NOTE_LEVEL_LOCKED");
        }

        note.setNoteText(request.getNoteText().trim());

        if (request.getMetadata() != null) {
            note.setMetadata(normalizeMetadata(request.getMetadata()));
        }

        note.setUpdatedAt(LocalDateTime.now());
        return repository.save(note);
    }

    @Transactional
    public void delete(Long id, Long authenticatedUserId) {
        Note note = findOwnedNote(id, authenticatedUserId);
        repository.delete(note);
    }

    private void validateUserAccess(Long requestedUserId, Long authenticatedUserId) {
        if (requestedUserId == null || authenticatedUserId == null) {
            throw new BadRequestException("userId is required", "MISSING_USER_ID");
        }

        if (!requestedUserId.equals(authenticatedUserId)) {
            throw new ForbiddenException("Users can only access their own notes");
        }
    }

    private Note findOwnedNote(Long noteId, Long authenticatedUserId) {
        return repository.findByIdAndUserId(noteId, authenticatedUserId)
                .orElseGet(() -> {
                    Note note = repository.findById(noteId)
                            .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));

                    if (!note.getUserId().equals(authenticatedUserId)) {
                        throw new ForbiddenException("Users can only access their own notes");
                    }

                    return note;
                });
    }

    private Map<String, Object> normalizeMetadata(Object metadata) {
        if (metadata == null) {
            return null;
        }

        if (metadata instanceof JsonNode jsonNode) {
            if (jsonNode.isNull()) {
                return null;
            }
            if (jsonNode.isTextual()) {
                return parseMetadataString(jsonNode.asText());
            }
            if (!jsonNode.isObject()) {
                throw new BadRequestException("metadata must be a JSON object or a JSON string object", "INVALID_METADATA");
            }

            return objectMapper.convertValue(jsonNode, MAP_TYPE);
        }

        if (metadata instanceof String metadataString) {
            return parseMetadataString(metadataString);
        }

        if (metadata instanceof Map<?, ?>) {
            return objectMapper.convertValue(metadata, MAP_TYPE);
        }

        throw new BadRequestException("metadata must be a JSON object or a JSON string object", "INVALID_METADATA");
    }

    private Map<String, Object> parseMetadataString(String metadataString) {
        String trimmed = metadataString == null ? "" : metadataString.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        try {
            JsonNode parsed = objectMapper.readTree(trimmed);
            if (!parsed.isObject()) {
                throw new BadRequestException("metadata must be a JSON object", "INVALID_METADATA");
            }
            return objectMapper.convertValue(parsed, MAP_TYPE);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("metadata must be valid JSON", "INVALID_METADATA");
        }
    }
}
