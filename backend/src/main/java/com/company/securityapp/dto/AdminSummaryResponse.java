package com.company.securityapp.dto;

public record AdminSummaryResponse(
        long users,
        long courses,
        long activeEnrollments,
        long certificates,
        long auditLogs,
        long publishedCourses) {
}
