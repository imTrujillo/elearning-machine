package com.learning_engine.service;

import com.learning_engine.dto.request.CourseRequest;
import com.learning_engine.dto.response.CourseResponse;
import com.learning_engine.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseService {
    PagedResponse<CourseResponse> findAll (Pageable pageable);
    PagedResponse<CourseResponse> findByCategory (String slug, Pageable pageable);
    CourseResponse findById(Long id);
    PagedResponse<CourseResponse> syncFromWordpress();
    CourseResponse assignCategory(Long courseId, String slug);
    CourseResponse createCourse(CourseRequest request);
    CourseResponse updateCourse(Long id, CourseRequest request);
    void deleteCourse(Long id);
}