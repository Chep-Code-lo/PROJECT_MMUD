package com.company.securityapp.service;

import com.company.securityapp.dto.AuthResponse;
import com.company.securityapp.dto.ForgotPasswordRequest;
import com.company.securityapp.dto.ForgotPasswordResponse;
import com.company.securityapp.dto.LoginRequest;
import com.company.securityapp.dto.LogoutRequest;
import com.company.securityapp.dto.RefreshTokenRequest;
import com.company.securityapp.dto.RegisterRequest;
import com.company.securityapp.dto.ResetPasswordRequest;
import com.company.securityapp.dto.UserResponse;
import com.company.securityapp.entity.PasswordResetToken;
import com.company.securityapp.entity.RefreshToken;
import com.company.securityapp.entity.Role;
import com.company.securityapp.entity.User;
import com.company.securityapp.exception.ApiException;
import com.company.securityapp.repository.PasswordResetTokenRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final EncryptionService encryptionService;
    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;
    private final long passwordResetExpireMinutes;
    private final boolean passwordResetDemoMode;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditLogService auditLogService,
            EncryptionService encryptionService,
            CurrentUserService currentUserService,
            AuthorizationService authorizationService,
            @Value("${app.password-reset.expire-minutes}") long passwordResetExpireMinutes,
            @Value("${app.password-reset.demo-mode}") boolean passwordResetDemoMode) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
        this.encryptionService = encryptionService;
        this.currentUserService = currentUserService;
        this.authorizationService = authorizationService;
        this.passwordResetExpireMinutes = passwordResetExpireMinutes;
        this.passwordResetDemoMode = passwordResetDemoMode;
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

    public ForgotPasswordResponse requestPasswordReset(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        String rawToken = generateOpaqueToken();

        if (user != null) {
            invalidateActivePasswordResetTokens(user.getId(), Instant.now());

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setTokenHash(hashOpaqueToken(rawToken));
            resetToken.setExpiresAt(Instant.now().plus(passwordResetExpireMinutes, ChronoUnit.MINUTES));
            passwordResetTokenRepository.save(resetToken);

            auditLogService.logForActor(
                    user.getId(),
                    user.getEmail(),
                    "PASSWORD_RESET_REQUESTED",
                    "User",
                    user.getId(),
                    "SUCCESS",
                    passwordResetDemoMode
                            ? "Password reset token issued in demo mode."
                            : "Password reset token issued.");
        } else {
            auditLogService.logForActor(
                    null,
                    email,
                    "PASSWORD_RESET_REQUESTED",
                    "User",
                    null,
                    "SUCCESS",
                    "Password reset request received for an unknown email.");
        }

        return new ForgotPasswordResponse(
                "If the email exists, a password reset instruction has been issued.",
                passwordResetDemoMode ? rawToken : null,
                passwordResetExpireMinutes * 60);
    }

    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken storedToken = findValidPasswordResetToken(request.token());
        User user = storedToken.getUser();
        Instant now = Instant.now();

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        invalidateActiveRefreshTokens(user.getId(), now);
        invalidateActivePasswordResetTokens(user.getId(), now);

        auditLogService.logForActor(
                user.getId(),
                user.getEmail(),
                "PASSWORD_RESET_CONFIRMED",
                "User",
                user.getId(),
                "SUCCESS",
                "Password reset completed and active refresh tokens were revoked.");
    }

    public void logout(LogoutRequest request) {
        User currentUser = currentUserService.getRequiredUser();
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hashOpaqueToken(request.refreshToken()))
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
        invalidateActiveRefreshTokens(user.getId(), Instant.now());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = generateOpaqueToken();

        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hashOpaqueToken(refreshToken));
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

    private void invalidateActiveRefreshTokens(Long userId, Instant revokedAt) {
        for (RefreshToken token : refreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(userId)) {
            token.setRevokedAt(revokedAt);
        }
    }

    private void invalidateActivePasswordResetTokens(Long userId, Instant usedAt) {
        for (PasswordResetToken token : passwordResetTokenRepository.findAllByUserIdAndUsedAtIsNull(userId)) {
            token.setUsedAt(usedAt);
        }
    }

    private RefreshToken findValidRefreshToken(String refreshToken) {
        String tokenHash = hashOpaqueToken(refreshToken);
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

    private PasswordResetToken findValidPasswordResetToken(String token) {
        String tokenHash = hashOpaqueToken(token);
        PasswordResetToken storedToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> invalidPasswordResetToken());

        if (storedToken.getUsedAt() != null || storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw invalidPasswordResetToken();
        }

        return storedToken;
    }

    private ApiException invalidCredentials(String email) {
        auditLogService.logAuthenticationFailure(email, "Invalid email or password.");
        return new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }

    private ApiException invalidPasswordResetToken() {
        auditLogService.logForActor(
                null,
                null,
                "PASSWORD_RESET_FAILED",
                "PasswordResetToken",
                null,
                "FAILED",
                "Password reset token is invalid, expired, or already used.");
        return new ApiException(HttpStatus.BAD_REQUEST, "Password reset token is invalid, expired, or already used.");
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

    private String generateOpaqueToken() {
        byte[] value = new byte[48];
        secureRandom.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private String hashOpaqueToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available on this runtime.", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
