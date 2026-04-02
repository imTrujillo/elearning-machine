package com.learning_engine.controller;

import com.learning_engine.dto.request.StudentRequest;
import com.learning_engine.dto.response.LearningApiResponse;
import com.learning_engine.dto.response.StudentResponse;
import com.learning_engine.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Estudiantes", description = "Gestión de estudiantes")
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "Registrar estudiante")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Estudiante registrado"),
            @ApiResponse(responseCode = "400", description = "Email duplicado"),
            @ApiResponse(responseCode = "422", description = "Validación fallida")
    })
    @PostMapping
    public ResponseEntity<LearningApiResponse<StudentResponse>> create(
            @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LearningApiResponse.success("Estudiante registrado", studentService.create(request)));
    }

    @Operation(summary = "Buscar estudiante por ID")
    @GetMapping("/{id}")
    public ResponseEntity<LearningApiResponse<StudentResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Estudiante encontrado", studentService.findById(id)));
    }

    @Operation(summary = "Buscar estudiante por email")
    @GetMapping("/email/{email}")
    public ResponseEntity<LearningApiResponse<StudentResponse>> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Estudiante encontrado", studentService.findByEmail(email)));
    }
}
