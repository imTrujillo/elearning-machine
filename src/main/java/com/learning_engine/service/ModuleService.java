package com.learning_engine.service;

import com.learning_engine.dto.request.ModuleRequest;
import com.learning_engine.dto.response.ModuleResponse;
import com.learning_engine.dto.response.PagedResponse;

import java.util.List;

public interface ModuleService {
    List<ModuleResponse> findByCourse(Long courseId, String studentEmail); // ← email
    ModuleResponse createModule(Long courseId, ModuleRequest request);
    ModuleResponse updateModule(Long courseId, Long id, ModuleRequest request);
    void deleteModule(Long courseId, Long id);
}
