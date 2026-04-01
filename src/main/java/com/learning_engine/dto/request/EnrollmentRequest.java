package com.learning_engine.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest (
        @NotNull Long courseId,
        @NotNull Long wooOrderId,
        @NotBlank Long studentId
) {
}
