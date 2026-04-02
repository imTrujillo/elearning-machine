package com.learning_engine.dto.response;

import java.util.List;

public record ModuleResponse(
        Long id,
        String title,
        String description,
        Integer orderIndex,
        List<LessonResponse> lessons
) {
}

