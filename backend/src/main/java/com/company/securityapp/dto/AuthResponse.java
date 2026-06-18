package com.company.securityapp.dto;

import com.company.securityapp.entity.Role;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Role role,
        UserResponse user) {
}

