package com.learning_engine.service.impl;

import com.learning_engine.dto.request.CategoryRequest;
import com.learning_engine.dto.response.CategoryResponse;
import com.learning_engine.dto.response.PagedResponse;
import com.learning_engine.entity.Category;
import com.learning_engine.mapper.CategoryMapper;
import com.learning_engine.repository.CategoryRepository;
import com.learning_engine.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new RuntimeException("Ya existe una categoría con el slug: " + request.slug());
        }

        return categoryMapper.toResponse(categoryRepository.save(categoryMapper.toEntity(request)));
    }

    @Override
    @Cacheable(value = "categories", key = "'page-' + #pageable.pageNumber")
    public PagedResponse<CategoryResponse> findAll(Pageable pageable) {
        Page<Category> page = categoryRepository.findAll(pageable);
        Page<CategoryResponse> mapped = page.map(categoryMapper::toResponse);
        return PagedResponse.of(mapped);
    }

    @Override
    @Cacheable(value = "categories", key = "#slug")
    public CategoryResponse findBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + slug));
        return categoryMapper.toResponse(category);
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + id));

        return categoryMapper.toResponse(categoryRepository.save(categoryMapper.updateEntity(category, request)));
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada: " + id);
        }
        categoryRepository.deleteById(id);
    }
}