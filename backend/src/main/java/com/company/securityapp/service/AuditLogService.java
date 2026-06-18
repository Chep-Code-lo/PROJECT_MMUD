package com.company.securityapp.service;

import com.company.securityapp.dto.AuditLogResponse;
import com.company.securityapp.entity.AuditLog;
import com.company.securityapp.entity.User;
import com.company.securityapp.repository.AuditLogRepository;
import java.util.List;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLogResponse> getAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBusinessAction(String action, String entityType, Long entityId, boolean success, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setSuccess(success);
        auditLog.setDetails(details);
        fillActorFromSecurityContext(auditLog);
        auditLogRepository.save(auditLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAuthenticationSuccess(User user) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("LOGIN_SUCCESS");
        auditLog.setEntityType("User");
        auditLog.setEntityId(user.getId());
        auditLog.setActorUserId(user.getId());
        auditLog.setActorEmail(user.getEmail());
        auditLog.setSuccess(true);
        auditLog.setDetails("User logged in successfully.");
        auditLogRepository.save(auditLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAuthenticationFailure(String email) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("LOGIN_FAILED");
        auditLog.setEntityType("User");
        auditLog.setActorEmail(email == null ? null : email.trim().toLowerCase());
        auditLog.setSuccess(false);
        auditLog.setDetails("Login failed because credentials were invalid.");
        auditLogRepository.save(auditLog);
    }

    private void fillActorFromSecurityContext(AuditLog auditLog) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            auditLog.setActorUserId(user.getId());
            auditLog.setActorEmail(user.getEmail());
            return;
        }

        auditLog.setActorEmail(authentication.getName());
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getActorUserId(),
                auditLog.getActorEmail(),
                auditLog.isSuccess(),
                auditLog.getDetails(),
                auditLog.getCreatedAt());
    }
}

