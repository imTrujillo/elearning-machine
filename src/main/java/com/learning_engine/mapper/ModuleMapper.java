package com.learning_engine.mapper;

import com.learning_engine.dto.request.ModuleRequest;
import com.learning_engine.dto.response.LessonResponse;
import com.learning_engine.dto.response.ModuleResponse;
import com.learning_engine.entity.CourseModule;
import com.learning_engine.repository.LessonProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ModuleMapper {

    private final LessonMapper lessonMapper;
    private final LessonProgressRepository lessonProgressRepository;

    // Used for public/student view — respects enrollment access and tracks progress
    public ModuleResponse toResponse(CourseModule module, Long studentId, boolean hasAccess) {
        List<LessonResponse> lessons = module.getLessons() != null
                ? module.getLessons().stream()
                        .map(lesson -> {
                            boolean canAccess = hasAccess || Boolean.TRUE.equals(lesson.getFreePreview());
                            boolean completed = canAccess && lessonProgressRepository
                                    .existsByStudentIdAndLessonIdAndCompletedTrue(studentId, lesson.getId());
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

    // Used for admin create/update — no access control, no progress
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
