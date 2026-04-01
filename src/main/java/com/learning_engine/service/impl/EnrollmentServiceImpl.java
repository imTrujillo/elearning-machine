package com.learning_engine.service.impl;

import com.learning_engine.dto.EnrollmentActivatedEvent;
import com.learning_engine.dto.request.EnrollmentRequest;
import com.learning_engine.dto.response.EnrollmentResponse;
import com.learning_engine.dto.response.StudentResponse;
import com.learning_engine.entity.Course;
import com.learning_engine.entity.Enrollment;
import com.learning_engine.entity.Student;
import com.learning_engine.enums.EnrollmentStatus;
import com.learning_engine.repository.CourseRepository;
import com.learning_engine.repository.EnrollmentRepository;
import com.learning_engine.repository.StudentRepository;
import com.learning_engine.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final RabbitTemplate rabbitTemplate;
    private final CourseServiceImpl courseService; // para toResponse

    @Value("${rabbitmq.exchanges.enrollments}")
    private String enrollmentsExchange;

    @Value("${rabbitmq.routing-keys.enrollment-activated}")
    private String enrollmentActivatedKey;

    @Override
    public EnrollmentResponse create(EnrollmentRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        if (enrollmentRepository.existsByStudentIdAndCourseId(
                request.studentId(), request.courseId())) {
            throw new RuntimeException("El estudiante ya está inscrito en este curso");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .wooOrderId(request.wooOrderId())
                .status(EnrollmentStatus.PENDING_PAYMENT)
                .build();

        return toResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    public List<EnrollmentResponse> findByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ✅ Llamado desde el webhook de WooCommerce
    @Override
    public EnrollmentResponse activateByWooOrder(Long wooOrderId) {
        Enrollment enrollment = enrollmentRepository.findByWooOrderId(wooOrderId)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada para orden: " + wooOrderId));

        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setActivatedAt(LocalDateTime.now());
        Enrollment saved = enrollmentRepository.save(enrollment);

        // ✅ Publicar evento a RabbitMQ
        EnrollmentActivatedEvent event = new EnrollmentActivatedEvent(
                saved.getId(),
                saved.getStudent().getId(),
                saved.getStudent().getEmail(),
                saved.getCourse().getId(),
                saved.getCourse().getTitle(),
                saved.getActivatedAt()
        );

        rabbitTemplate.convertAndSend(enrollmentsExchange, enrollmentActivatedKey, event);

        return toResponse(saved);
    }

    @Override
    public boolean hasActiveEnrollment(Long studentId, Long courseId) {
        return enrollmentRepository.hasActiveEnrollment(studentId, courseId);
    }

    private EnrollmentResponse toResponse(Enrollment e) {
        int progress = 0; // se puede calcular con LessonProgressRepository si se inyecta

        return new EnrollmentResponse(
                e.getId(),
                new StudentResponse(
                        e.getStudent().getId(),
                        e.getStudent().getFirstName(),
                        e.getStudent().getLastName(),
                        e.getStudent().getEmail(),
                        e.getStudent().getPhone(),
                        e.getStudent().getCreatedAt(),
                        null, null),
                e.getStatus(),
                e.getWooOrderId(),
                courseService.toResponse(e.getCourse()),
                e.getEnrolledAt(),
                e.getActivatedAt(),
                e.getCompletedAt(),
                progress
        );
    }
}
