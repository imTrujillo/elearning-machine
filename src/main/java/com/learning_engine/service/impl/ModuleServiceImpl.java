package com.learning_engine.service.impl;

import com.learning_engine.dto.response.LessonResponse;
import com.learning_engine.dto.response.ModuleResponse;
import com.learning_engine.entity.CourseModule;
import com.learning_engine.repository.LessonProgressRepository;
import com.learning_engine.repository.ModuleRepository;
import com.learning_engine.service.EnrollmentService;
import com.learning_engine.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final EnrollmentService enrollmentService;
    private final LessonProgressRepository lessonProgressRepository;

    @Override
    public List<ModuleResponse> findByCourse(Long courseId, Long studentId) {
        boolean hasAccess = enrollmentService.hasActiveEnrollment(studentId, courseId);

        return moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .map(module -> toResponse(module, studentId, hasAccess))
                .toList();
    }

    private ModuleResponse toResponse(CourseModule module, Long studentId, boolean hasAccess) {
        List<LessonResponse> lessons = module.getLessons()
                .stream()
                .map(lesson -> {
                    boolean canAccess = hasAccess || Boolean.TRUE.equals(lesson.getFreePreview());

                    boolean completed = canAccess && lessonProgressRepository
                            .existsByStudentIdAndLessonIdAndCompletedTrue(studentId, lesson.getId());

                    return new LessonResponse(
                            lesson.getId(),
                            lesson.getTitle(),
                            canAccess ? lesson.getContent() : null,
                            canAccess ? lesson.getVideoUrl() : null,
                            lesson.getDurationMinutes(),
                            lesson.getOrderIndex(),
                            lesson.getFreePreview(),
                            completed
                    );
                })
                .toList();

        return new ModuleResponse(
                module.getId(),
                module.getTitle(),
                module.getDescription(),
                module.getOrderIndex(),
                lessons
        );
    }
}