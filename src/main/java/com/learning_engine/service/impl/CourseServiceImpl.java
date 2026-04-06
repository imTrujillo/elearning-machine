package com.learning_engine.service.impl;

import com.learning_engine.dto.WooProductDto;
import com.learning_engine.dto.WordpressPostDto;
import com.learning_engine.dto.request.CourseRequest;
import com.learning_engine.dto.response.CategorySummaryResponse;
import com.learning_engine.dto.response.CourseResponse;
import com.learning_engine.entity.Category;
import com.learning_engine.entity.Course;
import com.learning_engine.repository.CategoryRepository;
import com.learning_engine.repository.CourseRepository;
import com.learning_engine.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;

    @Qualifier("wordpressWebClient")
    private final WebClient wordpressWebClient;

    @Qualifier("wooWebClient")
    private final WebClient wooWebClient;

    @Override
    @Cacheable(value = "courses", key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CourseResponse> findAll(Pageable pageable) {
        return courseRepository.findByActiveTrue(pageable)
                .map(this::toResponse);
    }

    @Override
    @Cacheable(value = "courses", key = "#slug + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CourseResponse> findByCategory(String slug, Pageable pageable) {
        return courseRepository.findByCategorySlugAndActiveTrue(slug, pageable)
                .map(this::toResponse);
    }

    @Override
    public CourseResponse findById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado: " + id));
        return toResponse(course);
    }

    @Override
    @CacheEvict(value = "courses", allEntries = true)
    public List<CourseResponse> syncFromWordpress() {
        List<WordpressPostDto> posts = wordpressWebClient.get()
                .uri("/wp-json/wp/v2/posts?per_page=100&_embed")
                .retrieve()
                .bodyToFlux(WordpressPostDto.class)
                .collectList()
                .block();

        if (posts == null || posts.isEmpty()) return List.of();

        List<WooProductDto> wooProducts = wooWebClient.get()
                .uri("/wp-json/wc/v3/products?per_page=100")
                .retrieve()
                .bodyToFlux(WooProductDto.class)
                .collectList()
                .block();

        Map<String, WooProductDto> wooByName = wooProducts == null
                ? Map.of()
                : wooProducts.stream()
                .collect(Collectors.toMap(
                        p -> p.name() != null ? p.name().toLowerCase() : "",
                        p -> p,
                        (a, b) -> a
                ));

        List<Course> saved = posts.stream().map(post -> {
            Course course = courseRepository
                    .findByWordpressPostId(post.id())
                    .orElse(new Course());

            course.setWordpressPostId(post.id());
            course.setTitle(post.title().rendered());
            course.setDescription(post.content().rendered());
            course.setImageUrl(post.jetpack_featured_media_url());
            course.setSlug(post.slug());
            course.setActive(true);

            WooProductDto woo = wooByName.get(
                    post.title().rendered() != null
                            ? post.title().rendered().toLowerCase()
                            : ""
            );

            if (woo != null) {
                course.setWooProductId(woo.id());

                String price = woo.regular_price();
                course.setPrice(new BigDecimal(
                        (price == null || price.isEmpty()) ? "0" : price
                ));

                if (woo.categories() != null && !woo.categories().isEmpty()) {
                    String catSlug = woo.categories().get(0).slug();
                    categoryRepository.findBySlug(catSlug)
                            .ifPresent(course::setCategory);
                }
            }

            return courseRepository.save(course);
        }).toList();

        return saved.stream().map(this::toResponse).toList();
    }

    // --- NUEVOS MÉTODOS CRUD ---

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseResponse createCourse(CourseRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setImageUrl(request.getImageUrl());
        course.setInstructor(request.getInstructor());
        course.setCategory(category);
        course.setPrice(request.getPrice());
        course.setActive(request.isActive());

        // Generamos un slug simple
        course.setSlug(request.getTitle().toLowerCase().replace(" ", "-"));

        return toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            course.setCategory(category);
        }

        if (request.getTitle() != null) course.setTitle(request.getTitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getPrice() != null) course.setPrice(request.getPrice());
        course.setActive(request.isActive());

        return toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found");
        }
        courseRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseResponse assignCategory(Long courseId, String slug) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + slug));

        course.setCategory(category);

        return toResponse(courseRepository.save(course));
    }

    // ✅ MAPPER
    public CourseResponse toResponse(Course c) {
        CategorySummaryResponse catSummary = c.getCategory() != null
                ? new CategorySummaryResponse(
                c.getCategory().getId(),
                c.getCategory().getName(),
                c.getCategory().getSlug())
                : null;

        int totalModules = c.getCourseModules() != null ? c.getCourseModules().size() : 0;
        int totalLessons = c.getCourseModules() != null
                ? c.getCourseModules().stream()
                .mapToInt(m -> m.getLessons() != null ? m.getLessons().size() : 0)
                .sum()
                : 0;

        return new CourseResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getImageUrl(),
                c.getInstructor(),
                catSummary,
                c.getSlug(),
                c.getPrice(),
                c.getWooProductId(),
                c.getWordpressPostId(),
                c.getActive(),
                totalModules,
                totalLessons,
                c.getCreatedAt()
        );
    }
}