package com.learning_engine.repository;

import com.learning_engine.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Course> findByActiveTrue(Pageable pageable);

    Page<Course> findByCategorySlug(String slug, Pageable pageable);

    Page<Course> findByCategorySlugAndActiveTrue(String slug, Pageable pageable);

    Optional<Course> findByWordpressPostId(Long wordpressPostId);

    Optional<Course> findByWooProductId(Long wooProductId);

    boolean existsByWordpressPostId(Long wordpressPostId);
}
