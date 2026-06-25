package com.company.securityapp.repository;

import com.company.securityapp.entity.Lesson;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findAllByCourseIdOrderBySortOrderAsc(Long courseId);

    Optional<Lesson> findByIdAndCourseId(Long lessonId, Long courseId);

    long countByCourseId(Long courseId);

    void deleteAllByCourseId(Long courseId);
}
