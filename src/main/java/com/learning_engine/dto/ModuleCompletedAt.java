package com.learning_engine.dto;

import java.time.LocalDateTime;

public record ModuleCompletedAt(
        Long enrollmentId,
        Long moduleId,
        String moduleTitle,
        Long courseId,
        String studentEmail,
        int completionPercent,
        LocalDateTime completedAt
) {}