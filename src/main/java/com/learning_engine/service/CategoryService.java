package com.learning_engine.service;

import com.learning_engine.dto.request.CategoryRequest;
import com.learning_engine.dto.response.CategoryResponse;
import com.learning_engine.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    CategoryResponse create (CategoryRequest request);
    PagedResponse<CategoryResponse> findAll(Pageable pageable);
    CategoryResponse findBySlug(String slug);
    CategoryResponse update(Long id, CategoryRequest request);
    void delete(Long id);
}
