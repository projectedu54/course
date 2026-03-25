package com.course.controller;

import com.course.dto.NoteSummaryResponse;
import com.course.entity.Note;
import com.course.enums.LevelType;
import com.course.exception.GlobalExceptionHandler;
import com.course.service.NoteService;
import com.course.service.NoteSummaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoteController.class)
@Import(GlobalExceptionHandler.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteService noteService;

    @MockBean
    private NoteSummaryService noteSummaryService;

    @Test
    void createReturnsBadRequestWhenValidationFails() throws Exception {
        String payload = """
                {
                  "userId": 7,
                  "levelType": "VIDEO",
                  "levelId": 101,
                  "noteText": ""
                }
                """;

        mockMvc.perform(post("/api/notes")
                        .header("X-USER-ID", 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.noteText").value("noteText is required"));
    }

    @Test
    void getUserNotesUsesAuthenticatedHeader() throws Exception {
        Note note = new Note();
        note.setId(5L);
        note.setUserId(7L);
        note.setNoteText("My note");

        when(noteService.getUserNotes(7L, 7L)).thenReturn(List.of(note));

        mockMvc.perform(get("/api/notes/user/7")
                        .header("X-USER-ID", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(5))
                .andExpect(jsonPath("$.data[0].userId").value(7));

        verify(noteService).getUserNotes(7L, 7L);
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        doNothing().when(noteService).delete(9L, 7L);

        mockMvc.perform(delete("/api/notes/9")
                        .header("X-USER-ID", 7))
                .andExpect(status().isNoContent());

        verify(noteService).delete(9L, 7L);
    }

    @Test
    void getSummaryUsesAuthenticatedHeader() throws Exception {
        NoteSummaryResponse response = new NoteSummaryResponse();
        response.setUserId(7L);
        response.setLevelType(LevelType.COURSE);
        response.setLevelId(11L);
        response.setTitle("Java Foundations");
        response.setNoteCount(4);

        when(noteSummaryService.getSummary(7L, LevelType.COURSE, 11L, 7L)).thenReturn(response);

        mockMvc.perform(get("/api/notes/summary")
                        .header("X-USER-ID", 7)
                        .param("userId", "7")
                        .param("levelType", "COURSE")
                        .param("levelId", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(7))
                .andExpect(jsonPath("$.data.levelType").value("COURSE"))
                .andExpect(jsonPath("$.data.noteCount").value(4));

        verify(noteSummaryService).getSummary(7L, LevelType.COURSE, 11L, 7L);
    }
}
