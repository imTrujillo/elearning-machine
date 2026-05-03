package com.learning_engine.dto;

import java.time.LocalDateTime;

public record EnrollmentActivatedEvent(
        Long enrollmentId,
        String studentEmail,
        String studentName,
        Long courseId,
        String courseTitle,
        int courseTotalModules,
        LocalDateTime activatedAt
) {
}
