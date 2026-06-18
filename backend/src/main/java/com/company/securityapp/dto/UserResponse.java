package com.company.securityapp.dto;

import com.company.securityapp.entity.Role;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role) {
}

