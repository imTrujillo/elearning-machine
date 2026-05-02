package com.learning_engine.mapper;

import com.learning_engine.dto.request.StudentRequest;
import com.learning_engine.dto.response.StudentResponse;
import com.learning_engine.entity.Student;
import com.learning_engine.enums.EnrollmentStatus;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public StudentResponse toResponse(Student s) {
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

    public Student toEntity(StudentRequest request) {
        return Student.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .build();
    }
}
