package com.company.securityapp.repository;

import com.company.securityapp.entity.Course;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByPublishedTrueOrderByCreatedAtDesc();

    Optional<Course> findByIdAndPublishedTrue(Long id);

    long countByPublishedTrue();
}
