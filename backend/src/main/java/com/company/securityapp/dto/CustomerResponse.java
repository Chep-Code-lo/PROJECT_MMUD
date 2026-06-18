package com.company.securityapp.dto;

import java.time.Instant;

public record CustomerResponse(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        String taxCode,
        Instant createdAt,
        Instant updatedAt) {
}

