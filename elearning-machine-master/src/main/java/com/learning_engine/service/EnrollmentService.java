package com.learning_engine.service;

import com.learning_engine.dto.request.EnrollmentRequest;
import com.learning_engine.dto.response.EnrollmentResponse;

import java.util.List;

public interface EnrollmentService {
    EnrollmentResponse create(EnrollmentRequest request);
    List<EnrollmentResponse> findByStudent (Long studentId);
    EnrollmentResponse activateByWooOrder(Long WooOrderId);
    boolean hasActiveEnrollment(Long studentId, Long courseId);
}
