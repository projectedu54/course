package com.course.service;

import com.course.dto.HighlightRequest;
import com.course.dto.HighlightResponse;
import com.course.entity.UserHighlight;
import com.course.repository.ContentRepository;
import com.course.repository.HighlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceHighlightTest {

    @Mock
    private HighlightRepository highlightRepository;

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private ContentService contentService;

    private final Long USER_ID = 1L;
    private final Long CONTENT_ID = 10L;

    @Test
    void saveHighlight_WhenNew_ShouldCreate() {
        // Arrange
        HighlightRequest request = new HighlightRequest();
        request.setSelectionData("coords-123");
        request.setHighlightedText("Java");

        when(contentRepository.existsById(CONTENT_ID)).thenReturn(true);
        when(highlightRepository.findByUserIdAndContentId(USER_ID, CONTENT_ID))
                .thenReturn(Collections.emptyList());
        
        when(highlightRepository.save(any(UserHighlight.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        HighlightResponse result = contentService.saveHighlight(USER_ID, CONTENT_ID, request);

        // Assert
        assertNotNull(result);
        verify(highlightRepository, times(1)).save(any());
    }

    @Test
    void saveHighlight_WhenDuplicateCoords_ShouldUpdate() {
        // Arrange
        String sharedCoords = "coords-123";
        HighlightRequest request = new HighlightRequest();
        request.setSelectionData(sharedCoords);
        request.setColor("red");

        UserHighlight existingHighlight = new UserHighlight();
        existingHighlight.setId(99L);
        existingHighlight.setSelectionCoords(sharedCoords);

        when(contentRepository.existsById(CONTENT_ID)).thenReturn(true);
        when(highlightRepository.findByUserIdAndContentId(USER_ID, CONTENT_ID))
                .thenReturn(List.of(existingHighlight));
        
        when(highlightRepository.save(any(UserHighlight.class))).thenReturn(existingHighlight);

        // Act
        HighlightResponse result = contentService.saveHighlight(USER_ID, CONTENT_ID, request);

        // Assert
        assertEquals(99L, result.getId());
        verify(highlightRepository, times(1)).save(existingHighlight); // Verifies update, not new insert
    }
}