package com.learning_engine.dto.response;

import java.time.LocalDateTime;

public record StudentResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDateTime createdAt,
        Integer totalEnrollments,
        Integer activeEnrollments
) {}