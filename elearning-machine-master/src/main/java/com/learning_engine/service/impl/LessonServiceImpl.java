package com.learning_engine.service.impl;

import com.learning_engine.dto.response.LessonProgressResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
}