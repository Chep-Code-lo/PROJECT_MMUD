package com.company.securityapp.dto;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        String action,
        String entityType,
        Long entityId,
        Long actorUserId,
        String actorEmail,
        boolean success,
        String details,
        Instant createdAt) {
}

