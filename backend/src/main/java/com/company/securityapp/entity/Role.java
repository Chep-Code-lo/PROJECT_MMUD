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
    // Retained for compatibility with older demo data. The current demo flow does not use this role directly.
    INSTRUCTOR(List.of(
            "course:read",
            "lesson:read",
            "enrollment:read",
            "certificate:read",
            "profile:read")),
    ADMIN(List.of(
            "course:read",
            "enrollment:manage",
            "user:read",
            "admin:read",
            "audit:read"));

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
