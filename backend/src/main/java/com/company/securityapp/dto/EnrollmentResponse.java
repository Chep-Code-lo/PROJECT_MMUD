package com.company.securityapp.dto;

import com.company.securityapp.entity.EnrollmentStatus;
import java.time.Instant;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        Long courseId,
        String courseTitle,
        EnrollmentStatus status,
        Instant createdAt,
        Instant activatedAt,
        boolean certificateIssued) {
}
