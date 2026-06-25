package com.company.securityapp.controller;

import com.company.securityapp.dto.AdminSummaryResponse;
import com.company.securityapp.dto.AdminUserResponse;
import com.company.securityapp.dto.AuditLogResponse;
import com.company.securityapp.entity.User;
import com.company.securityapp.service.AdminService;
import com.company.securityapp.service.CurrentUserService;
import com.company.securityapp.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin API", description = "Administrative APIs for demo users, audit logs, and summary statistics.")
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

    @GetMapping("/users")
    @Operation(summary = "List all users for admin review")
    public List<AdminUserResponse> users(HttpServletRequest request) {
        applyAdminLimit(request);
        return adminService.getUsers();
    }

    @GetMapping("/summary")
    @Operation(summary = "Get simple admin counters for demo purposes")
    public AdminSummaryResponse summary(HttpServletRequest request) {
        applyAdminLimit(request);
        return adminService.getSummary();
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs for security monitoring")
    public List<AuditLogResponse> auditLogs(HttpServletRequest request) {
        applyAdminLimit(request);
        return adminService.getAuditLogs();
    }

    private void applyAdminLimit(HttpServletRequest request) {
        User currentUser = currentUserService.getRequiredUser();
        rateLimitService.checkAdminLimit(request, currentUser);
    }
}
