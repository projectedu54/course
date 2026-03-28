package com.course.service;

import com.course.dto.NoteSummaryHighlightResponse;
import com.course.dto.NoteSummaryResponse;
import com.course.entity.Chapter;
import com.course.entity.Content;
import com.course.entity.Course;
import com.course.entity.Module;
import com.course.entity.Note;
import com.course.entity.Section;
import com.course.entity.Topic;
import com.course.enums.CourseStructure;
import com.course.enums.LevelType;
import com.course.exception.customException.BadRequestException;
import com.course.exception.customException.ForbiddenException;
import com.course.exception.customException.ResourceNotFoundException;
import com.course.repository.ChapterRepository;
import com.course.repository.ContentRepository;
import com.course.repository.CourseRepository;
import com.course.repository.ModuleRepository;
import com.course.repository.NoteRepository;
import com.course.repository.SectionRepository;
import com.course.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class NoteSummaryService {

    private static final EnumSet<LevelType> CONTENT_LEVEL_TYPES = EnumSet.of(
            LevelType.CONTENT,
            LevelType.VIDEO,
            LevelType.QUIZ
    );
    private static final int MAX_HIGHLIGHTS = 6;
    private static final int MAX_SUMMARY_SNIPPETS = 3;

    private final NoteRepository noteRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final SectionRepository sectionRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;
    private final ContentRepository contentRepository;

    public NoteSummaryService(
            NoteRepository noteRepository,
            CourseRepository courseRepository,
            ModuleRepository moduleRepository,
            SectionRepository sectionRepository,
            ChapterRepository chapterRepository,
            TopicRepository topicRepository,
            ContentRepository contentRepository
    ) {
        this.noteRepository = noteRepository;
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.sectionRepository = sectionRepository;
        this.chapterRepository = chapterRepository;
        this.topicRepository = topicRepository;
        this.contentRepository = contentRepository;
    }

    @Transactional(readOnly = true)
    public NoteSummaryResponse getSummary(
            Long requestedUserId,
            LevelType requestedLevelType,
            Long requestedLevelId,
            Long authenticatedUserId
    ) {
        validateUserAccess(requestedUserId, authenticatedUserId);

        if (requestedLevelType == null) {
            throw new BadRequestException("levelType is required", "MISSING_LEVEL_TYPE");
        }
        if (requestedLevelId == null) {
            throw new BadRequestException("levelId is required", "MISSING_LEVEL_ID");
        }

        SummaryContext context = buildContext(requestedLevelType, requestedLevelId);
        List<OrderedNote> orderedNotes = collectOrderedNotes(authenticatedUserId, context);
        return buildResponse(authenticatedUserId, context, orderedNotes);
    }

    private SummaryContext buildContext(LevelType requestedLevelType, Long requestedLevelId) {
        return switch (requestedLevelType) {
            case COURSE -> buildCourseContext(requestedLevelId);
            case MODULE -> buildModuleContext(requestedLevelId);
            case SECTION -> buildSectionContext(requestedLevelId);
            case CHAPTER -> buildChapterContext(requestedLevelId);
            case TOPIC -> buildTopicContext(requestedLevelId);
            case CONTENT, VIDEO, QUIZ -> buildContentContext(requestedLevelType, requestedLevelId);
        };
    }

    private SummaryContext buildCourseContext(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        SummaryContext context = new SummaryContext(LevelType.COURSE, course.getId());
        context.title = normalizeTitle(course.getTitle(), "Course #" + course.getId());
        context.courseTitle = context.title;
        context.childParentLevelType = toLevelType(course.getCourseStructure());

        loadCourseParents(context, course);

        for (Topic topic : sortTopics(topicRepository.findByCourseIdOrderByDisplayOrderAsc(courseId), context.parentDescriptors)) {
            registerTopic(context, topic, null);
        }

        for (Topic topic : sortTopics(findTopicsByParentType(context.childParentLevelType, context.parentIds), context.parentDescriptors)) {
            registerTopic(context, topic, context.parentDescriptors.get(resolveParentId(topic)));
        }

        registerContents(context, findContentsByTopicIds(context.topicIds));
        context.parentCount = context.parentIds.size();
        context.topicCount = context.topicDescriptors.size();
        context.contentCount = context.contentDescriptors.size();
        return context;
    }

    private SummaryContext buildModuleContext(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));

        SummaryContext context = new SummaryContext(LevelType.MODULE, module.getId());
        context.title = normalizeTitle(module.getTitle(), "Module #" + module.getId());
        context.courseTitle = resolveCourseTitle(module.getCourse(), context.title);

        ParentDescriptor descriptor = new ParentDescriptor(
                LevelType.MODULE,
                module.getId(),
                context.title,
                safeOrder(module.getDisplayOrder()),
                context.courseTitle
        );
        context.parentDescriptors.put(module.getId(), descriptor);
        context.parentIds.add(module.getId());
        context.parentCount = 1;

        for (Topic topic : sortTopics(topicRepository.findByModuleId(moduleId), context.parentDescriptors)) {
            registerTopic(context, topic, descriptor);
        }

        registerContents(context, findContentsByTopicIds(context.topicIds));
        context.topicCount = context.topicDescriptors.size();
        context.contentCount = context.contentDescriptors.size();
        return context;
    }

    private SummaryContext buildSectionContext(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + sectionId));

        SummaryContext context = new SummaryContext(LevelType.SECTION, section.getId());
        context.title = normalizeTitle(section.getTitle(), "Section #" + section.getId());
        context.courseTitle = resolveCourseTitle(section.getCourse(), context.title);

        ParentDescriptor descriptor = new ParentDescriptor(
                LevelType.SECTION,
                section.getId(),
                context.title,
                safeOrder(section.getDisplayOrder()),
                context.courseTitle
        );
        context.parentDescriptors.put(section.getId(), descriptor);
        context.parentIds.add(section.getId());
        context.parentCount = 1;

        for (Topic topic : sortTopics(topicRepository.findBySectionId(sectionId), context.parentDescriptors)) {
            registerTopic(context, topic, descriptor);
        }

        registerContents(context, findContentsByTopicIds(context.topicIds));
        context.topicCount = context.topicDescriptors.size();
        context.contentCount = context.contentDescriptors.size();
        return context;
    }

    private SummaryContext buildChapterContext(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        SummaryContext context = new SummaryContext(LevelType.CHAPTER, chapter.getId());
        context.title = normalizeTitle(chapter.getTitle(), "Chapter #" + chapter.getId());
        context.courseTitle = resolveCourseTitle(chapter.getCourse(), context.title);

        ParentDescriptor descriptor = new ParentDescriptor(
                LevelType.CHAPTER,
                chapter.getId(),
                context.title,
                safeOrder(chapter.getDisplayOrder()),
                context.courseTitle
        );
        context.parentDescriptors.put(chapter.getId(), descriptor);
        context.parentIds.add(chapter.getId());
        context.parentCount = 1;

        for (Topic topic : sortTopics(topicRepository.findByChapterId(chapterId), context.parentDescriptors)) {
            registerTopic(context, topic, descriptor);
        }

        registerContents(context, findContentsByTopicIds(context.topicIds));
        context.topicCount = context.topicDescriptors.size();
        context.contentCount = context.contentDescriptors.size();
        return context;
    }

    private SummaryContext buildTopicContext(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + topicId));

        SummaryContext context = new SummaryContext(LevelType.TOPIC, topic.getId());
        context.courseTitle = resolveTopicCourseTitle(topic);
        context.title = normalizeTitle(topic.getTitle(), "Topic #" + topic.getId());

        ParentDescriptor parentDescriptor = buildParentDescriptor(topic, context.courseTitle);
        if (parentDescriptor != null) {
            context.parentDescriptors.put(parentDescriptor.id, parentDescriptor);
            context.parentIds.add(parentDescriptor.id);
            context.parentCount = 1;
        }

        registerTopic(context, topic, parentDescriptor);
        registerContents(context, contentRepository.findByTopicIdOrderByDisplayOrderAsc(topicId));
        context.topicCount = context.topicDescriptors.size();
        context.contentCount = context.contentDescriptors.size();
        return context;
    }

    private SummaryContext buildContentContext(LevelType requestedLevelType, Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

        SummaryContext context = new SummaryContext(requestedLevelType, content.getId());
        context.title = normalizeTitle(content.getTitle(), "Content #" + content.getId());

        Topic topic = content.getTopic();
        context.courseTitle = resolveTopicCourseTitle(topic);
        ParentDescriptor parentDescriptor = buildParentDescriptor(topic, context.courseTitle);

        if (parentDescriptor != null) {
            context.parentDescriptors.put(parentDescriptor.id, parentDescriptor);
            context.parentIds.add(parentDescriptor.id);
            context.parentCount = 1;
        }

        registerTopic(context, topic, parentDescriptor);
        registerContent(context, content);
        context.topicCount = context.topicDescriptors.size();
        context.contentCount = context.contentDescriptors.size();
        return context;
    }

    private void loadCourseParents(SummaryContext context, Course course) {
        CourseStructure structure = course.getCourseStructure();
        switch (structure) {
            case MODULE -> moduleRepository.findByCourseIdOrderByDisplayOrderAsc(course.getId())
                    .forEach(module -> registerParent(context, LevelType.MODULE, module.getId(), module.getTitle(), module.getDisplayOrder()));
            case SECTION -> sectionRepository.findByCourseIdOrderByDisplayOrderAsc(course.getId())
                    .forEach(section -> registerParent(context, LevelType.SECTION, section.getId(), section.getTitle(), section.getDisplayOrder()));
            case CHAPTER -> chapterRepository.findByCourseIdOrderByDisplayOrderAsc(course.getId())
                    .forEach(chapter -> registerParent(context, LevelType.CHAPTER, chapter.getId(), chapter.getTitle(), chapter.getDisplayOrder()));
            default -> throw new BadRequestException("Unsupported course structure: " + structure, "UNSUPPORTED_COURSE_STRUCTURE");
        }
    }

    private void registerParent(
            SummaryContext context,
            LevelType levelType,
            Long id,
            String title,
            Integer displayOrder
    ) {
        context.parentIds.add(id);
        context.parentDescriptors.put(id, new ParentDescriptor(
                levelType,
                id,
                normalizeTitle(title, levelType.name() + " #" + id),
                safeOrder(displayOrder),
                context.courseTitle
        ));
    }

    private void registerTopic(SummaryContext context, Topic topic, ParentDescriptor explicitParent) {
        if (topic == null || topic.getId() == null) {
            return;
        }

        ParentDescriptor parentDescriptor = explicitParent != null
                ? explicitParent
                : buildParentDescriptor(topic, context.courseTitle);

        String courseTitle = context.courseTitle == null || context.courseTitle.isBlank()
                ? resolveTopicCourseTitle(topic)
                : context.courseTitle;

        TopicDescriptor descriptor = new TopicDescriptor(
                topic.getId(),
                normalizeTitle(topic.getTitle(), "Topic #" + topic.getId()),
                safeOrder(topic.getDisplayOrder()),
                courseTitle,
                parentDescriptor
        );

        context.topicDescriptors.put(topic.getId(), descriptor);
        if (!context.topicIds.contains(topic.getId())) {
            context.topicIds.add(topic.getId());
        }
    }

    private void registerContents(SummaryContext context, List<Content> contents) {
        for (Content content : sortContents(contents, context.topicDescriptors)) {
            registerContent(context, content);
        }
    }

    private void registerContent(SummaryContext context, Content content) {
        if (content == null || content.getId() == null) {
            return;
        }

        Long topicId = resolveTopicId(content);
        TopicDescriptor topicDescriptor = context.topicDescriptors.get(topicId);

        if (topicDescriptor == null && content.getTopic() != null) {
            registerTopic(context, content.getTopic(), buildParentDescriptor(content.getTopic(), context.courseTitle));
            topicDescriptor = context.topicDescriptors.get(topicId);
        }

        context.contentDescriptors.put(content.getId(), new ContentDescriptor(
                content.getId(),
                normalizeTitle(content.getTitle(), "Content #" + content.getId()),
                safeOrder(content.getDisplayOrder()),
                topicDescriptor
        ));
        if (!context.contentIds.contains(content.getId())) {
            context.contentIds.add(content.getId());
        }
    }

    private List<OrderedNote> collectOrderedNotes(Long authenticatedUserId, SummaryContext context) {
        List<OrderedNote> orderedNotes = new ArrayList<>();

        addOrderedNotes(
                orderedNotes,
                noteRepository.findByUserIdAndLevelTypeAndLevelIdOrderByUpdatedAtDescCreatedAtDesc(
                        authenticatedUserId,
                        context.requestedLevelType,
                        context.requestedLevelId
                ),
                context,
                0
        );

        switch (context.requestedLevelType) {
            case COURSE -> {
                addOrderedNotes(orderedNotes, fetchNotesByIds(authenticatedUserId, context.childParentLevelType, context.parentIds), context, 1);
                addOrderedNotes(orderedNotes, fetchNotesByIds(authenticatedUserId, LevelType.TOPIC, context.topicIds), context, 2);
                addOrderedNotes(orderedNotes, fetchContentNotes(authenticatedUserId, context.contentIds), context, 3);
            }
            case MODULE, SECTION, CHAPTER -> {
                addOrderedNotes(orderedNotes, fetchNotesByIds(authenticatedUserId, LevelType.TOPIC, context.topicIds), context, 1);
                addOrderedNotes(orderedNotes, fetchContentNotes(authenticatedUserId, context.contentIds), context, 2);
            }
            case TOPIC -> addOrderedNotes(orderedNotes, fetchContentNotes(authenticatedUserId, context.contentIds), context, 1);
            case CONTENT, VIDEO, QUIZ -> {
            }
        }

        orderedNotes.sort(Comparator
                .comparingInt((OrderedNote note) -> note.scopeRank)
                .thenComparingInt(note -> note.parentOrder)
                .thenComparingInt(note -> note.topicOrder)
                .thenComparingInt(note -> note.contentOrder)
                .thenComparing(note -> note.videoTimestampSeconds, Comparator.nullsLast(Integer::compareTo))
                .thenComparing((OrderedNote note) -> note.updatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing((OrderedNote note) -> note.createdAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(note -> note.noteId, Comparator.nullsLast(Long::compareTo)));

        return orderedNotes;
    }

    private void addOrderedNotes(
            List<OrderedNote> orderedNotes,
            List<Note> notes,
            SummaryContext context,
            int scopeRank
    ) {
        for (Note note : notes) {
            orderedNotes.add(toOrderedNote(note, context, scopeRank));
        }
    }

    private OrderedNote toOrderedNote(Note note, SummaryContext context, int scopeRank) {
        SourceInfo sourceInfo = resolveSourceInfo(note, context);

        OrderedNote orderedNote = new OrderedNote();
        orderedNote.noteId = note.getId();
        orderedNote.note = note;
        orderedNote.scopeRank = scopeRank;
        orderedNote.parentOrder = sourceInfo.parentOrder;
        orderedNote.topicOrder = sourceInfo.topicOrder;
        orderedNote.contentOrder = sourceInfo.contentOrder;
        orderedNote.videoTimestampSeconds = extractVideoTimestamp(note.getMetadata());
        orderedNote.createdAt = note.getCreatedAt();
        orderedNote.updatedAt = note.getUpdatedAt();
        orderedNote.sourceInfo = sourceInfo;
        return orderedNote;
    }

    private SourceInfo resolveSourceInfo(Note note, SummaryContext context) {
        if (note.getLevelType() == LevelType.COURSE) {
            String courseTitle = normalizeTitle(context.courseTitle, context.title);
            return new SourceInfo(courseTitle, courseTitle, courseTitle, null, null, null, 0, 0, 0);
        }

        if (note.getLevelType() == LevelType.MODULE || note.getLevelType() == LevelType.SECTION || note.getLevelType() == LevelType.CHAPTER) {
            ParentDescriptor descriptor = context.parentDescriptors.get(note.getLevelId());
            if (descriptor != null) {
                return new SourceInfo(
                        descriptor.title,
                        joinPath(descriptor.courseTitle, descriptor.title),
                        descriptor.courseTitle,
                        descriptor.title,
                        null,
                        null,
                        descriptor.displayOrder,
                        0,
                        0
                );
            }
        }

        if (note.getLevelType() == LevelType.TOPIC) {
            TopicDescriptor descriptor = context.topicDescriptors.get(note.getLevelId());
            if (descriptor != null) {
                return new SourceInfo(
                        descriptor.title,
                        joinPath(
                                descriptor.courseTitle,
                                descriptor.parentDescriptor == null ? null : descriptor.parentDescriptor.title,
                                descriptor.title
                        ),
                        descriptor.courseTitle,
                        descriptor.parentDescriptor == null ? null : descriptor.parentDescriptor.title,
                        descriptor.title,
                        null,
                        descriptor.parentDescriptor == null ? -1 : descriptor.parentDescriptor.displayOrder,
                        descriptor.displayOrder,
                        0
                );
            }
        }

        if (CONTENT_LEVEL_TYPES.contains(note.getLevelType())) {
            ContentDescriptor descriptor = context.contentDescriptors.get(note.getLevelId());
            if (descriptor != null) {
                TopicDescriptor topicDescriptor = descriptor.topicDescriptor;
                ParentDescriptor parentDescriptor = topicDescriptor == null ? null : topicDescriptor.parentDescriptor;
                String courseTitle = topicDescriptor == null ? normalizeTitle(context.courseTitle, context.title) : topicDescriptor.courseTitle;

                return new SourceInfo(
                        descriptor.title,
                        joinPath(
                                courseTitle,
                                parentDescriptor == null ? null : parentDescriptor.title,
                                topicDescriptor == null ? null : topicDescriptor.title,
                                descriptor.title
                        ),
                        courseTitle,
                        parentDescriptor == null ? null : parentDescriptor.title,
                        topicDescriptor == null ? null : topicDescriptor.title,
                        descriptor.title,
                        parentDescriptor == null ? -1 : parentDescriptor.displayOrder,
                        topicDescriptor == null ? Integer.MAX_VALUE : topicDescriptor.displayOrder,
                        descriptor.displayOrder
                );
            }
        }

        String fallbackTitle = formatFallbackTitle(note.getLevelType(), note.getLevelId());
        String courseTitle = normalizeTitle(context.courseTitle, context.title);
        return new SourceInfo(
                fallbackTitle,
                joinPath(courseTitle, fallbackTitle),
                courseTitle,
                null,
                null,
                null,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE
        );
    }

    private NoteSummaryResponse buildResponse(Long authenticatedUserId, SummaryContext context, List<OrderedNote> orderedNotes) {
        NoteSummaryResponse response = new NoteSummaryResponse();
        response.setUserId(authenticatedUserId);
        response.setLevelType(context.requestedLevelType);
        response.setLevelId(context.requestedLevelId);
        response.setTitle(context.title);
        response.setNoteCount(orderedNotes.size());
        response.setDirectNoteCount((int) orderedNotes.stream().filter(note -> note.scopeRank == 0).count());
        response.setDescendantNoteCount(Math.max(0, orderedNotes.size() - response.getDirectNoteCount()));
        response.setParentCount(context.parentCount);
        response.setTopicCount(context.topicCount);
        response.setContentCount(context.contentCount);
        response.setLatestNoteAt(
                orderedNotes.stream()
                        .map(note -> note.updatedAt != null ? note.updatedAt : note.createdAt)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null)
        );
        response.setSummaryText(buildSummaryText(context, response, orderedNotes));
        response.setHighlights(
                orderedNotes.stream()
                        .limit(MAX_HIGHLIGHTS)
                        .map(this::toHighlightResponse)
                        .collect(Collectors.toList())
        );
        return response;
    }

    private NoteSummaryHighlightResponse toHighlightResponse(OrderedNote orderedNote) {
        NoteSummaryHighlightResponse response = new NoteSummaryHighlightResponse();
        response.setNoteId(orderedNote.noteId);
        response.setNoteText(cleanNoteText(orderedNote.note.getNoteText()));
        response.setNoteLevelType(orderedNote.note.getLevelType());
        response.setNoteLevelId(orderedNote.note.getLevelId());
        response.setSourceTitle(orderedNote.sourceInfo.sourceTitle);
        response.setSourcePath(orderedNote.sourceInfo.sourcePath);
        response.setCourseTitle(orderedNote.sourceInfo.courseTitle);
        response.setParentTitle(orderedNote.sourceInfo.parentTitle);
        response.setTopicTitle(orderedNote.sourceInfo.topicTitle);
        response.setContentTitle(orderedNote.sourceInfo.contentTitle);
        response.setVideoTimestampSeconds(orderedNote.videoTimestampSeconds);
        response.setCreatedAt(orderedNote.createdAt);
        response.setUpdatedAt(orderedNote.updatedAt);
        return response;
    }

    private String buildSummaryText(SummaryContext context, NoteSummaryResponse response, List<OrderedNote> orderedNotes) {
        String scopeLabel = toScopeLabel(context.requestedLevelType);

        if (orderedNotes.isEmpty()) {
            return switch (context.requestedLevelType) {
                case COURSE -> "No notes saved for this course yet. Summaries will build automatically as you add notes across the course.";
                case MODULE, SECTION, CHAPTER -> "No notes saved for this " + scopeLabel + " yet. Add notes on topics or content to generate a revision summary.";
                case TOPIC -> "No notes saved for this topic yet. Add notes inside the topic content to generate a revision summary here.";
                case CONTENT, VIDEO, QUIZ -> "No notes saved for this content item yet. Your personal notes will appear here once you start annotating.";
            };
        }

        StringBuilder builder = new StringBuilder();
        builder.append(response.getNoteCount())
                .append(response.getNoteCount() == 1 ? " note" : " notes")
                .append(" aggregated for this ")
                .append(scopeLabel);

        List<String> coverage = new ArrayList<>();
        if (context.requestedLevelType == LevelType.COURSE && response.getParentCount() > 0 && context.childParentLevelType != null) {
            coverage.add(response.getParentCount() + " " + pluralize(toScopeLabel(context.childParentLevelType), response.getParentCount()));
        }
        if (context.requestedLevelType != LevelType.CONTENT && context.requestedLevelType != LevelType.VIDEO && context.requestedLevelType != LevelType.QUIZ && response.getTopicCount() > 0) {
            coverage.add(response.getTopicCount() + " " + pluralize("topic", response.getTopicCount()));
        }
        if (response.getContentCount() > 0) {
            coverage.add(response.getContentCount() + " " + pluralize("content item", response.getContentCount()));
        }
        if (!coverage.isEmpty()) {
            builder.append(" across ").append(String.join(", ", coverage));
        }

        if (response.getDescendantNoteCount() != null && response.getDescendantNoteCount() > 0) {
            builder.append(". ")
                    .append(response.getDescendantNoteCount())
                    .append(response.getDescendantNoteCount() == 1 ? " note rolls up" : " notes roll up")
                    .append(" from child levels");
        }

        List<String> snippets = orderedNotes.stream()
                .map(note -> truncateSnippet(cleanNoteText(note.note.getNoteText())))
                .filter(snippet -> !snippet.isBlank())
                .distinct()
                .limit(MAX_SUMMARY_SNIPPETS)
                .collect(Collectors.toList());

        if (!snippets.isEmpty()) {
            builder.append(". Quick revision: ").append(String.join("; ", snippets));
        }

        return builder.toString();
    }

    private List<Note> fetchNotesByIds(Long userId, LevelType levelType, List<Long> levelIds) {
        if (levelType == null || levelIds.isEmpty()) {
            return Collections.emptyList();
        }
        return noteRepository.findByUserIdAndLevelTypeAndLevelIdIn(userId, levelType, levelIds);
    }

    private List<Note> fetchContentNotes(Long userId, List<Long> contentIds) {
        if (contentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return noteRepository.findByUserIdAndLevelTypeInAndLevelIdIn(userId, CONTENT_LEVEL_TYPES, contentIds);
    }

    private List<Topic> findTopicsByParentType(LevelType parentLevelType, Collection<Long> parentIds) {
        if (parentLevelType == null || parentIds == null || parentIds.isEmpty()) {
            return Collections.emptyList();
        }

        return switch (parentLevelType) {
            case MODULE -> topicRepository.findByModuleIdIn(parentIds);
            case SECTION -> topicRepository.findBySectionIdIn(parentIds);
            case CHAPTER -> topicRepository.findByChapterIdIn(parentIds);
            default -> Collections.emptyList();
        };
    }

    private List<Content> findContentsByTopicIds(Collection<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return Collections.emptyList();
        }
        return contentRepository.findByTopicIdIn(topicIds);
    }

    private List<Topic> sortTopics(List<Topic> topics, Map<Long, ParentDescriptor> parentDescriptors) {
        return topics.stream()
                .sorted(Comparator
                        .comparingInt((Topic topic) -> resolveParentOrder(topic, parentDescriptors))
                        .thenComparingInt(topic -> safeOrder(topic.getDisplayOrder()))
                        .thenComparing(topic -> normalizeTitle(topic.getTitle(), "")))
                .collect(Collectors.toList());
    }

    private List<Content> sortContents(List<Content> contents, Map<Long, TopicDescriptor> topicDescriptors) {
        return contents.stream()
                .sorted(Comparator
                        .comparingInt((Content content) -> resolveTopicOrder(content, topicDescriptors))
                        .thenComparingInt(content -> safeOrder(content.getDisplayOrder()))
                        .thenComparing(content -> normalizeTitle(content.getTitle(), "")))
                .collect(Collectors.toList());
    }

    private int resolveParentOrder(Topic topic, Map<Long, ParentDescriptor> parentDescriptors) {
        Long parentId = resolveParentId(topic);
        if (parentId == null) {
            return -1;
        }
        ParentDescriptor descriptor = parentDescriptors.get(parentId);
        return descriptor == null ? Integer.MAX_VALUE : descriptor.displayOrder;
    }

    private int resolveTopicOrder(Content content, Map<Long, TopicDescriptor> topicDescriptors) {
        TopicDescriptor descriptor = topicDescriptors.get(resolveTopicId(content));
        return descriptor == null ? Integer.MAX_VALUE : descriptor.displayOrder;
    }

    private ParentDescriptor buildParentDescriptor(Topic topic, String courseTitle) {
        if (topic == null) {
            return null;
        }

        if (topic.getModule() != null && topic.getModule().getId() != null) {
            return new ParentDescriptor(
                    LevelType.MODULE,
                    topic.getModule().getId(),
                    normalizeTitle(topic.getModule().getTitle(), "Module #" + topic.getModule().getId()),
                    safeOrder(topic.getModule().getDisplayOrder()),
                    courseTitle
            );
        }
        if (topic.getSection() != null && topic.getSection().getId() != null) {
            return new ParentDescriptor(
                    LevelType.SECTION,
                    topic.getSection().getId(),
                    normalizeTitle(topic.getSection().getTitle(), "Section #" + topic.getSection().getId()),
                    safeOrder(topic.getSection().getDisplayOrder()),
                    courseTitle
            );
        }
        if (topic.getChapter() != null && topic.getChapter().getId() != null) {
            return new ParentDescriptor(
                    LevelType.CHAPTER,
                    topic.getChapter().getId(),
                    normalizeTitle(topic.getChapter().getTitle(), "Chapter #" + topic.getChapter().getId()),
                    safeOrder(topic.getChapter().getDisplayOrder()),
                    courseTitle
            );
        }
        return null;
    }

    private String resolveTopicCourseTitle(Topic topic) {
        if (topic == null) {
            return "";
        }
        if (topic.getCourse() != null && topic.getCourse().getId() != null) {
            return normalizeTitle(topic.getCourse().getTitle(), "Course #" + topic.getCourse().getId());
        }
        if (topic.getModule() != null && topic.getModule().getCourse() != null && topic.getModule().getCourse().getId() != null) {
            return normalizeTitle(topic.getModule().getCourse().getTitle(), "Course #" + topic.getModule().getCourse().getId());
        }
        if (topic.getSection() != null && topic.getSection().getCourse() != null && topic.getSection().getCourse().getId() != null) {
            return normalizeTitle(topic.getSection().getCourse().getTitle(), "Course #" + topic.getSection().getCourse().getId());
        }
        if (topic.getChapter() != null && topic.getChapter().getCourse() != null && topic.getChapter().getCourse().getId() != null) {
            return normalizeTitle(topic.getChapter().getCourse().getTitle(), "Course #" + topic.getChapter().getCourse().getId());
        }
        return "";
    }

    private String resolveCourseTitle(Course course, String fallbackTitle) {
        if (course == null || course.getId() == null) {
            return fallbackTitle == null ? "" : fallbackTitle;
        }
        return normalizeTitle(course.getTitle(), "Course #" + course.getId());
    }

    private Long resolveParentId(Topic topic) {
        if (topic == null) {
            return null;
        }
        if (topic.getModule() != null && topic.getModule().getId() != null) {
            return topic.getModule().getId();
        }
        if (topic.getSection() != null && topic.getSection().getId() != null) {
            return topic.getSection().getId();
        }
        if (topic.getChapter() != null && topic.getChapter().getId() != null) {
            return topic.getChapter().getId();
        }
        return null;
    }

    private Long resolveTopicId(Content content) {
        return content != null && content.getTopic() != null ? content.getTopic().getId() : null;
    }

    private Integer extractVideoTimestamp(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        Integer directTimestamp = coerceInteger(metadata.get("videoTimestamp"));
        if (directTimestamp != null) {
            return directTimestamp;
        }

        Object nestedVideo = metadata.get("video");
        if (nestedVideo instanceof Map<?, ?> nestedMap) {
            return coerceInteger(nestedMap.get("timestamp"));
        }

        return null;
    }

    private Integer coerceInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }

        String normalized = String.valueOf(value).trim();
        if (normalized.isEmpty()) {
            return null;
        }

        try {
            return (int) Math.round(Double.parseDouble(normalized));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void validateUserAccess(Long requestedUserId, Long authenticatedUserId) {
        if (requestedUserId == null || authenticatedUserId == null) {
            throw new BadRequestException("userId is required", "MISSING_USER_ID");
        }
        if (!requestedUserId.equals(authenticatedUserId)) {
            throw new ForbiddenException("Users can only access their own notes");
        }
    }

    private LevelType toLevelType(CourseStructure structure) {
        return switch (structure) {
            case MODULE -> LevelType.MODULE;
            case SECTION -> LevelType.SECTION;
            case CHAPTER -> LevelType.CHAPTER;
        };
    }

    private String toScopeLabel(LevelType levelType) {
        return switch (levelType) {
            case COURSE -> "course";
            case MODULE -> "module";
            case SECTION -> "section";
            case CHAPTER -> "chapter";
            case TOPIC -> "topic";
            case CONTENT, VIDEO, QUIZ -> "content item";
        };
    }

    private String pluralize(String label, int count) {
        if (count == 1) {
            return label;
        }
        return switch (label) {
            case "course" -> "courses";
            case "module" -> "modules";
            case "section" -> "sections";
            case "chapter" -> "chapters";
            case "topic" -> "topics";
            case "content item" -> "content items";
            default -> label + "s";
        };
    }

    private int safeOrder(Integer value) {
        return value == null ? Integer.MAX_VALUE : value;
    }

    private String normalizeTitle(String value, String fallback) {
        String normalized = value == null ? "" : value.trim();
        return normalized.isEmpty() ? fallback : normalized;
    }

    private String cleanNoteText(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private String truncateSnippet(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.length() <= 110) {
            return value;
        }
        return value.substring(0, 107).trim() + "...";
    }

    private String formatFallbackTitle(LevelType levelType, Long levelId) {
        String label = levelType.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(label.charAt(0)) + label.substring(1) + " #" + levelId;
    }

    private String joinPath(String... parts) {
        return Arrays.stream(parts)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining(" > "));
    }

    private static final class SummaryContext {
        private final LevelType requestedLevelType;
        private final Long requestedLevelId;
        private String title;
        private String courseTitle;
        private LevelType childParentLevelType;
        private int parentCount;
        private int topicCount;
        private int contentCount;
        private final List<Long> parentIds = new ArrayList<>();
        private final List<Long> topicIds = new ArrayList<>();
        private final List<Long> contentIds = new ArrayList<>();
        private final Map<Long, ParentDescriptor> parentDescriptors = new LinkedHashMap<>();
        private final Map<Long, TopicDescriptor> topicDescriptors = new LinkedHashMap<>();
        private final Map<Long, ContentDescriptor> contentDescriptors = new LinkedHashMap<>();

        private SummaryContext(LevelType requestedLevelType, Long requestedLevelId) {
            this.requestedLevelType = requestedLevelType;
            this.requestedLevelId = requestedLevelId;
        }
    }

    private static final class ParentDescriptor {
        private final LevelType levelType;
        private final Long id;
        private final String title;
        private final int displayOrder;
        private final String courseTitle;

        private ParentDescriptor(LevelType levelType, Long id, String title, int displayOrder, String courseTitle) {
            this.levelType = levelType;
            this.id = id;
            this.title = title;
            this.displayOrder = displayOrder;
            this.courseTitle = courseTitle;
        }
    }

    private static final class TopicDescriptor {
        private final Long id;
        private final String title;
        private final int displayOrder;
        private final String courseTitle;
        private final ParentDescriptor parentDescriptor;

        private TopicDescriptor(Long id, String title, int displayOrder, String courseTitle, ParentDescriptor parentDescriptor) {
            this.id = id;
            this.title = title;
            this.displayOrder = displayOrder;
            this.courseTitle = courseTitle;
            this.parentDescriptor = parentDescriptor;
        }
    }

    private static final class ContentDescriptor {
        private final Long id;
        private final String title;
        private final int displayOrder;
        private final TopicDescriptor topicDescriptor;

        private ContentDescriptor(Long id, String title, int displayOrder, TopicDescriptor topicDescriptor) {
            this.id = id;
            this.title = title;
            this.displayOrder = displayOrder;
            this.topicDescriptor = topicDescriptor;
        }
    }

    private static final class OrderedNote {
        private Long noteId;
        private Note note;
        private int scopeRank;
        private int parentOrder;
        private int topicOrder;
        private int contentOrder;
        private Integer videoTimestampSeconds;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private SourceInfo sourceInfo;
    }

    private static final class SourceInfo {
        private final String sourceTitle;
        private final String sourcePath;
        private final String courseTitle;
        private final String parentTitle;
        private final String topicTitle;
        private final String contentTitle;
        private final int parentOrder;
        private final int topicOrder;
        private final int contentOrder;

        private SourceInfo(
                String sourceTitle,
                String sourcePath,
                String courseTitle,
                String parentTitle,
                String topicTitle,
                String contentTitle,
                int parentOrder,
                int topicOrder,
                int contentOrder
        ) {
            this.sourceTitle = sourceTitle;
            this.sourcePath = sourcePath;
            this.courseTitle = courseTitle;
            this.parentTitle = parentTitle;
            this.topicTitle = topicTitle;
            this.contentTitle = contentTitle;
            this.parentOrder = parentOrder;
            this.topicOrder = topicOrder;
            this.contentOrder = contentOrder;
        }
    }
}
