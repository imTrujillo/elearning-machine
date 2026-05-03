package com.learning_engine.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest(
        @NotNull(message = "El curso es obligatorio")
        Long courseId,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Email inválido")
        String studentEmail
) {}