package com.learning_engine.service;

import com.learning_engine.dto.response.CourseResponse;
import com.learning_engine.dto.response.PagedResponse;

import java.util.List;

public interface CourseSyncService {
    PagedResponse<CourseResponse> syncCourses();
}