package com.learning_engine.dto.request;
import lombok.Data;

@Data
public class ModuleRequest {
    private String title;
    private String description;
    private Integer orderIndex;
}
