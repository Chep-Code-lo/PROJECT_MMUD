package com.company.securityapp.dto;

public record LessonDetailResponse(
        Long id,
        Long courseId,
        String courseTitle,
        String title,
        String previewText,
        String content,
        int sortOrder,
        boolean unlocked) {
}
