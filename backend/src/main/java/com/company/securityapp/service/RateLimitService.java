package com.company.securityapp.service;

import com.company.securityapp.entity.User;
import com.company.securityapp.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, FixedWindowCounter> counters = new ConcurrentHashMap<>();
    private final AuditLogService auditLogService;
    private final int loginLimit;
    private final int registerLimit;
    private final int passwordResetLimit;
    private final int adminLimit;

    public RateLimitService(
            AuditLogService auditLogService,
            @Value("${app.rate-limit.login-limit}") int loginLimit,
            @Value("${app.rate-limit.register-limit}") int registerLimit,
            @Value("${app.rate-limit.password-reset-limit}") int passwordResetLimit,
            @Value("${app.rate-limit.admin-limit}") int adminLimit) {
        this.auditLogService = auditLogService;
        this.loginLimit = loginLimit;
        this.registerLimit = registerLimit;
        this.passwordResetLimit = passwordResetLimit;
        this.adminLimit = adminLimit;
    }

    public void checkLoginLimit(HttpServletRequest request, String email) {
        enforceLimit("login", resolveIp(request) + "|" + normalize(email), loginLimit, "Login rate limit exceeded.");
    }

    public void checkRegisterLimit(HttpServletRequest request, String email) {
        enforceLimit(
                "register",
                resolveIp(request) + "|" + normalize(email),
                registerLimit,
                "Registration rate limit exceeded.");
    }

    public void checkPasswordResetLimit(HttpServletRequest request, String email) {
        enforceLimit(
                "password-reset",
                resolveIp(request) + "|" + normalize(email),
                passwordResetLimit,
                "Password reset rate limit exceeded.");
    }

    public void checkAdminLimit(HttpServletRequest request, User user) {
        enforceLimit(
                "admin",
                resolveIp(request) + "|" + user.getEmail().toLowerCase(),
                adminLimit,
                "Admin API rate limit exceeded.");
    }

    private void enforceLimit(String category, String subject, int limit, String message) {
        if (limit <= 0) {
            return;
        }

        String key = category + ":" + subject;
        FixedWindowCounter counter = counters.computeIfAbsent(key, unused -> new FixedWindowCounter());
        synchronized (counter) {
            Instant now = Instant.now();
            if (counter.windowStartedAt == null || now.isAfter(counter.windowStartedAt.plus(WINDOW))) {
                counter.windowStartedAt = now;
                counter.count = 0;
            }

            if (counter.count >= limit) {
                auditLogService.logSecurityEvent("RATE_LIMIT_EXCEEDED", "Request", null, "FAILED", message);
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, message);
            }

            counter.count++;
        }
    }

    private String resolveIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String normalize(String value) {
        return value == null ? "anonymous" : value.trim().toLowerCase();
    }

    private static final class FixedWindowCounter {
        private Instant windowStartedAt;
        private int count;
    }
}
