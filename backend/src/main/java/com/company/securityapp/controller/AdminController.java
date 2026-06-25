package com.company.securityapp.controller;

import com.company.securityapp.dto.AdminAddStudentRequest;
import com.company.securityapp.dto.AdminCourseOverviewResponse;
import com.company.securityapp.dto.AdminCourseRosterResponse;
import com.company.securityapp.dto.AdminEnrollmentManagementResponse;
import com.company.securityapp.dto.AuditLogResponse;
import com.company.securityapp.dto.MessageResponse;
import com.company.securityapp.dto.UserResponse;
import com.company.securityapp.entity.User;
import com.company.securityapp.service.AdminService;
import com.company.securityapp.service.CurrentUserService;
import com.company.securityapp.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin API", description = "Administrative APIs for enrollment approval, student management, and security monitoring.")
public class AdminController {

    private final AdminService adminService;
    private final RateLimitService rateLimitService;
    private final CurrentUserService currentUserService;

    public AdminController(
            AdminService adminService,
            RateLimitService rateLimitService,
            CurrentUserService currentUserService) {
        this.adminService = adminService;
        this.rateLimitService = rateLimitService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs for security monitoring")
    public List<AuditLogResponse> auditLogs(HttpServletRequest request) {
        applyAdminLimit(request);
        return adminService.getAuditLogs();
    }

    @GetMapping("/courses")
    @Operation(summary = "List courses with active students and pending enrollment requests")
    public List<AdminCourseOverviewResponse> courses(HttpServletRequest request) {
        applyAdminLimit(request);
        return adminService.getCourseManagementOverview();
    }

    @GetMapping("/courses/{courseId}/enrollments")
    @Operation(summary = "Get pending requests and active students of a course")
    public AdminCourseRosterResponse courseEnrollments(
            @PathVariable Long courseId,
            HttpServletRequest request) {
        applyAdminLimit(request);
        return adminService.getCourseRoster(courseId);
    }

    @PostMapping("/courses/{courseId}/students")
    @Operation(summary = "Add a student directly into a course")
    public AdminEnrollmentManagementResponse addStudentToCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody AdminAddStudentRequest addStudentRequest,
            HttpServletRequest request) {
        applyAdminLimit(request);
        return adminService.addStudentToCourse(courseId, addStudentRequest);
    }

    @PostMapping("/enrollments/{enrollmentId}/approve")
    @Operation(summary = "Approve a pending course enrollment request")
    public AdminEnrollmentManagementResponse approveEnrollment(
            @PathVariable Long enrollmentId,
            HttpServletRequest request) {
        applyAdminLimit(request);
        return adminService.approveEnrollment(enrollmentId);
    }

    @DeleteMapping("/enrollments/{enrollmentId}")
    @Operation(summary = "Remove a student from a course or reject a pending request")
    public MessageResponse removeEnrollment(
            @PathVariable Long enrollmentId,
            HttpServletRequest request) {
        applyAdminLimit(request);
        adminService.removeEnrollment(enrollmentId);
        return new MessageResponse("Enrollment was removed successfully.");
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "View a student profile in the admin portal")
    public UserResponse userProfile(
            @PathVariable Long userId,
            HttpServletRequest request) {
        applyAdminLimit(request);
        return adminService.getStudentProfile(userId);
    }

    private void applyAdminLimit(HttpServletRequest request) {
        User currentUser = currentUserService.getRequiredUser();
        rateLimitService.checkAdminLimit(request, currentUser);
    }
}
