package com.learning_engine.dto.request;
import lombok.Data;

@Data
public class LessonRequest {
    private String title;
    private String content;
    private String videoUrl;
    private Integer durationMinutes;
    private Integer orderIndex;
    private boolean freePreview;
}