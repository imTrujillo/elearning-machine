package com.learning_engine.service;

import com.learning_engine.dto.response.CourseResponse;
import java.util.List;

public interface CourseSyncService {
    List<CourseResponse> syncCourses();
}