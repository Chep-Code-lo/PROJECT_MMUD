package com.company.securityapp.dto;

import java.math.BigDecimal;

public record CourseSummaryResponse(
        Long id,
        String title,
        String summary,
        BigDecimal price,
        boolean published,
        String instructorName) {
}
