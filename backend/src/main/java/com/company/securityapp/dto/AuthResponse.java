package com.company.securityapp.dto;

import com.company.securityapp.entity.Role;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresInSeconds,
        long refreshTokenExpiresInSeconds,
        Role role,
        String scope,
        UserResponse user) {
}
