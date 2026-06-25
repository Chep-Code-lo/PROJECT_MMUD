package com.company.securityapp.dto;

import com.company.securityapp.entity.EnrollmentStatus;
import java.time.Instant;

public record AdminEnrollmentManagementResponse(
        Long enrollmentId,
        Long studentId,
        String studentName,
        String studentEmail,
        Long courseId,
        String courseTitle,
        EnrollmentStatus status,
        Instant createdAt,
        Instant activatedAt,
        boolean certificateIssued) {
}
