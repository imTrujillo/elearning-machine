package com.learning_engine.service;

import com.learning_engine.dto.response.CourseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseService {
    Page<CourseResponse> findAll (Pageable pageable);
    Page<CourseResponse> findByCategory (String slug, Pageable pageable);
    CourseResponse findById(Long id);
    List<CourseResponse> syncFromWordpress();
}
