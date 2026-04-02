package com.learning_engine.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record CourseResponse(
        Long id,
        String title,
        String description,
        String imageUrl,
        String instructor,
        CategorySummaryResponse category,
        String slug,
        BigDecimal price,
        Long wooProductId,
        Long wordpressPostId,
        Boolean active,
        Integer totalModules,
        Integer totalLessons,
        LocalDateTime createdAt
) {
}
