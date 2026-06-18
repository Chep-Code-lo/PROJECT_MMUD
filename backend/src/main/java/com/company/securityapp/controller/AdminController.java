package com.company.securityapp.controller;

import com.company.securityapp.repository.AuditLogRepository;
import com.company.securityapp.repository.CustomerRepository;
import com.company.securityapp.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminController(
            UserRepository userRepository,
            CustomerRepository customerRepository,
            AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/summary")
    public Map<String, Long> summary() {
        Map<String, Long> response = new LinkedHashMap<>();
        response.put("users", userRepository.count());
        response.put("customers", customerRepository.count());
        response.put("auditLogs", auditLogRepository.count());
        return response;
    }
}
