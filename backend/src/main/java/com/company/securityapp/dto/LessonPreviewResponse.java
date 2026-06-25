package com.company.securityapp.dto;

public record LessonPreviewResponse(
        Long id,
        String title,
        String previewText,
        int sortOrder,
        boolean unlocked) {
}
