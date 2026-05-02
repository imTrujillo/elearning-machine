package com.learning_engine.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record CategoryResponse(
        @JsonProperty("id")  Long id,
        @JsonProperty("name") String name,
        @JsonProperty("slug") String slug,
        @JsonProperty("description") String description,
        @JsonProperty("imageUrl") String imageUrl,
        @JsonProperty("totalCourses") Integer totalCourses,
        @JsonProperty("createdAt") LocalDateTime createdAt
) {}
