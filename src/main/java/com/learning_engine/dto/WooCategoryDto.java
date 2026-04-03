package com.learning_engine.dto;

public record WooCategoryDto(
        Long id,
        String name,
        String slug,
        String description,
        WooCategoryImageDto image
) {
    public record WooCategoryImageDto(String src) {}
}