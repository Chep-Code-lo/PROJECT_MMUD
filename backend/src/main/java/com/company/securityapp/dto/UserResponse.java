package com.company.securityapp.dto;

import com.company.securityapp.entity.Role;
import java.time.Instant;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        String phoneNumber,
        String billingAddress,
        Instant createdAt) {
}
