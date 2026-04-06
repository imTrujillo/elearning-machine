package com.learning_engine.dto.request;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourseRequest {
    private String title;
    private String description;
    private String imageUrl;
    private String instructor;
    private Long categoryId;
    private BigDecimal price;
    private boolean active;
}
