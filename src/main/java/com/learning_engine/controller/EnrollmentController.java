package com.learning_engine.controller;

import com.learning_engine.dto.request.EnrollmentRequest;
import com.learning_engine.dto.request.WooWebhookRequest;
import com.learning_engine.dto.response.EnrollmentResponse;
import com.learning_engine.dto.response.LearningApiResponse;
import com.learning_engine.service.EnrollmentService;
import com.learning_engine.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Inscripciones", description = "Gestión de inscripciones y pagos")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final StudentService studentService;

    @Operation(summary = "Crear inscripción")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inscripción creada"),
            @ApiResponse(responseCode = "400", description = "Ya inscrito"),
            @ApiResponse(responseCode = "404", description = "Curso o estudiante no encontrado")
    })
    @PostMapping("/enrollments")
    public ResponseEntity<LearningApiResponse<EnrollmentResponse>> create(
            @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LearningApiResponse.success("Inscripción creada",
                        enrollmentService.create(request)));
    }

    @Operation(summary = "Cursos del estudiante")
    @GetMapping("/my-courses")
    public ResponseEntity<LearningApiResponse<List<EnrollmentResponse>>> myCourses(
            Authentication auth) {

        String email = auth.getName();
        Long studentId = studentService.findByEmail(email).id();

        return ResponseEntity.ok(
                LearningApiResponse.success("Cursos obtenidos",
                        enrollmentService.findByStudent(studentId)));
    }

    @Operation(summary = "Webhook de WooCommerce — confirmar pago")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripción activada"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @PostMapping("/woocommerce/webhook")
    public ResponseEntity<LearningApiResponse<EnrollmentResponse>> webhook(
            @RequestBody WooWebhookRequest request) {

        if (!"completed".equalsIgnoreCase(request.status())) {
            return ResponseEntity.ok(
                    LearningApiResponse.success("Estado ignorado: " + request.status(), null));
        }

        return ResponseEntity.ok(
                LearningApiResponse.success("Inscripción activada",
                        enrollmentService.activateByWooOrder(request.id())));
    }

    @Operation(summary = "Verificar inscripción activa")
    @GetMapping("/enrollments/verify")
    public ResponseEntity<LearningApiResponse<Boolean>> verify(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Verificación completada",
                        enrollmentService.hasActiveEnrollment(studentId, courseId)));
    }
}