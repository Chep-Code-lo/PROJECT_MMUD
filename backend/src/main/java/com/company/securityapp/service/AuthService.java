package com.company.securityapp.service;

import com.company.securityapp.dto.AuthResponse;
import com.company.securityapp.dto.LoginRequest;
import com.company.securityapp.dto.LogoutRequest;
import com.company.securityapp.dto.RefreshTokenRequest;
import com.company.securityapp.dto.RegisterRequest;
import com.company.securityapp.dto.UserResponse;
import com.company.securityapp.entity.RefreshToken;
import com.company.securityapp.entity.Role;
import com.company.securityapp.entity.User;
import com.company.securityapp.exception.ApiException;
import com.company.securityapp.repository.RefreshTokenRepository;
import com.company.securityapp.repository.UserRepository;
import com.company.securityapp.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final EncryptionService encryptionService;
    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditLogService auditLogService,
            EncryptionService encryptionService,
            CurrentUserService currentUserService,
            AuthorizationService authorizationService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
        this.encryptionService = encryptionService;
        this.currentUserService = currentUserService;
        this.authorizationService = authorizationService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered.");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setRole(Role.STUDENT);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhoneNumberEncrypted(encryptionService.encryptUserField(request.phoneNumber(), email, "phoneNumber"));
        user.setBillingAddressEncrypted(
                encryptionService.encryptUserField(request.billingAddress(), email, "billingAddress"));

        User savedUser = userRepository.save(user);
        auditLogService.logForActor(
                savedUser.getId(),
                savedUser.getEmail(),
                "REGISTER_SUCCESS",
                "User",
                savedUser.getId(),
                "SUCCESS",
                "Student account registered successfully.");
        return issueTokens(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> invalidCredentials(email));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials(email);
        }

        auditLogService.logAuthenticationSuccess(user);
        return issueTokens(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken storedToken = findValidRefreshToken(request.refreshToken());
        storedToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        auditLogService.logForActor(
                user.getId(),
                user.getEmail(),
                "TOKEN_REFRESHED",
                "RefreshToken",
                storedToken.getId(),
                "SUCCESS",
                "Refresh token rotated successfully.");
        return issueTokens(user);
    }

    public void logout(LogoutRequest request) {
        User currentUser = currentUserService.getRequiredUser();
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hashRefreshToken(request.refreshToken()))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Refresh token is invalid or already revoked."));

        if (!storedToken.getUser().getId().equals(currentUser.getId()) && !authorizationService.isAdmin(currentUser)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot revoke another user's refresh token.");
        }

        if (storedToken.getRevokedAt() == null) {
            storedToken.setRevokedAt(Instant.now());
            refreshTokenRepository.save(storedToken);
        }

        auditLogService.logForActor(
                currentUser.getId(),
                currentUser.getEmail(),
                "LOGOUT_SUCCESS",
                "RefreshToken",
                storedToken.getId(),
                "SUCCESS",
                "Refresh token revoked successfully.");
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        return toUserResponse(currentUserService.getRequiredUser());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Long userId) {
        User actor = currentUserService.getRequiredUser();
        authorizationService.assertSelfOrAdmin(actor, userId);
        User requestedUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User profile was not found."));
        return toUserResponse(requestedUser);
    }

    private AuthResponse issueTokens(User user) {
        revokeActiveRefreshTokens(user.getId());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = generateRefreshToken();

        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hashRefreshToken(refreshToken));
        entity.setExpiresAt(Instant.now().plus(jwtService.getRefreshTokenExpiresInSeconds(), ChronoUnit.SECONDS));
        refreshTokenRepository.save(entity);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpiresInSeconds(),
                jwtService.getRefreshTokenExpiresInSeconds(),
                user.getRole(),
                user.getRole().toScopeClaim(),
                toUserResponse(user));
    }

    private void revokeActiveRefreshTokens(Long userId) {
        for (RefreshToken token : refreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(userId)) {
            token.setRevokedAt(Instant.now());
        }
    }

    private RefreshToken findValidRefreshToken(String refreshToken) {
        String tokenHash = hashRefreshToken(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid."));

        if (storedToken.getRevokedAt() != null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token has been revoked.");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token has expired.");
        }

        return storedToken;
    }

    private ApiException invalidCredentials(String email) {
        auditLogService.logAuthenticationFailure(email, "Invalid email or password.");
        return new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                encryptionService.decryptUserField(user.getPhoneNumberEncrypted(), user.getEmail(), "phoneNumber"),
                encryptionService.decryptUserField(user.getBillingAddressEncrypted(), user.getEmail(), "billingAddress"),
                user.getCreatedAt());
    }

    private String generateRefreshToken() {
        byte[] value = new byte[48];
        secureRandom.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private String hashRefreshToken(String refreshToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(refreshToken.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available on this runtime.", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
