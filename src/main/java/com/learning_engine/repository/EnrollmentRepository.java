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

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Enrollment> findByStudent_EmailOrderByEnrolledAtDesc(String email);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentEmailAndCourseId(String email, Long courseId);

    Optional<Enrollment> findFirstByWooOrderId(Long wooOrderId);

    Optional<Enrollment> findFirstByStudentEmailAndCourseId(String email, Long courseId); 

    @Query("""
            SELECT COUNT(e) > 0 FROM Enrollment e
            WHERE e.student.email = :email
            AND e.course.id = :courseId
            AND e.status = 'ACTIVE'
            """)
    boolean hasActiveEnrollmentByEmail(@Param("email") String email,
                                       @Param("courseId") Long courseId); // ← nuevo

    @Query("""
            SELECT COUNT(e) > 0 FROM Enrollment e
            WHERE e.student.id = :studentId
            AND e.course.id = :courseId
            AND e.status = 'ACTIVE'
            """)
    boolean hasActiveEnrollment(@Param("studentId") Long studentId,
                                @Param("courseId") Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.email = :email AND e.status = :status ORDER BY e.enrolledAt DESC")
    Optional<Enrollment> findLatestPendingByStudentEmail(
            @Param("email") String email,
            @Param("status") EnrollmentStatus status
    );

    List<Enrollment> findByStatus(EnrollmentStatus status);
}