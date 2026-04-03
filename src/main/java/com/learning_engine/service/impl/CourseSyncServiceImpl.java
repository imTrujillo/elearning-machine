package com.learning_engine.service.impl;

import com.learning_engine.dto.WooProductDto;
import com.learning_engine.dto.response.CategorySummaryResponse;
import com.learning_engine.dto.response.CourseResponse;
import com.learning_engine.entity.Category;
import com.learning_engine.entity.Course;
import com.learning_engine.repository.CategoryRepository;
import com.learning_engine.repository.CourseRepository;
import com.learning_engine.service.CourseSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CourseSyncServiceImpl implements CourseSyncService {

    private final WebClient webClient;
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;

    public CourseSyncServiceImpl(
            @Qualifier("wooWebClient") WebClient webClient,
            CourseRepository courseRepository,
            CategoryRepository categoryRepository) {
        this.webClient = webClient;
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @CacheEvict(value = "courses", allEntries = true) // 🔥 limpiar cache
    public List<CourseResponse> syncCourses() {

        List<WooProductDto> products = webClient.get()
                .uri("/wp-json/wc/v3/products")
                .retrieve()
                .bodyToFlux(WooProductDto.class)
                .collectList()
                .block();

        List<CourseResponse> result = new ArrayList<>();

        if (products == null) return result;

        for (WooProductDto product : products) {

            // 🔥 evitar duplicados
            Optional<Course> existing = courseRepository.findByWooProductId(product.id());
            if (existing.isPresent()) continue;

            Course course = new Course();

            // 📌 datos principales
            course.setTitle(product.name());
            course.setDescription(product.name());
            course.setInstructor("Admin");

            // 📌 precio (FIX null-safe)
            String price = product.regular_price();
            course.setPrice(new BigDecimal(
                    (price == null || price.isEmpty()) ? "0" : price
            ));

            // 📌 IDs externos
            course.setWooProductId(product.id());
            course.setSlug(
                    product.name() != null
                            ? product.name().toLowerCase().replace(" ", "-")
                            : "course-" + product.id()
            );
            course.setActive(true);

            // 📌 categoría
            String categoryName = (product.categories() == null || product.categories().isEmpty())
                    ? "General"
                    : product.categories().get(0).name();

            Category category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> {
                        Category newCat = new Category();
                        newCat.setName(categoryName);
                        newCat.setSlug(categoryName.toLowerCase().replace(" ", "-"));
                        return categoryRepository.save(newCat);
                    });

            course.setCategory(category);

            // 💾 guardar
            Course saved = courseRepository.save(course);

            // ⚠️ evitar posibles NPE en modules
            int totalModules = saved.getCourseModules() != null
                    ? saved.getCourseModules().size()
                    : 0;

            CourseResponse response = new CourseResponse(
                    saved.getId(),
                    saved.getTitle(),
                    saved.getDescription(),
                    saved.getImageUrl(),
                    saved.getInstructor(),
                    new CategorySummaryResponse(
                            category.getId(),
                            category.getName(),
                            category.getSlug()
                    ),
                    saved.getSlug(),
                    saved.getPrice(),
                    saved.getWooProductId(),
                    saved.getWordpressPostId(),
                    saved.getActive(),
                    totalModules,
                    0,
                    saved.getCreatedAt()
            );

            result.add(response);
        }

        return result;
    }
}