package com.learning_engine.mapper;

import com.learning_engine.dto.request.CourseRequest;
import com.learning_engine.dto.response.CategorySummaryResponse;
import com.learning_engine.dto.response.CourseResponse;
import com.learning_engine.entity.Category;
import com.learning_engine.entity.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseMapper {
    public CourseResponse toResponse(Course course) {
        CategorySummaryResponse catSummary = course.getCategory() != null
                ? new CategorySummaryResponse(
                course.getCategory().getId(),
                course.getCategory().getName(),
                course.getCategory().getSlug())
                : null;

        int totalModules = course.getCourseModules() != null
                ? course.getCourseModules().size() : 0;

        int totalLessons = course.getCourseModules() != null
                ? course.getCourseModules().stream()
                .mapToInt(m -> m.getLessons() != null ? m.getLessons().size() : 0)
                .sum()
                : 0;

        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getImageUrl(),
                course.getInstructor(),
                catSummary,
                course.getSlug(),
                course.getPrice(),
                course.getWooProductId(),
                course.getWordpressPostId(),
                course.getActive(),
                totalModules,
                totalLessons,
                course.getCreatedAt()
        );
    }

    public Course toEntity(CourseRequest request, Category category) {
        Course course = new Course();

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setImageUrl(request.getImageUrl());
        course.setInstructor(request.getInstructor());
        course.setPrice(request.getPrice());
        course.setActive(request.isActive());
        course.setCategory(category);
        course.setSlug(request.getTitle().toLowerCase().replace(" ", "-"));

        return course;
    }

    public Course updateEntity(Course course, CourseRequest request, Category category) {
        if (request.getTitle() != null) {
            course.setTitle(request.getTitle());
            course.setSlug(request.getTitle().toLowerCase().replace(" ", "-"));
        }
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getPrice() != null) course.setPrice(request.getPrice());
        if (category != null) course.setCategory(category);
        course.setActive(request.isActive());

        return course;
    }
}