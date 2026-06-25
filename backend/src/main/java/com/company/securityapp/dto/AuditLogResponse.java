package com.company.securityapp.dto;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Long actorUserId,
        String actorEmail,
        String action,
        String targetType,
        Long targetId,
        String ipAddress,
        String userAgent,
        String status,
        String message,
        Instant createdAt) {
}
