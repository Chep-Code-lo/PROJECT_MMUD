package com.company.securityapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LessonRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 1000) String previewText,
        @NotBlank @Size(max = 10000) String content,
        @Min(1) int sortOrder) {
}
