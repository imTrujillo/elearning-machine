package com.learning_engine.dto.request;

import jakarta.validation.constraints.NotNull;

public record LessonCompleteRequest(
        @NotNull Long lessonId,
        @NotNull Long studentId
) {
}
