package com.learning_engine.mapper;

import com.learning_engine.dto.request.CategoryRequest;
import com.learning_engine.dto.response.CategoryResponse;
import com.learning_engine.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryResponse toResponse(Category category){
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getImageUrl(),
                category.getCourses() != null ? category.getCourses().size() : 0,
                category.getCreatedAt()
        );
    }

    public Category toEntity(CategoryRequest request) {
        Category entity = new Category();

        entity.setName(request.name());
        entity.setSlug(request.slug());
        entity.setDescription(request.description());
        entity.setImageUrl(request.imageUrl());

        return entity;
    }

    public Category updateEntity(Category category, CategoryRequest request) {
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        category.setImageUrl(request.imageUrl());

        return category;
    }
}
