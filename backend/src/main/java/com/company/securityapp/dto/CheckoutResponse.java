package com.company.securityapp.dto;

import com.company.securityapp.entity.EnrollmentStatus;

public record CheckoutResponse(
        Long enrollmentId,
        Long courseId,
        String courseTitle,
        EnrollmentStatus status,
        String paymentReference,
        String suggestedEventId,
        long suggestedTimestampEpochSeconds,
        String webhookPath,
        String message) {
}
