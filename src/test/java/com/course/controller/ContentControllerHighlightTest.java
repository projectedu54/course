package com.course.controller;

import com.course.dto.HighlightRequest;
import com.course.dto.HighlightResponse;
import com.course.service.ContentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // New Import
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContentController.class)
public class ContentControllerHighlightTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean // Replaces @MockBean
    private ContentService contentService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Long USER_ID = 500L;
    private final Long CONTENT_ID = 101L;

    @Test
    @DisplayName("POST Highlight - Verify API Gateway Header Requirement")
    void testSaveHighlightHeader() throws Exception {
        HighlightRequest request = new HighlightRequest();
        request.setHighlightedText("Important Text");
        request.setSelectionData("{}");

        HighlightResponse response = new HighlightResponse(1L, "{}", "Important Text", "yellow");

        when(contentService.saveHighlight(eq(USER_ID), eq(CONTENT_ID), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/topics/1/contents/{contentId}/highlights", CONTENT_ID)
                        .header("X-User-Id", USER_ID) // Gateway Simulation
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }
}