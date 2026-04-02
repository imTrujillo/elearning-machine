package com.learning_engine.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotBlank(message = "El slug es obligatorio")
        String slug,

        String description,

        String imageUrl
) {}
