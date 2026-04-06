package com.learning_engine.service.impl;

import com.learning_engine.dto.request.LessonRequest;
import com.learning_engine.dto.response.LessonProgressResponse;
import com.learning_engine.dto.response.LessonResponse;
import com.learning_engine.dto.response.ModuleCompletedEvent;
import com.learning_engine.entity.CourseModule;
import com.learning_engine.entity.Lesson;
import com.learning_engine.entity.LessonProgress;
import com.learning_engine.entity.Student;
import com.learning_engine.repository.EnrollmentRepository;
import com.learning_engine.repository.LessonProgressRepository;
import com.learning_engine.repository.LessonRepository;
import com.learning_engine.repository.ModuleRepository;
import com.learning_engine.service.LessonService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final ModuleRepository moduleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchanges.modules}")
    private String modulesExchange;

    @Value("${rabbitmq.routing-keys.module-completed}")
    private String moduleCompletedKey;

    @Override
    public LessonProgressResponse complete(Long lessonId, Long studentId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lección no encontrada: " + lessonId));

        Long courseId = lesson.getModule().getCourse().getId();

        if (!enrollmentRepository.hasActiveEnrollment(studentId, courseId)) {
            throw new RuntimeException("No tienes acceso a este curso");
        }

        LessonProgress progress = lessonProgressRepository
                .findByStudentIdAndLessonId(studentId, lessonId)
                .orElse(LessonProgress.builder()
                        .lesson(lesson)
                        .build());

        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        lessonProgressRepository.save(progress);

        CourseModule module = lesson.getModule();
        int totalLessons = module.getLessons().size();
        int completedLessons = lessonProgressRepository
                .countCompletedByStudentAndModule(studentId, module.getId());

        boolean moduleCompleted = totalLessons > 0 && completedLessons >= totalLessons;

        if (moduleCompleted) {
            Student student = progress.getStudent();
            ModuleCompletedEvent event = new ModuleCompletedEvent(
                    module.getId(),
                    module.getTitle(),
                    courseId,
                    studentId,
                    student != null ? student.getEmail() : "",
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
        return lessonRepository.findByModuleIdOrderByOrderIndexAsc(moduleId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LessonResponse createLesson(Long moduleId, LessonRequest request) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setDurationMinutes(request.getDurationMinutes());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setFreePreview(request.isFreePreview());

        return mapToResponse(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public LessonResponse updateLesson(Long moduleId, Long id, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        if (!lesson.getModule().getId().equals(moduleId)) {
            throw new RuntimeException("Lesson does not belong to this module");
        }

        if (request.getTitle() != null) lesson.setTitle(request.getTitle());
        if (request.getContent() != null) lesson.setContent(request.getContent());
        if (request.getVideoUrl() != null) lesson.setVideoUrl(request.getVideoUrl());
        if (request.getDurationMinutes() != null) lesson.setDurationMinutes(request.getDurationMinutes());
        if (request.getOrderIndex() != null) lesson.setOrderIndex(request.getOrderIndex());

        return mapToResponse(lessonRepository.save(lesson));
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

    // ¡Aquí está el método que faltaba!
    private LessonResponse mapToResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getContent(),
                lesson.getVideoUrl(),
                lesson.getDurationMinutes(),
                lesson.getOrderIndex(),
                lesson.getFreePreview() != null ? lesson.getFreePreview() : false,
                false // completed: por defecto false para la vista administrativa
        );
    }
}