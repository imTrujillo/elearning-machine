package com.learning_engine.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest (
        @NotNull(message = "El curso es obligatorio")
        Long courseId,

        Long wooOrderId,

        @NotNull(message = "El estudiante es obligatorio")
        Long studentId
) {
}
