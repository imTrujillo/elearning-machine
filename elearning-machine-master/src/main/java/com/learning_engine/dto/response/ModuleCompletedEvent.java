package com.learning_engine.dto.response;

import java.time.LocalDateTime;

public record ModuleCompletedEvent(
        Long moduleId,
        String moduleTitle,
        Long courseId,
        Long studentId,
        String studentEmail,
        LocalDateTime completedAt
) {}