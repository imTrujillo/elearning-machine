package com.learning_engine.service.impl;

import com.learning_engine.dto.request.StudentRequest;
import com.learning_engine.dto.response.StudentResponse;
import com.learning_engine.entity.Student;
import com.learning_engine.mapper.StudentMapper;
import com.learning_engine.repository.StudentRepository;
import com.learning_engine.service.StudentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    @Override
    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Ya existe un estudiante con el email: " + request.email());
        }
        return studentMapper.toResponse(studentRepository.save(studentMapper.toEntity(request)));
    }

    @Override
    @Transactional
    @Cacheable(value = "students", key = "'id:' + #id")
    public StudentResponse findById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado: " + id));
        return studentMapper.toResponse(student);
    }

    @Override
    @Transactional
    @Cacheable(value = "students", key = "'email:' + #email")
    public StudentResponse findByEmail(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado: " + email));
        return studentMapper.toResponse(student);
    }

    @Override
    @Transactional
    public StudentResponse findOrCreateByEmail(String email, String firstName, String lastName) {
        return studentRepository.findByEmail(email)
                .map(studentMapper::toResponse)
                .orElseGet(() -> {
                    Student student = Student.builder()
                            .email(email)
                            .firstName(firstName)
                            .lastName(lastName)
                            .build();
                    return studentMapper.toResponse(studentRepository.save(student));
                });
    }
}
