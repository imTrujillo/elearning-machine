package com.learning_engine.service;

import com.learning_engine.dto.request.LessonRequest;
import com.learning_engine.dto.response.LessonProgressResponse;
import com.learning_engine.dto.response.LessonResponse;

import java.util.List;

public interface LessonService {
    LessonProgressResponse complete(Long lessonId, Long studentId);
    List<LessonResponse> getLessonsByModuleId(Long moduleId);
    LessonResponse createLesson(Long moduleId, LessonRequest request);
    LessonResponse updateLesson(Long moduleId, Long id, LessonRequest request);
    void deleteLesson(Long moduleId, Long id);
}
