package com.company.securityapp.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CertificateResponse(
        Long id,
        Long ownerUserId,
        String ownerEmail,
        Long courseId,
        String courseTitle,
        BigDecimal score,
        String certificateCode,
        Instant issuedAt) {
}
