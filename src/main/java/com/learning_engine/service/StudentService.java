package com.learning_engine.service;

import com.learning_engine.dto.request.StudentRequest;
import com.learning_engine.dto.response.StudentResponse;

public interface StudentService {
    StudentResponse create(StudentRequest request);
    StudentResponse findById(Long id);
    StudentResponse findByEmail(String email);
    StudentResponse findOrCreateByEmail(String email, String firstName, String lastName);
}
