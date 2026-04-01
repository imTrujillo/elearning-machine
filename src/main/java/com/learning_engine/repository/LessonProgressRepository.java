package com.learning_engine.repository;

import com.learning_engine.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByStudentIdAndLessonId(Long studentId, Long lessonId);

    List<LessonProgress> findByStudentIdAndCompletedTrue(Long studentId);

    boolean existsByStudentIdAndLessonIdAndCompletedTrue(Long studentId, Long lessonId);

    @Query("""
            SELECT COUNT(lp) FROM LessonProgress lp
            WHERE lp.student.id = :studentId
            AND lp.lesson.module.id = :moduleId
            AND lp.completed = true
            """)
    Integer countCompletedByStudentAndModule(@Param("studentId") Long studentId,
                                             @Param("moduleId") Long moduleId);

    @Query("""
            SELECT COUNT(lp) FROM LessonProgress lp
            WHERE lp.student.id = :studentId
            AND lp.lesson.module.course.id = :courseId
            AND lp.completed = true
            """)
    Integer countCompletedByStudentAndCourse(@Param("studentId") Long studentId,
                                             @Param("courseId") Long courseId);
}
