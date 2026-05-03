package com.learning_engine.controller;

import com.learning_engine.dto.request.EnrollmentRequest;
import com.learning_engine.dto.request.WooWebhookRequest;
import com.learning_engine.dto.response.EnrollmentResponse;
import com.learning_engine.dto.response.LearningApiResponse;
import com.learning_engine.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Inscripciones", description = "Gestión de inscripciones y pagos")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Operation(summary = "Crear inscripción (PENDING_PAYMENT)")
    @PostMapping("/enrollments")
    public ResponseEntity<LearningApiResponse<EnrollmentResponse>> create(
            @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LearningApiResponse.success("Inscripción creada",
                        enrollmentService.create(request)));
    }

    @Operation(summary = "Cursos del estudiante por email")
    @GetMapping("/my-courses")
    public ResponseEntity<LearningApiResponse<List<EnrollmentResponse>>> myCourses(
            @RequestParam String studentEmail) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Cursos obtenidos",
                        enrollmentService.findByStudentEmail(studentEmail)));
    }

    @Operation(summary = "Webhook de WooCommerce — crea/activa inscripción automáticamente")
    @PostMapping("/woocommerce/webhook")
    public ResponseEntity<LearningApiResponse<EnrollmentResponse>> webhook(
            @RequestBody WooWebhookRequest request) {

        if (!"completed".equalsIgnoreCase(request.status())) {
            return ResponseEntity.ok(
                    LearningApiResponse.success("Estado ignorado: " + request.status(), null));
        }

        return ResponseEntity.ok(
                LearningApiResponse.success("Inscripción activada",
                        enrollmentService.processWebhook(request)));
    }

    @Operation(summary = "Activar inscripción manualmente (sin pago WooCommerce)")
    @PostMapping("/woocommerce/activate")
    public ResponseEntity<LearningApiResponse<EnrollmentResponse>> activate(
            @RequestParam String customerEmail,
            @RequestParam Long courseId) {

        return ResponseEntity.ok(
                LearningApiResponse.success("Inscripción activada",
                        enrollmentService.activateEnrollment(customerEmail, courseId)));
    }

    @Operation(summary = "Verificar inscripción activa por email")
    @GetMapping("/enrollments/verify")
    public ResponseEntity<LearningApiResponse<Boolean>> verify(
            @RequestParam String studentEmail,
            @RequestParam Long courseId) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Verificación completada",
                        enrollmentService.hasActiveEnrollmentByEmail(studentEmail, courseId)));
    }
}