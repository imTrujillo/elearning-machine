package com.learning_engine.mapper;

import com.learning_engine.dto.request.LessonRequest;
import com.learning_engine.dto.response.LessonResponse;
import com.learning_engine.entity.Lesson;
import org.springframework.stereotype.Component;

@Component
public class LessonMapper {

    public LessonResponse toResponse(Lesson lesson) {
        return toResponse(lesson, false);
    }

    public LessonResponse toResponse(Lesson lesson, boolean completed) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getContent(),
                lesson.getVideoUrl(),
                lesson.getDurationMinutes(),
                lesson.getOrderIndex(),
                lesson.getFreePreview() != null ? lesson.getFreePreview() : false,
                completed,
                true
        );
    }

    // Used by ModuleMapper: respects access control
    public LessonResponse toResponse(Lesson lesson, boolean canAccess, boolean completed) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                canAccess ? lesson.getContent() : null,
                canAccess ? lesson.getVideoUrl() : null,
                lesson.getDurationMinutes(),
                lesson.getOrderIndex(),
                lesson.getFreePreview() != null ? lesson.getFreePreview() : false,
                completed,
                canAccess
        );
    }

    public Lesson toEntity(LessonRequest request) {
        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setDurationMinutes(request.getDurationMinutes());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setFreePreview(request.isFreePreview());
        return lesson;
    }

    public Lesson updateEntity(Lesson lesson, LessonRequest request) {
        if (request.getTitle() != null) lesson.setTitle(request.getTitle());
        if (request.getContent() != null) lesson.setContent(request.getContent());
        if (request.getVideoUrl() != null) lesson.setVideoUrl(request.getVideoUrl());
        if (request.getDurationMinutes() != null) lesson.setDurationMinutes(request.getDurationMinutes());
        if (request.getOrderIndex() != null) lesson.setOrderIndex(request.getOrderIndex());
        return lesson;
    }
}
