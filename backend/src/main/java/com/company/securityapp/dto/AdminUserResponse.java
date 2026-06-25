package com.company.securityapp.dto;

import com.company.securityapp.entity.Role;
import java.time.Instant;

public record AdminUserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        Instant createdAt) {
}
