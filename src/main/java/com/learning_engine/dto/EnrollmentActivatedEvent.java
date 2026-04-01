package com.learning_engine.dto;

import java.time.LocalDateTime;

public record EnrollmentActivatedEvent(
        Long enrollmentId,
        Long studentId,
        String studentEmail,
        Long courseId,
        String courseTitle,
        LocalDateTime activatedAt
) {
}
