package com.learning_engine.service.impl;

import com.learning_engine.dto.request.ModuleRequest;
import com.learning_engine.dto.response.ModuleResponse;
import com.learning_engine.dto.response.PagedResponse;
import com.learning_engine.entity.Course;
import com.learning_engine.entity.CourseModule;
import com.learning_engine.mapper.ModuleMapper;
import com.learning_engine.repository.CourseRepository;
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
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;
    private final ModuleMapper moduleMapper;

    @Override
    public List<ModuleResponse> findByCourse(Long courseId, Long studentId) {
        boolean hasAccess = enrollmentService.hasActiveEnrollment(studentId, courseId);

        List<ModuleResponse> modules = moduleRepository
                .findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .map(module -> moduleMapper.toResponse(module, studentId, hasAccess))
                .toList();

        return modules;
    }

    @Override
    @Transactional
    public ModuleResponse createModule(Long courseId, ModuleRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseModule module = moduleMapper.toEntity(request);
        module.setCourse(course);

        return moduleMapper.toAdminResponse(moduleRepository.save(module));
    }

    @Override
    @Transactional
    public ModuleResponse updateModule(Long courseId, Long id, ModuleRequest request) {
        CourseModule module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        if (!module.getCourse().getId().equals(courseId)) {
            throw new RuntimeException("Module does not belong to this course");
        }

        return moduleMapper.toAdminResponse(moduleRepository.save(moduleMapper.updateEntity(module, request)));
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
}
