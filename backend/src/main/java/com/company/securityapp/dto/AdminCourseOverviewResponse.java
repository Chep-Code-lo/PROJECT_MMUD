package com.company.securityapp.dto;

import java.math.BigDecimal;

public record AdminCourseOverviewResponse(
        Long id,
        String title,
        String summary,
        BigDecimal price,
        String instructorName,
        long activeStudentCount,
        long pendingRequestCount) {
}
