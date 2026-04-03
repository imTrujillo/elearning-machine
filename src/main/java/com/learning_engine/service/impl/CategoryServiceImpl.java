package com.learning_engine.service.impl;

import com.learning_engine.dto.WooCategoryDto;
import com.learning_engine.dto.request.CategoryRequest;
import com.learning_engine.dto.response.CategoryResponse;
import com.learning_engine.entity.Category;
import com.learning_engine.repository.CategoryRepository;
import com.learning_engine.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Qualifier("wooWebClient")
    private final WebClient wooWebClient;

    @Override
    public List<CategoryResponse> syncFromWooCommerce() {
        List<WooCategoryDto> wooCategories = wooWebClient.get()
                .uri("/wp-json/wc/v3/products/categories?per_page=100&hide_empty=false")
                .retrieve()
                .bodyToFlux(WooCategoryDto.class)
                .collectList()
                .block();

        if (wooCategories == null || wooCategories.isEmpty()) return List.of();

        List<Category> saved = wooCategories.stream()
                .filter(c -> !"uncategorized".equals(c.slug()))  // filtra la default
                .map(woo -> {
                    Category cat = categoryRepository.findBySlug(woo.slug())
                            .orElse(new Category());

                    cat.setName(woo.name());
                    cat.setSlug(woo.slug());
                    cat.setDescription(woo.description());

                    // imagen si existe
                    if (woo.image() != null) {
                        cat.setImageUrl(woo.image().src());
                    }

                    return categoryRepository.save(cat);
                }).toList();

        return saved.stream().map(this::toResponse).toList();
    }

    @Override
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new RuntimeException("Ya existe una categoría con el slug: " + request.slug());
        }

        Category category = Category.builder()
                .name(request.name())
                .slug(request.slug())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse findBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + slug));
        return toResponse(category);
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + id));

        category.setName(request.name());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        category.setImageUrl(request.imageUrl());

        return toResponse(categoryRepository.save(category));
    }

    @Override
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getSlug(),
                c.getDescription(),
                c.getImageUrl(),
                categoryRepository.countCoursesBySlug(c.getSlug()),
                c.getCreatedAt()
        );
    }
}
