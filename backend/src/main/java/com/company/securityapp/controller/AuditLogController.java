package com.company.securityapp.controller;

import com.company.securityapp.dto.AuditLogResponse;
import com.company.securityapp.service.AuditLogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLogResponse> getAuditLogs() {
        return auditLogService.getAuditLogs();
    }
}

