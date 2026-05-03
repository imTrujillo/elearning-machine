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
import org.springframework.dao.DataIntegrityViolationException;
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
                .build();

        return enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentResponse processWebhook(WooWebhookRequest request) {
        var existing = enrollmentRepository.findFirstByWooOrderId(request.id());
        if (existing.isPresent()) {
            log.info("Orden {} ya procesada, ignorando.", request.id());
            return enrollmentMapper.toResponse(existing.get());
        }

        Student student = studentRepository.findByEmail(request.customerEmail())
                .orElseGet(() -> {
                    log.info("Estudiante no encontrado, creando: {}", request.customerEmail());
                    String namePart = request.customerEmail().contains("@")
                            ? request.customerEmail().split("@")[0]
                            : request.customerEmail();
                    return studentRepository.save(Student.builder()
                            .email(request.customerEmail())
                            .firstName(namePart)
                            .lastName("")
                            .build());
                });

        Course course;
        if (request.lineItems() != null && !request.lineItems().isEmpty()) {
            Long wooProductId = request.lineItems().get(0).productId();
            course = courseRepository.findByWooProductId(wooProductId)
                    .orElseThrow(() -> new RuntimeException(
                            "Curso no encontrado para producto WooCommerce: " + wooProductId));
        } else {
            Enrollment pending = enrollmentRepository
                    .findLatestPendingByStudentEmail(
                            student.getEmail(),
                            EnrollmentStatus.PENDING_PAYMENT)
                    .orElseThrow(() -> new RuntimeException(
                            "No hay inscripción pendiente de pago para: " + student.getEmail()));
            course = pending.getCourse();
        }

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

        if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            log.info("Inscripción ya activa para {} en curso {}",
                    student.getEmail(), course.getTitle());
            return enrollmentMapper.toResponse(enrollment);
        }

        enrollment.setWooOrderId(request.id());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setActivatedAt(LocalDateTime.now());

        Enrollment saved;
        try {
            saved = enrollmentRepository.save(enrollment);
        } catch (DataIntegrityViolationException e) {
            log.warn("Conflicto al guardar orden {}, reintentando búsqueda.", request.id());
            saved = enrollmentRepository.findFirstByWooOrderId(request.id())
                    .orElseThrow(() -> new RuntimeException("Error de concurrencia al activar inscripción"));
        }

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
    @Transactional
    public EnrollmentResponse activateEnrollment(String customerEmail, Long courseId) {

        // 1. Buscar estudiante
        Student student = studentRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Estudiante no encontrado: " + customerEmail));

        // 2. Buscar curso
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException(
                        "Curso no encontrado: " + courseId));

        // 3. Buscar la inscripción exacta
        Enrollment enrollment = enrollmentRepository
                .findFirstByStudentEmailAndCourseId(customerEmail, courseId)
                .orElseThrow(() -> new RuntimeException(
                        "No hay inscripción para " + customerEmail + " en curso " + courseId));

        // 4. Guardia: ya activa
        if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            log.info("Inscripción ya activa para {} en curso {}", customerEmail, course.getTitle());
            return enrollmentMapper.toResponse(enrollment);
        }

        // 5. Generar wooOrderId sintético — prefijo MANUAL + timestamp para evitar colisiones
        //    Usamos negativo para que nunca colisione con IDs reales de WooCommerce (siempre positivos)
        long syntheticOrderId = -(System.currentTimeMillis());
        enrollment.setWooOrderId(syntheticOrderId);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setActivatedAt(LocalDateTime.now());
        Enrollment saved = enrollmentRepository.save(enrollment);

        // 6. Publicar evento
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
        log.info("Inscripción activada manualmente, synthetic orderId: {}", syntheticOrderId);

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