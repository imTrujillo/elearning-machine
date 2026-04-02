package com.learning_engine.service;

import com.learning_engine.dto.response.LessonProgressResponse;

public interface LessonService {
    LessonProgressResponse complete(Long lessonId, Long studentId);
}
