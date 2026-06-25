package com.company.securityapp.security;

import com.company.securityapp.entity.Role;
import java.time.Instant;
import java.util.List;

public record JwtAccessTokenClaims(
        Long userId,
        String email,
        Role role,
        List<String> scopes,
        Instant issuedAt,
        Instant expiresAt) {
}
