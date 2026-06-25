package com.company.securityapp.controller;

import com.company.securityapp.dto.AuthResponse;
import com.company.securityapp.dto.LoginRequest;
import com.company.securityapp.dto.LogoutRequest;
import com.company.securityapp.dto.MessageResponse;
import com.company.securityapp.dto.RefreshTokenRequest;
import com.company.securityapp.dto.RegisterRequest;
import com.company.securityapp.dto.UserResponse;
import com.company.securityapp.service.AuthService;
import com.company.securityapp.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Auth API", description = "JWT authentication, refresh token rotation, and protected user profile endpoints.")
public class AuthController {

    private final AuthService authService;
    private final RateLimitService rateLimitService;

    public AuthController(AuthService authService, RateLimitService rateLimitService) {
        this.authService = authService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new student account")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        rateLimitService.checkRegisterLimit(httpRequest, request.email());
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Authenticate with email and password to receive JWT and refresh token")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        rateLimitService.checkLoginLimit(httpRequest, request.email());
        return authService.login(request);
    }

    @PostMapping("/auth/refresh")
    @Operation(summary = "Exchange a valid refresh token for a new access token")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "Revoke a refresh token and log out")
    public MessageResponse logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return new MessageResponse("Refresh token revoked successfully.");
    }

    @GetMapping("/auth/me")
    @Operation(summary = "Get the current authenticated user profile")
    public UserResponse me() {
        return authService.getCurrentUserProfile();
    }

    @GetMapping("/users/{userId}/profile")
    @Operation(summary = "Get a specific user profile with server-side ownership checks")
    public UserResponse profile(@PathVariable Long userId) {
        return authService.getUserProfile(userId);
    }
}
