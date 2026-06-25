package com.company.securityapp.repository;

import com.company.securityapp.entity.Certificate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findAllByStudentIdOrderByIssuedAtDesc(Long studentId);

    Optional<Certificate> findByIdAndStudentId(Long id, Long studentId);

    Optional<Certificate> findByEnrollmentId(Long enrollmentId);

    long countByCourseId(Long courseId);
}
