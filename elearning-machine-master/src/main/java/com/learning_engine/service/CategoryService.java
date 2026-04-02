package com.learning_engine.service;

import com.learning_engine.dto.request.CategoryRequest;
import com.learning_engine.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse create (CategoryRequest request);
    List<CategoryResponse> findAll();
    CategoryResponse findBySlug(String slug);
    CategoryResponse update(Long id, CategoryRequest request);
    void delete(Long id);
}
