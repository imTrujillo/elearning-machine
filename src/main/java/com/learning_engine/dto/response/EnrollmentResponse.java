package com.learning_engine.dto.response;

import com.learning_engine.enums.EnrollmentStatus;

import java.time.LocalDateTime;

public record EnrollmentResponse (
        Long id,
        StudentResponse student,
        EnrollmentStatus status,
        Long wooOrderId,
        CourseResponse course,
        LocalDateTime enrolledAt,
        LocalDateTime activatedAt,
        LocalDateTime completedAt,
        Integer progressPercent
        ){ }
