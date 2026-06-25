package com.company.securityapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CourseRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 500) String summary,
        @NotBlank @Size(max = 4000) String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
        boolean published,
        Long instructorId) {
}
