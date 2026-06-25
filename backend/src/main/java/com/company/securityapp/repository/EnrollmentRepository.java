package com.company.securityapp.repository;

import com.company.securityapp.entity.Enrollment;
import com.company.securityapp.entity.EnrollmentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId, EnrollmentStatus status);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<Enrollment> findByIdAndStudentId(Long id, Long studentId);

    List<Enrollment> findAllByStudentIdOrderByCreatedAtDesc(Long studentId);

    long countByStatus(EnrollmentStatus status);

    long countByCourseId(Long courseId);
}
