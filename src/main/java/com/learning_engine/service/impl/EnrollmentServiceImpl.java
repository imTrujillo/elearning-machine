package com.learning_engine.service.impl;

import com.learning_engine.dto.EnrollmentActivatedEvent;
import com.learning_engine.dto.request.EnrollmentRequest;
import com.learning_engine.dto.request.WooWebhookRequest;
import com.learning_engine.dto.response.EnrollmentResponse;
import com.learning_engine.entity.Course;
import com.learning_engine.entity.Enrollment;
import com.learning_engine.entity.Student;
import com.learning_engine.enums.EnrollmentStatus;
import com.learning_engine.mapper.EnrollmentMapper;
import com.learning_engine.repository.CourseRepository;
import com.learning_engine.repository.EnrollmentRepository;
import com.learning_engine.repository.ModuleRepository;
import com.learning_engine.repository.StudentRepository;
import com.learning_engine.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final RabbitTemplate rabbitTemplate;
    private final EnrollmentMapper enrollmentMapper;

    @Value("${rabbitmq.exchanges.enrollments}")
    private String enrollmentsExchange;

    @Value("${rabbitmq.routing-keys.enrollment-activated}")
    private String enrollmentActivatedKey;

    // ─── POST /api/enrollments ─────────────────────────────────────────────────
    // Solo crea en PENDING_PAYMENT. El webhook activa.
    @Override
    @Transactional
    public EnrollmentResponse create(EnrollmentRequest request) {
        Student student = studentRepository.findByEmail(request.studentEmail())
                .orElseThrow(() -> new RuntimeException(
                        "Estudiante no encontrado: " + request.studentEmail()));

        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new RuntimeException(
                        "Curso no encontrado: " + request.courseId()));

        if (enrollmentRepository.existsByStudentEmailAndCourseId(
                request.studentEmail(), request.courseId())) {
            throw new RuntimeException("El estudiante ya está inscrito en este curso");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.PENDING_PAYMENT)
                // wooOrderId = null hasta que llegue el webhook
                .build();

        return enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }

    // ─── POST /api/woocommerce/webhook ─────────────────────────────────────────
    // Encuentra o crea al estudiante, encuentra el curso por wooProductId,
    // encuentra o crea la inscripción, asigna el wooOrderId y activa.
    @Override
    @Transactional
    public EnrollmentResponse processWebhook(WooWebhookRequest request) {
        // 1. Idempotencia: si esta orden ya fue procesada, devolver la inscripción
        var existing = enrollmentRepository.findFirstByWooOrderId(request.id());
        if (existing.isPresent()) {
            log.info("Orden {} ya procesada, ignorando.", request.id());
            return enrollmentMapper.toResponse(existing.get());
        }

        // 2. Encontrar o crear el estudiante por email
        Student student = studentRepository.findByEmail(request.customerEmail())
                .orElseGet(() -> {
                    log.info("Estudiante no encontrado, creando: {}", request.customerEmail());
                    return studentRepository.save(Student.builder()
                            .email(request.customerEmail())
                            .firstName(request.customerEmail().split("@")[0])
                            .lastName("")
                            .build());
                });

        // 3. Encontrar el curso por wooProductId (primer producto del pedido)
        if (request.lineItems() == null || request.lineItems().isEmpty()) {
            throw new RuntimeException("El pedido no tiene productos");
        }
        Long wooProductId = request.lineItems().get(0).productId();
        Course course = courseRepository.findByWooProductId(wooProductId)
                .orElseThrow(() -> new RuntimeException(
                        "Curso no encontrado para producto WooCommerce: " + wooProductId));

        // 4. Buscar inscripción existente (PENDING_PAYMENT) o crear una nueva
        Enrollment enrollment = enrollmentRepository
                .findFirstByStudentEmailAndCourseId(student.getEmail(), course.getId())
                .orElseGet(() -> {
                    log.info("Creando nueva inscripción para {} en curso {}",
                            student.getEmail(), course.getTitle());
                    return Enrollment.builder()
                            .student(student)
                            .course(course)
                            .status(EnrollmentStatus.PENDING_PAYMENT)
                            .build();
                });

        // 5. Activar
        if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            log.info("Inscripción ya activa para {} en curso {}", student.getEmail(), course.getTitle());
            return enrollmentMapper.toResponse(enrollment);
        }

        enrollment.setWooOrderId(request.id()); // ← el ID secuencial de WooCommerce
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setActivatedAt(LocalDateTime.now());
        Enrollment saved = enrollmentRepository.save(enrollment);

        // 6. Publicar evento a Group B
        int totalModules = moduleRepository.countByCourseId(course.getId());
        EnrollmentActivatedEvent event = new EnrollmentActivatedEvent(
                saved.getId(),
                saved.getStudent().getEmail(),
                saved.getStudent().getFirstName() + " " + saved.getStudent().getLastName(),
                saved.getCourse().getId(),
                saved.getCourse().getTitle(),
                totalModules,
                saved.getActivatedAt()
        );
        rabbitTemplate.convertAndSend(enrollmentsExchange, enrollmentActivatedKey, event);
        log.info("Inscripción activada y evento publicado: {}", saved.getId());

        return enrollmentMapper.toResponse(saved);
    }

    @Override
    public List<EnrollmentResponse> findByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId)
                .stream().map(enrollmentMapper::toResponse).toList();
    }

    @Override
    public List<EnrollmentResponse> findByStudentEmail(String email) {
        return enrollmentRepository.findByStudentEmail(email)
                .stream().map(enrollmentMapper::toResponse).toList();
    }

    @Override
    public boolean hasActiveEnrollmentByEmail(String studentEmail, Long courseId) {
        return enrollmentRepository.hasActiveEnrollmentByEmail(studentEmail, courseId);
    }
}