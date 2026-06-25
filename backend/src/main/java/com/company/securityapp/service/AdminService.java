package com.company.securityapp.service;

import com.company.securityapp.dto.AdminSummaryResponse;
import com.company.securityapp.dto.AdminUserResponse;
import com.company.securityapp.dto.AuditLogResponse;
import com.company.securityapp.entity.EnrollmentStatus;
import com.company.securityapp.entity.User;
import com.company.securityapp.repository.AuditLogRepository;
import com.company.securityapp.repository.CertificateRepository;
import com.company.securityapp.repository.CourseRepository;
import com.company.securityapp.repository.EnrollmentRepository;
import com.company.securityapp.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CertificateRepository certificateRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;

    public AdminService(
            UserRepository userRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            CertificateRepository certificateRepository,
            AuditLogRepository auditLogRepository,
            AuditLogService auditLogService,
            CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.certificateRepository = certificateRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditLogService = auditLogService;
        this.currentUserService = currentUserService;
    }

    public List<AdminUserResponse> getUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toAdminUserResponse)
                .toList();
    }

    public List<AuditLogResponse> getAuditLogs() {
        User admin = currentUserService.getRequiredUser();
        auditLogService.logForActor(
                admin.getId(),
                admin.getEmail(),
                "AUDIT_LOG_VIEWED",
                "AuditLog",
                null,
                "SUCCESS",
                "Administrator viewed audit logs.");
        return auditLogService.getAuditLogs();
    }

    public AdminSummaryResponse getSummary() {
        return new AdminSummaryResponse(
                userRepository.count(),
                courseRepository.count(),
                enrollmentRepository.countByStatus(EnrollmentStatus.ACTIVE),
                certificateRepository.count(),
                auditLogRepository.count(),
                courseRepository.countByPublishedTrue());
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt());
    }
}
