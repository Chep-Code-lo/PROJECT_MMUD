package com.company.securityapp.entity;

import java.util.List;

public enum Role {
    STUDENT(List.of(
            "course:read",
            "lesson:read",
            "enrollment:read",
            "certificate:read",
            "profile:read",
            "checkout:create")),
    INSTRUCTOR(List.of(
            "course:read",
            "course:write",
            "lesson:read",
            "lesson:write",
            "enrollment:read",
            "certificate:read",
            "profile:read")),
    ADMIN(List.of(
            "course:read",
            "course:write",
            "lesson:read",
            "lesson:write",
            "enrollment:read",
            "certificate:read",
            "profile:read",
            "admin:read",
            "admin:write",
            "audit:read",
            "checkout:create"));

    private final List<String> scopes;

    Role(List<String> scopes) {
        this.scopes = scopes;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public String toScopeClaim() {
        return String.join(" ", scopes);
    }
}
