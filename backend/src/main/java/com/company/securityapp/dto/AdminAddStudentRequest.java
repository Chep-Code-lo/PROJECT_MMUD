package com.company.securityapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminAddStudentRequest(@NotBlank @Email String email) {
}
