package com.learning_engine.service;

import com.learning_engine.dto.response.ModuleResponse;

import java.util.List;

public interface ModuleService {
    List<ModuleResponse> findByCourse(Long courseId, Long studentId);
}
