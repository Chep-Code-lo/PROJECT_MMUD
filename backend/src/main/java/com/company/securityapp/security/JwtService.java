package com.company.securityapp.security;

import com.company.securityapp.entity.Role;
import com.company.securityapp.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessExpireMinutes;
    private final long refreshExpireDays;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expire-minutes}") long accessExpireMinutes,
            @Value("${app.jwt.refresh-expire-days}") long refreshExpireDays) {
        this.signingKey = Keys.hmacShaKeyFor(deriveSigningKey(secret));
        this.accessExpireMinutes = accessExpireMinutes;
        this.refreshExpireDays = refreshExpireDays;
    }

    public String generateAccessToken(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(accessExpireMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("scope", user.getRole().toScopeClaim())
                .claim("token_type", "access")
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public JwtAccessTokenClaims parseAccessToken(String token) {
        Claims claims = parseClaims(token);
        String tokenType = claims.get("token_type", String.class);
        if (!"access".equals(tokenType)) {
            throw new TokenValidationException("Only access tokens are accepted for API requests.");
        }

        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new TokenValidationException("JWT subject is missing.");
        }

        String email = claims.get("email", String.class);
        if (email == null || email.isBlank()) {
            throw new TokenValidationException("JWT email claim is missing.");
        }

        String roleClaim = claims.get("role", String.class);
        if (roleClaim == null || roleClaim.isBlank()) {
            throw new TokenValidationException("JWT role claim is missing.");
        }

        Role role;
        try {
            role = Role.valueOf(roleClaim);
        } catch (IllegalArgumentException exception) {
            throw new TokenValidationException("JWT role claim is invalid.");
        }

        Long userId;
        try {
            userId = Long.parseLong(subject);
        } catch (NumberFormatException exception) {
            throw new TokenValidationException("JWT subject claim is invalid.");
        }

        String scopeClaim = claims.get("scope", String.class);
        List<String> scopes = scopeClaim == null || scopeClaim.isBlank()
                ? List.of()
                : List.of(scopeClaim.trim().split("\\s+"));

        return new JwtAccessTokenClaims(
                userId,
                email,
                role,
                scopes,
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant());
    }

    public long getAccessTokenExpiresInSeconds() {
        return accessExpireMinutes * 60;
    }

    public long getRefreshTokenExpiresInSeconds() {
        return refreshExpireDays * 24 * 60 * 60;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            throw new TokenValidationException("JWT access token has expired.");
        } catch (SignatureException exception) {
            throw new TokenValidationException("JWT signature is invalid.");
        } catch (MalformedJwtException | UnsupportedJwtException exception) {
            throw new TokenValidationException("JWT token format is invalid.");
        } catch (IllegalArgumentException exception) {
            throw new TokenValidationException("JWT token is empty or malformed.");
        }
    }

    private byte[] deriveSigningKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must not be blank.");
        }

        try {
            return MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available on this runtime.", exception);
        }
    }
}
