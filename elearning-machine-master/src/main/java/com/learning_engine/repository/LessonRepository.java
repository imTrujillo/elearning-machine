package com.learning_engine.repository;

import com.learning_engine.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByModuleIdOrderByOrderIndexAsc(Long moduleId);

    List<Lesson> findByModuleId(Long moduleId);

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.course.id = :courseId")
    Integer countByCourseId(@Param("courseId") Long courseId);

    List<Lesson> findByModuleIdAndFreePreviewTrue(Long moduleId);
}
