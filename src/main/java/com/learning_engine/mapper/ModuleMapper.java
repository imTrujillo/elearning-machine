package com.learning_engine.mapper;

import com.learning_engine.dto.request.ModuleRequest;
import com.learning_engine.dto.response.LessonResponse;
import com.learning_engine.dto.response.ModuleResponse;
import com.learning_engine.entity.CourseModule;
import com.learning_engine.entity.Student;
import com.learning_engine.repository.LessonProgressRepository;
import com.learning_engine.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ModuleMapper {
    private final LessonMapper lessonMapper;
    private final LessonProgressRepository lessonProgressRepository;
    private final StudentRepository studentRepository;

    public ModuleResponse toResponse(CourseModule module, String studentEmail, boolean hasAccess) {

        Long studentId = (studentEmail == null || studentEmail.isBlank())
                ? null
                : studentRepository.findByEmail(studentEmail.trim())
                        .map(Student::getId)
                        .orElse(null);
        List<LessonResponse> lessons = module.getLessons() != null
                ? module.getLessons().stream()
                .map(lesson -> {
                    boolean canAccess = hasAccess || Boolean.TRUE.equals(lesson.getFreePreview());
                    boolean completed = canAccess
                            && studentId != null
                            && lessonProgressRepository
                            .existsByStudentIdAndLessonIdAndCompletedTrue(
                                    studentId, lesson.getId());
                    return lessonMapper.toResponse(lesson, canAccess, completed);
                })
                .toList()
                : List.of();

        return new ModuleResponse(
                module.getId(),
                module.getTitle(),
                module.getDescription(),
                module.getOrderIndex(),
                lessons
        );
    }

    public ModuleResponse toAdminResponse(CourseModule module) {
        return new ModuleResponse(
                module.getId(),
                module.getTitle(),
                module.getDescription(),
                module.getOrderIndex(),
                List.of()
        );
    }

    public CourseModule toEntity(ModuleRequest request) {
        CourseModule module = new CourseModule();
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setOrderIndex(request.getOrderIndex());
        return module;
    }

    public CourseModule updateEntity(CourseModule module, ModuleRequest request) {
        if (request.getTitle() != null) module.setTitle(request.getTitle());
        if (request.getDescription() != null) module.setDescription(request.getDescription());
        if (request.getOrderIndex() != null) module.setOrderIndex(request.getOrderIndex());
        return module;
    }
}