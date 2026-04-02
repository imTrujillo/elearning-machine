package com.learning_engine.repository;

import com.learning_engine.entity.Enrollment;
import com.learning_engine.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<Enrollment> findByWooOrderId(Long wooOrderId);

    @Query("""
            SELECT COUNT(e) > 0 FROM Enrollment e
            WHERE e.student.id = :studentId
            AND e.course.id = :courseId
            AND e.status = 'ACTIVE'
            """)
    boolean hasActiveEnrollment(@Param("studentId") Long studentId,
                                @Param("courseId") Long courseId);

    List<Enrollment> findByStatus(EnrollmentStatus status);
}
