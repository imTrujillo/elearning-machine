package com.learning_engine.repository;

import com.learning_engine.entity.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<CourseModule, Long> {
    List<CourseModule> findByCourseIdOrderByOrderIndexAsc(Long courseId);
    Integer countByCourseId(Long courseId);
}
