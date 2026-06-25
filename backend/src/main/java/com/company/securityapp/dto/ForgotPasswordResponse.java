package com.company.securityapp.dto;

public record ForgotPasswordResponse(
        String message,
        String demoResetToken,
        long expiresInSeconds) {
}
