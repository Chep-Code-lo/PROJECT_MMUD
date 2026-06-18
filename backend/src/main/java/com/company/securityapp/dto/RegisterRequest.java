package com.company.securityapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Full name is required.")
        @Size(max = 150, message = "Full name must be at most 150 characters.")
        String fullName,

        @NotBlank(message = "Email is required.")
        @Email(message = "Email format is invalid.")
        @Size(max = 255, message = "Email must be at most 255 characters.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
        String password) {
}

