package com.learning_engine.service.impl;

import com.learning_engine.dto.request.StudentRequest;
import com.learning_engine.dto.response.StudentResponse;
import com.learning_engine.entity.Student;
import com.learning_engine.enums.EnrollmentStatus;
import com.learning_engine.repository.StudentRepository;
import com.learning_engine.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Ya existe un estudiante con el email: " + request.email());
        }

        Student student = Student.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .build();

        return toResponse(studentRepository.save(student));
    }

    @Override
    public StudentResponse findById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado: " + id));
        return toResponse(student);
    }

    @Override
    public StudentResponse findByEmail(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado: " + email));
        return toResponse(student);
    }

    private StudentResponse toResponse(Student s) {
        int total = s.getEnrollments() != null ? s.getEnrollments().size() : 0;
        int active = s.getEnrollments() != null
                ? (int) s.getEnrollments().stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .count()
                : 0;

        return new StudentResponse(
                s.getId(),
                s.getFirstName(),
                s.getLastName(),
                s.getEmail(),
                s.getPhone(),
                s.getCreatedAt(),
                total,
                active
        );
    }
}