package com.company.securityapp.service;

import com.company.securityapp.dto.AuditLogResponse;
import com.company.securityapp.entity.AuditLog;
import com.company.securityapp.entity.User;
import com.company.securityapp.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final CurrentUserService currentUserService;

    public AuditLogService(AuditLogRepository auditLogRepository, CurrentUserService currentUserService) {
        this.auditLogRepository = auditLogRepository;
        this.currentUserService = currentUserService;
    }

    public List<AuditLogResponse> getAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(log -> new AuditLogResponse(
                        log.getId(),
                        log.getActorUserId(),
                        log.getActorEmail(),
                        log.getAction(),
                        log.getTargetType(),
                        log.getTargetId(),
                        log.getIpAddress(),
                        log.getUserAgent(),
                        log.getStatus(),
                        log.getMessage(),
                        log.getCreatedAt()))
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAuthenticationSuccess(User user) {
        logForActor(user.getId(), user.getEmail(), "LOGIN_SUCCESS", "User", user.getId(), "SUCCESS",
                "User logged in successfully.");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAuthenticationFailure(String email, String message) {
        logForActor(null, email == null ? null : email.trim().toLowerCase(), "LOGIN_FAILED", "User", null, "FAILED", message);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSecurityEvent(String action, String targetType, Long targetId, String status, String message) {
        User currentUser = currentUserService.getCurrentUserOrNull();
        logForActor(
                currentUser == null ? null : currentUser.getId(),
                currentUser == null ? null : currentUser.getEmail(),
                action,
                targetType,
                targetId,
                status,
                message);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logForActor(
            Long actorUserId,
            String actorEmail,
            String action,
            String targetType,
            Long targetId,
            String status,
            String message) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(actorUserId);
        auditLog.setActorEmail(actorEmail);
        auditLog.setAction(action);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);
        auditLog.setStatus(status);
        auditLog.setMessage(message);

        HttpServletRequest request = currentRequest();
        if (request != null) {
            auditLog.setIpAddress(resolveIpAddress(request));
            auditLog.setUserAgent(truncate(request.getHeader("User-Agent"), 512));
        }

        auditLogRepository.save(auditLog);
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return truncate(forwardedFor.split(",")[0].trim(), 128);
        }
        return truncate(request.getRemoteAddr(), 128);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }
}
