package com.learning_engine.dto.response;

public record LessonResponse(
        Long id,
        String title,
        String content,
        String videoUrl,
        Integer durationMinutes,
        Integer orderIndex,
        Boolean freePreview,
        Boolean completed,
        Boolean canAccess
) { }
