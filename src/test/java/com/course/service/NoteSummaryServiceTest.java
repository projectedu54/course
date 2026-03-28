package com.course.service;

import com.course.dto.NoteSummaryResponse;
import com.course.entity.Content;
import com.course.entity.Course;
import com.course.entity.Module;
import com.course.entity.Note;
import com.course.entity.Topic;
import com.course.enums.CourseStructure;
import com.course.enums.LevelType;
import com.course.exception.customException.ForbiddenException;
import com.course.repository.ChapterRepository;
import com.course.repository.ContentRepository;
import com.course.repository.CourseRepository;
import com.course.repository.ModuleRepository;
import com.course.repository.NoteRepository;
import com.course.repository.SectionRepository;
import com.course.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteSummaryServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private ContentRepository contentRepository;

    private NoteSummaryService noteSummaryService;

    @BeforeEach
    void setUp() {
        noteSummaryService = new NoteSummaryService(
                noteRepository,
                courseRepository,
                moduleRepository,
                sectionRepository,
                chapterRepository,
                topicRepository,
                contentRepository
        );
    }

    @Test
    void getSummaryAggregatesCourseHierarchy() {
        Course course = new Course();
        course.setId(1L);
        course.setTitle("Java Foundations");
        course.setCourseStructure(CourseStructure.MODULE);

        Module module = new Module();
        module.setId(10L);
        module.setTitle("Core Syntax");
        module.setDisplayOrder(1);
        module.setCourse(course);

        Topic topic = new Topic();
        topic.setId(20L);
        topic.setTitle("Control Flow");
        topic.setDisplayOrder(1);
        topic.setModule(module);

        Content content = new Content();
        content.setId(30L);
        content.setTitle("Loop Walkthrough");
        content.setDisplayOrder(1);
        content.setTopic(topic);

        Note courseNote = note(100L, 7L, LevelType.COURSE, 1L, "Remember the compile and run steps", LocalDateTime.of(2026, 3, 20, 9, 0));
        Note moduleNote = note(101L, 7L, LevelType.MODULE, 10L, "Classes need matching braces", LocalDateTime.of(2026, 3, 20, 10, 0));
        Note topicNote = note(102L, 7L, LevelType.TOPIC, 20L, "Review for vs while loop differences", LocalDateTime.of(2026, 3, 20, 11, 0));
        Note contentNote = note(103L, 7L, LevelType.VIDEO, 30L, "Constructor example at 1:30", LocalDateTime.of(2026, 3, 20, 12, 0));
        contentNote.setMetadata(Map.of("videoTimestamp", 90));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(moduleRepository.findByCourseIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of(module));
        when(topicRepository.findByCourseIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of());
        when(topicRepository.findByModuleIdIn(List.of(10L))).thenReturn(List.of(topic));
        when(contentRepository.findByTopicIdIn(List.of(20L))).thenReturn(List.of(content));
        when(noteRepository.findByUserIdAndLevelTypeAndLevelIdOrderByUpdatedAtDescCreatedAtDesc(7L, LevelType.COURSE, 1L))
                .thenReturn(List.of(courseNote));
        when(noteRepository.findByUserIdAndLevelTypeAndLevelIdIn(7L, LevelType.MODULE, List.of(10L)))
                .thenReturn(List.of(moduleNote));
        when(noteRepository.findByUserIdAndLevelTypeAndLevelIdIn(7L, LevelType.TOPIC, List.of(20L)))
                .thenReturn(List.of(topicNote));
        when(noteRepository.findByUserIdAndLevelTypeInAndLevelIdIn(
                eq(7L),
                eq(EnumSet.of(LevelType.CONTENT, LevelType.VIDEO, LevelType.QUIZ)),
                eq(List.of(30L))
        )).thenReturn(List.of(contentNote));

        NoteSummaryResponse summary = noteSummaryService.getSummary(7L, LevelType.COURSE, 1L, 7L);

        assertEquals("Java Foundations", summary.getTitle());
        assertEquals(4, summary.getNoteCount());
        assertEquals(1, summary.getDirectNoteCount());
        assertEquals(3, summary.getDescendantNoteCount());
        assertEquals(1, summary.getParentCount());
        assertEquals(1, summary.getTopicCount());
        assertEquals(1, summary.getContentCount());
        assertEquals(4, summary.getHighlights().size());
        assertEquals("Java Foundations", summary.getHighlights().get(0).getSourceTitle());
        assertEquals("Core Syntax", summary.getHighlights().get(1).getSourceTitle());
        assertEquals("Control Flow", summary.getHighlights().get(2).getSourceTitle());
        assertEquals("Loop Walkthrough", summary.getHighlights().get(3).getSourceTitle());
        assertEquals(Integer.valueOf(90), summary.getHighlights().get(3).getVideoTimestampSeconds());
    }

    @Test
    void getSummaryRejectsAccessToAnotherUsersNotes() {
        assertThrows(ForbiddenException.class, () -> noteSummaryService.getSummary(11L, LevelType.COURSE, 1L, 7L));
    }

    private Note note(Long id, Long userId, LevelType levelType, Long levelId, String text, LocalDateTime updatedAt) {
        Note note = new Note();
        note.setId(id);
        note.setUserId(userId);
        note.setLevelType(levelType);
        note.setLevelId(levelId);
        note.setNoteText(text);
        note.setCreatedAt(updatedAt.minusMinutes(5));
        note.setUpdatedAt(updatedAt);
        return note;
    }
}
