package com.learning_engine.service.impl;

import com.learning_engine.dto.request.ModuleRequest;
import com.learning_engine.dto.response.LessonResponse;
import com.learning_engine.dto.response.ModuleResponse;
import com.learning_engine.entity.Course;
import com.learning_engine.entity.CourseModule;
import com.learning_engine.repository.CourseRepository;
import com.learning_engine.repository.LessonProgressRepository;
import com.learning_engine.repository.ModuleRepository;
import com.learning_engine.service.EnrollmentService;
import com.learning_engine.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final EnrollmentService enrollmentService;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseRepository courseRepository; // Agregado para buscar el curso

    @Override
    public List<ModuleResponse> findByCourse(Long courseId, Long studentId) {
        boolean hasAccess = enrollmentService.hasActiveEnrollment(studentId, courseId);

        return moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .map(module -> toResponse(module, studentId, hasAccess))
                .toList();
    }

    @Transactional
    @Override
    public ModuleResponse createModule(Long courseId, ModuleRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseModule module = new CourseModule();
        module.setCourse(course);
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setOrderIndex(request.getOrderIndex());

        return toAdminResponse(moduleRepository.save(module));
    }

    @Override
    @Transactional
    public ModuleResponse updateModule(Long courseId, Long id, ModuleRequest request) {
        CourseModule module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        if (!module.getCourse().getId().equals(courseId)) {
            throw new RuntimeException("Module does not belong to this course");
        }

        if (request.getTitle() != null) module.setTitle(request.getTitle());
        if (request.getDescription() != null) module.setDescription(request.getDescription());
        if (request.getOrderIndex() != null) module.setOrderIndex(request.getOrderIndex());

        return toAdminResponse(moduleRepository.save(module));
    }

    @Override
    @Transactional
    public void deleteModule(Long courseId, Long id) {
        CourseModule module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        if (!module.getCourse().getId().equals(courseId)) {
            throw new RuntimeException("Module does not belong to this course");
        }
        moduleRepository.delete(module);
    }

    private ModuleResponse toResponse(CourseModule module, Long studentId, boolean hasAccess) {
        List<LessonResponse> lessons = module.getLessons() != null ? module.getLessons()
                .stream()
                .map(lesson -> {
                    boolean canAccess = hasAccess || Boolean.TRUE.equals(lesson.getFreePreview());
                    boolean completed = canAccess && lessonProgressRepository
                            .existsByStudentIdAndLessonIdAndCompletedTrue(studentId, lesson.getId());
                    return new LessonResponse(
                            lesson.getId(), lesson.getTitle(),
                            canAccess ? lesson.getContent() : null,
                            canAccess ? lesson.getVideoUrl() : null,
                            lesson.getDurationMinutes(), lesson.getOrderIndex(),
                            lesson.getFreePreview(), completed
                    );
                })
                .toList() : List.of();

        return new ModuleResponse(module.getId(), module.getTitle(), module.getDescription(), module.getOrderIndex(), lessons);
    }

    // Mapper simple para crear/actualizar desde el panel de admin (no evaluamos lecciones/studentId)
    private ModuleResponse toAdminResponse(CourseModule module) {
        return new ModuleResponse(
                module.getId(),
                module.getTitle(),
                module.getDescription(),
                module.getOrderIndex(),
                List.of()
        );
    }
}