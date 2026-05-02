package com.learning_engine.mapper;

import com.learning_engine.dto.response.EnrollmentResponse;
import com.learning_engine.dto.response.StudentResponse;
import com.learning_engine.entity.Enrollment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentMapper {

    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;

    public EnrollmentResponse toResponse(Enrollment e) {
        StudentResponse studentResponse = new StudentResponse(
                e.getStudent().getId(),
                e.getStudent().getFirstName(),
                e.getStudent().getLastName(),
                e.getStudent().getEmail(),
                e.getStudent().getPhone(),
                e.getStudent().getCreatedAt(),
                null,
                null
        );

        return new EnrollmentResponse(
                e.getId(),
                studentResponse,
                e.getStatus(),
                e.getWooOrderId(),
                courseMapper.toResponse(e.getCourse()),
                e.getEnrolledAt(),
                e.getActivatedAt(),
                e.getCompletedAt(),
                0
        );
    }
}
