package com.company.securityapp.dto;

import java.math.BigDecimal;
import java.util.List;

public record CourseDetailResponse(
        Long id,
        String title,
        String summary,
        String description,
        BigDecimal price,
        boolean published,
        String instructorName,
        boolean enrolled,
        List<LessonPreviewResponse> lessons) {
}
