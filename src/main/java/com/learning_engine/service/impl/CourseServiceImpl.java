package com.learning_engine.service.impl;

import com.learning_engine.dto.WooProductDto;
import com.learning_engine.dto.WordpressPostDto;
import com.learning_engine.dto.request.CourseRequest;
import com.learning_engine.dto.response.CourseResponse;
import com.learning_engine.dto.response.PagedResponse;
import com.learning_engine.entity.Category;
import com.learning_engine.entity.Course;
import com.learning_engine.mapper.CourseMapper;
import com.learning_engine.repository.CategoryRepository;
import com.learning_engine.repository.CourseRepository;
import com.learning_engine.integration.WordpressRestUri;
import com.learning_engine.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final CourseMapper courseMapper;

    @Qualifier("wordpressWebClient")
    private final WebClient wordpressWebClient;

    @Qualifier("wooWebClient")
    private final WebClient wooWebClient;

    @Value("${wordpress.plain-rest:true}")
    private boolean wordpressPlainRest;

    @Override
    @Cacheable(value = "courses", key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PagedResponse<CourseResponse> findAll(Pageable pageable) {
        return PagedResponse.of(courseRepository.findByActiveTrue(pageable)
                .map(courseMapper::toResponse));
    }

    @Override
    @Cacheable(value = "courses", key = "#slug + '_' + #pageable.pageNumber")
    public PagedResponse<CourseResponse> findByCategory(String slug, Pageable pageable) {
        return PagedResponse.of(courseRepository.findByCategorySlugAndActiveTrue(slug, pageable)
                .map(courseMapper::toResponse));
    }

    @Override
    @Cacheable(value = "courses", key = "'id:' + #id")
    public CourseResponse findById(Long id) {
        return courseMapper.toResponse(courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado: " + id)));
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseResponse createCourse(CourseRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return courseMapper.toResponse(courseRepository.save(courseMapper.toEntity(request, category)));
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Category category = request.getCategoryId() != null
                ? categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"))
                : null;

        return courseMapper.toResponse(courseRepository.save(courseMapper.updateEntity(course, request, category)));
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
        return courseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) throw new RuntimeException("Course not found");
        courseRepository.deleteById(id);
    }

    private List<WooProductDto> fetchWooProducts() {
        try {
            return wooWebClient.get()
                    .uri(WordpressRestUri.resolve("/wp-json/wc/v3/products?per_page=100", wordpressPlainRest))
                    .retrieve()
                    .bodyToFlux(WooProductDto.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException.Unauthorized e) {
            log.warn("WooCommerce REST no autorizado (revisa WOO_CONSUMER_KEY/SECRET en WP). Sincronizando sin precios WC.");
            return List.of();
        } catch (WebClientResponseException e) {
            log.warn("WooCommerce REST error {}: {}", e.getStatusCode(), e.getMessage());
            return List.of();
        }
    }

    @Scheduled(fixedRate = 300000)
    public void scheduledSync() {
        try {
            syncFromWordpress();
            log.info("Scheduled course sync completed");
        } catch (Exception e) {
            log.error("Scheduled course sync failed: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public PagedResponse<CourseResponse> syncFromWordpress() {
        List<WordpressPostDto> posts;
        try {
            posts = wordpressWebClient.get()
                    .uri(WordpressRestUri.resolve("/wp-json/wp/v2/posts?per_page=100&_embed", wordpressPlainRest))
                    .retrieve()
                    .bodyToFlux(WordpressPostDto.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException.Unauthorized e) {
            throw new RuntimeException(
                    "WordPress 401: Application Password incorrecta o revocada. "
                            + "Crea una nueva en WP (Usuarios → Perfil → Contraseñas de aplicación) y actualiza WORDPRESS_PASSWORD, "
                            + "o en local con entradas públicas usa WORDPRESS_AUTH_ENABLED=false.",
                    e);
        }

        if (posts == null || posts.isEmpty()) return PagedResponse.empty(); // ✅ not List.of()

        List<WooProductDto> wooProducts = fetchWooProducts();

        Map<String, WooProductDto> wooByName = wooProducts == null ? Map.of()
                : wooProducts.stream().collect(Collectors.toMap(
                p -> p.name() != null ? p.name().toLowerCase() : "",
                p -> p, (a, b) -> a));

        List<Course> saved = posts.stream().map(post -> {
            Course course = courseRepository.findByWordpressPostId(post.id())
                    .orElse(new Course());

            course.setWordpressPostId(post.id());
            course.setTitle(post.title().rendered());
            course.setDescription(post.content().rendered());
            course.setImageUrl(post.jetpack_featured_media_url());
            course.setSlug(post.slug());
            course.setActive(true);

            WooProductDto woo = wooByName.get(
                    post.title().rendered() != null ? post.title().rendered().toLowerCase() : "");

            if (woo != null) {
                course.setWooProductId(woo.id());
                String price = woo.regular_price();
                course.setPrice(new BigDecimal((price == null || price.isEmpty()) ? "0" : price));

                if (woo.categories() != null && !woo.categories().isEmpty()) {
                    categoryRepository.findBySlug(woo.categories().get(0).slug())
                            .ifPresent(course::setCategory);
                }
            }

            return courseRepository.save(course);
        }).toList();

        return PagedResponse.of(saved.stream().map(courseMapper::toResponse).toList()); // ✅
    }
}