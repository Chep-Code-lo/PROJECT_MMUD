package com.company.securityapp.controller;

import com.company.securityapp.dto.AuthResponse;
import com.company.securityapp.dto.LoginRequest;
import com.company.securityapp.dto.RegisterRequest;
import com.company.securityapp.dto.UserResponse;
import com.company.securityapp.service.AuthService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully."),
        @ApiResponse(responseCode = "409", description = "User email already exists.")
    })
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful."),
        @ApiResponse(responseCode = "401", description = "Invalid email or password.")
    })
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Current user returned."),
        @ApiResponse(responseCode = "401", description = "Authentication required.")
    })
    public UserResponse me() {
        return authService.getCurrentUser();
    }
}

