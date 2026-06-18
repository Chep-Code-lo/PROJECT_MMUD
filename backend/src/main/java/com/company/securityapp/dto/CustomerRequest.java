package com.company.securityapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "Name is required.")
        @Size(max = 150, message = "Name must be at most 150 characters.")
        String name,

        @NotBlank(message = "Email is required.")
        @Email(message = "Email format is invalid.")
        @Size(max = 255, message = "Email must be at most 255 characters.")
        String email,

        @NotBlank(message = "Phone is required.")
        @Size(max = 50, message = "Phone must be at most 50 characters.")
        String phone,

        @NotBlank(message = "Address is required.")
        @Size(max = 500, message = "Address must be at most 500 characters.")
        String address,

        @NotBlank(message = "Tax code is required.")
        @Size(max = 50, message = "Tax code must be at most 50 characters.")
        String taxCode) {
}

