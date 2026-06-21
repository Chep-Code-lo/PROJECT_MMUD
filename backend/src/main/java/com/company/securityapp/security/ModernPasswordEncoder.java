package com.company.securityapp.security;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class ModernPasswordEncoder implements PasswordEncoder {

    private static final String BCRYPT_PREFIX = "{bcrypt}";
    private static final String ARGON2_PREFIX = "{argon2}";

    private final Argon2PasswordEncoder argon2PasswordEncoder;
    private final BCryptPasswordEncoder bcryptPasswordEncoder;

    public ModernPasswordEncoder() {
        this.argon2PasswordEncoder = new Argon2PasswordEncoder(16, 32, 1, 1 << 16, 3);
        this.bcryptPasswordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return argon2PasswordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String normalizedPassword = normalize(encodedPassword);
        if (normalizedPassword == null) {
            return false;
        }

        if (normalizedPassword.startsWith("$argon2")) {
            return argon2PasswordEncoder.matches(rawPassword, normalizedPassword);
        }

        if (normalizedPassword.startsWith("$2a$")
                || normalizedPassword.startsWith("$2b$")
                || normalizedPassword.startsWith("$2y$")) {
            return bcryptPasswordEncoder.matches(rawPassword, normalizedPassword);
        }

        return false;
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        String normalizedPassword = normalize(encodedPassword);
        if (normalizedPassword == null) {
            return false;
        }

        if (normalizedPassword.startsWith("$argon2")) {
            return argon2PasswordEncoder.upgradeEncoding(normalizedPassword);
        }

        if (normalizedPassword.startsWith("$2a$")
                || normalizedPassword.startsWith("$2b$")
                || normalizedPassword.startsWith("$2y$")) {
            return true;
        }

        return false;
    }

    private String normalize(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            return null;
        }

        if (encodedPassword.startsWith(ARGON2_PREFIX)) {
            return encodedPassword.substring(ARGON2_PREFIX.length());
        }

        if (encodedPassword.startsWith(BCRYPT_PREFIX)) {
            return encodedPassword.substring(BCRYPT_PREFIX.length());
        }

        return encodedPassword;
    }
}
