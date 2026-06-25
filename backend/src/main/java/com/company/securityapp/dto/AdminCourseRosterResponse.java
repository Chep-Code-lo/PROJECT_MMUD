package com.company.securityapp.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminCourseRosterResponse(
        Long courseId,
        String courseTitle,
        String courseSummary,
        BigDecimal price,
        String instructorName,
        long activeStudentCount,
        long pendingRequestCount,
        List<AdminEnrollmentManagementResponse> activeStudents,
        List<AdminEnrollmentManagementResponse> pendingRequests) {
}
