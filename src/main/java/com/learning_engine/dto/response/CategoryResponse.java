package com.learning_engine.dto.response;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        String imageUrl,
        Integer totalCourses,
        LocalDateTime createdAt
) {}
