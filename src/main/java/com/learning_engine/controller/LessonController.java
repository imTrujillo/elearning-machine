package com.learning_engine.controller;

import com.learning_engine.dto.request.LessonRequest;
import com.learning_engine.dto.response.LearningApiResponse;
import com.learning_engine.dto.response.LessonProgressResponse;
import com.learning_engine.dto.response.LessonResponse;
import com.learning_engine.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/modules/{moduleId}/lessons")
@Tag(name = "Lecciones", description = "Administrador de lecciones")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping
    public ResponseEntity<LearningApiResponse<List<LessonResponse>>> getLessons(@PathVariable Long moduleId) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Lecciones obtenidas", lessonService.getLessonsByModuleId(moduleId))
        );
    }

    @PostMapping
    public ResponseEntity<LearningApiResponse<LessonResponse>> createLesson(
            @PathVariable Long moduleId, @RequestBody LessonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                LearningApiResponse.success("Lección creada", lessonService.createLesson(moduleId, request))
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LearningApiResponse<LessonResponse>> updateLesson(
            @PathVariable Long moduleId, @PathVariable Long id, @RequestBody LessonRequest request) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Lección actualizada", lessonService.updateLesson(moduleId, id, request))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<LearningApiResponse<Void>> deleteLesson(
            @PathVariable Long moduleId, @PathVariable Long id) {
        lessonService.deleteLesson(moduleId, id);
        return ResponseEntity.ok(
                LearningApiResponse.success("Lección eliminada", null)
        );
    }

    @Operation(summary = "Marcar lección como completada")
    @PostMapping("/{id}/complete")
    public ResponseEntity<LearningApiResponse<LessonProgressResponse>> completeLesson(
            @PathVariable Long moduleId,
            @PathVariable Long id,
            @RequestParam Long studentId) {

        return ResponseEntity.ok(
                LearningApiResponse.success("Lección completada",
                        lessonService.complete(id, studentId))
        );
    }
}