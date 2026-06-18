package com.company.securityapp.service;

import com.company.securityapp.dto.AuthResponse;
import com.company.securityapp.dto.LoginRequest;
import com.company.securityapp.dto.RegisterRequest;
import com.company.securityapp.dto.UserResponse;
import com.company.securityapp.entity.Role;
import com.company.securityapp.entity.User;
import com.company.securityapp.exception.ApiException;
import com.company.securityapp.repository.UserRepository;
import com.company.securityapp.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "User email already exists.");
        }

        User user = new User();
        user.setFullName(normalizeText(request.fullName()));
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        return toUserResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password()));
        } catch (AuthenticationException exception) {
            auditLogService.logAuthenticationFailure(normalizedEmail);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        String token = jwtService.generateToken(user);
        auditLogService.logAuthenticationSuccess(user);

        return new AuthResponse(
                token,
                "Bearer",
                user.getRole(),
                toUserResponse(user));
    }

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return toUserResponse(user);
        }

        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication is required."));
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }

    private String normalizeEmail(String email) {
        return normalizeText(email).toLowerCase();
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }
}

