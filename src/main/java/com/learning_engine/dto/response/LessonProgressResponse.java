package com.learning_engine.dto.response;

import java.time.LocalDateTime;

public record LessonProgressResponse(
        Long lessonId,
        String lessonTitle,
        Boolean completed,
        LocalDateTime completedAt,
        Boolean moduleCompleted,
        String moduleTitle
) { }
