package com.learning_engine.repository;

import com.learning_engine.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    // Categorías que tienen al menos un curso activo
    @Query("""
            SELECT DISTINCT c FROM Category c
            WHERE EXISTS (
                SELECT co FROM Course co
                WHERE co.category = c
                AND co.active = true
            )
            """)
    List<Category> findAllWithActiveCourses();

    @Query("SELECT COUNT(c) FROM Course c WHERE c.category.slug = :slug AND c.active = true")
    int countCoursesBySlug(@Param("slug") String slug);
}