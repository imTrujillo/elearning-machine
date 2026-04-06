package com.learning_engine.dto;

import java.time.LocalDateTime;

public record ModuleCompletedAt(
        Long moduleId,
        String moduleTitle,
        Long courseId,
        Long studentId,
        String studentEmail,
        LocalDateTime completedAt
) {}