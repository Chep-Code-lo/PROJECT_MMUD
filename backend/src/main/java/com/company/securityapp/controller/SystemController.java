package com.company.securityapp.controller;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SystemController {

    private final Environment environment;

    public SystemController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("service", "securityapp-backend");
        response.put("activeProfiles", Arrays.asList(environment.getActiveProfiles()));
        return response;
    }
}
