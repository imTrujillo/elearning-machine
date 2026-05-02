package com.learning_engine.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.learning_engine.dto.response.CategorySummaryResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CourseResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("imageUrl") String imageUrl,
        @JsonProperty("instructor") String instructor,
        @JsonProperty("category") CategorySummaryResponse category,
        @JsonProperty("slug") String slug,
        @JsonProperty("price") BigDecimal price,
        @JsonProperty("wooProductId") Long wooProductId,
        @JsonProperty("wordpressPostId") Long wordpressPostId,
        @JsonProperty("active") Boolean active,
        @JsonProperty("totalModules") Integer totalModules,
        @JsonProperty("totalLessons") Integer totalLessons,
        @JsonProperty("createdAt") LocalDateTime createdAt
) {}