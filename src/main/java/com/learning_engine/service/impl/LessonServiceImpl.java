package com.learning_engine.service.impl;

import com.learning_engine.dto.ModuleCompletedAt;
import com.learning_engine.dto.request.LessonRequest;
import com.learning_engine.dto.response.LessonProgressResponse;
import com.learning_engine.dto.response.LessonResponse;
import com.learning_engine.dto.response.PagedResponse;
import com.learning_engine.entity.*;
import com.learning_engine.mapper.LessonMapper;
import com.learning_engine.repository.*;
import com.learning_engine.service.LessonService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final StudentRepository studentRepository;
    private final ModuleRepository moduleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final LessonMapper lessonMapper;

    @Value("${rabbitmq.exchanges.modules}")
    private String modulesExchange;

    @Value("${rabbitmq.routing-keys.module-completed}")
    private String moduleCompletedKey;

    @Override
    @CacheEvict(value = "students", allEntries = true, beforeInvocation = true)
    public LessonProgressResponse complete(Long lessonId, Long studentId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lección no encontrada: " + lessonId));

        Long courseId = lesson.getModule().getCourse().getId();

        if (!enrollmentRepository.hasActiveEnrollment(studentId, courseId)) {
            throw new RuntimeException("No tienes acceso a este curso");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado: " + studentId));

        LessonProgress progress = lessonProgressRepository
                .findByStudentIdAndLessonId(studentId, lessonId)
                .orElse(LessonProgress.builder()
                        .lesson(lesson)
                        .student(student)
                        .build());

        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        lessonProgressRepository.save(progress);

        CourseModule module = lesson.getModule();

        int moduleTotalLessons = module.getLessons().size();
        int moduleCompletedLessons = lessonProgressRepository
                .countCompletedByStudentAndModule(studentId, module.getId());
        boolean moduleCompleted = moduleTotalLessons > 0
                && moduleCompletedLessons >= moduleTotalLessons;

        if (moduleCompleted) {
            Enrollment enrollment = enrollmentRepository
                    .findByStudentIdAndCourseId(studentId, courseId)
                    .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

            int totalLessons = lessonRepository.countTotalLessonsByCourse(courseId);
            int completedLessons = lessonProgressRepository
                    .countCompletedByStudentAndCourse(studentId, courseId);
            int completionPercent = totalLessons > 0
                    ? (completedLessons * 100 / totalLessons) : 0;

            ModuleCompletedAt event = new ModuleCompletedAt(
                    enrollment.getId(),
                    module.getId(),
                    module.getTitle(),
                    courseId,
                    student.getEmail(),
                    completionPercent,
                    LocalDateTime.now()
            );
            rabbitTemplate.convertAndSend(modulesExchange, moduleCompletedKey, event);
        }

        return new LessonProgressResponse(
                lesson.getId(),
                lesson.getTitle(),
                true,
                progress.getCompletedAt(),
                moduleCompleted,
                module.getTitle()
        );
    }

    @Override
    public List<LessonResponse> getLessonsByModuleId(Long moduleId) {
        List<LessonResponse> lessons = lessonRepository
                .findByModuleIdOrderByOrderIndexAsc(moduleId)
                .stream()
                .map(lessonMapper::toResponse)
                .toList();
        return lessons;
    }

    @Override
    @Transactional
    public LessonResponse createLesson(Long moduleId, LessonRequest request) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        Lesson lesson = lessonMapper.toEntity(request);
        lesson.setModule(module);

        return lessonMapper.toResponse(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public LessonResponse updateLesson(Long moduleId, Long id, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        if (!lesson.getModule().getId().equals(moduleId)) {
            throw new RuntimeException("Lesson does not belong to this module");
        }

        return lessonMapper.toResponse(lessonRepository.save(lessonMapper.updateEntity(lesson, request)));
    }

    @Override
    @Transactional
    public void deleteLesson(Long moduleId, Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        if (!lesson.getModule().getId().equals(moduleId)) {
            throw new RuntimeException("Lesson does not belong to this module");
        }
        lessonRepository.delete(lesson);
    }
}
