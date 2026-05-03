package com.learning_engine.controller;

import com.learning_engine.dto.request.ModuleRequest;
import com.learning_engine.dto.response.LearningApiResponse;
import com.learning_engine.dto.response.ModuleResponse;
import com.learning_engine.dto.response.PagedResponse;
import com.learning_engine.service.ModuleService;
import com.learning_engine.service.StudentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/courses/{courseId}/modules")
@Tag(name = "Módulos", description = "Administrador de módulos")
public class ModuleController {

    private final ModuleService moduleService;
    private final StudentService studentService;

    public ModuleController(ModuleService moduleService, StudentService studentService) {
        this.moduleService = moduleService;
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<LearningApiResponse<List<ModuleResponse>>> getModules(
            @PathVariable Long courseId,
            @RequestParam String studentEmail) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Módulos obtenidos",
                        moduleService.findByCourse(courseId, studentEmail)));
    }

    @PostMapping
    public ResponseEntity<LearningApiResponse<ModuleResponse>> createModule(
            @PathVariable Long courseId, @RequestBody ModuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                LearningApiResponse.success("Módulo creado", moduleService.createModule(courseId, request))
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LearningApiResponse<ModuleResponse>> updateModule(
            @PathVariable Long courseId, @PathVariable Long id, @RequestBody ModuleRequest request) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Módulo actualizado", moduleService.updateModule(courseId, id, request))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<LearningApiResponse<Void>> deleteModule(
            @PathVariable Long courseId, @PathVariable Long id) {
        moduleService.deleteModule(courseId, id);
        return ResponseEntity.ok(
                LearningApiResponse.success("Módulo eliminado", null)
        );
    }
}