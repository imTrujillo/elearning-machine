package com.learning_engine.controller;

import com.learning_engine.dto.request.LessonCompleteRequest;
import com.learning_engine.dto.response.LearningApiResponse;
import com.learning_engine.dto.response.LessonProgressResponse;
import com.learning_engine.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Tag(name = "Lecciones", description = "Progreso de lecciones")
public class LessonController {

    private final LessonService lessonService;

    @Operation(summary = "Marcar lección como completada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lección completada"),
            @ApiResponse(responseCode = "403", description = "Sin inscripción activa"),
            @ApiResponse(responseCode = "404", description = "Lección no encontrada")
    })
    @PostMapping("/{id}/complete")
    public ResponseEntity<LearningApiResponse<LessonProgressResponse>> complete(
            @PathVariable Long id,
            @RequestBody LessonCompleteRequest request) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Lección completada",
                        lessonService.complete(id, request.studentId())));
    }
}