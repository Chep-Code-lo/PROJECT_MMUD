package com.company.securityapp.dto;

import com.company.securityapp.entity.EnrollmentStatus;

public record EnrollmentRequestResponse(
        Long enrollmentId,
        Long courseId,
        String courseTitle,
        EnrollmentStatus status,
        String message) {
}
