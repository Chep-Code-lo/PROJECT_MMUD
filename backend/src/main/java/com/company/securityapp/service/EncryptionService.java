package com.company.securityapp.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BYTES = 16;
    private static final int TAG_BITS = 128;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptionService(@Value("${app.encryption.key}") String encryptionKey) {
        this.secretKey = new SecretKeySpec(deriveKey(encryptionKey), AES);
    }

    public String encryptUserField(String plaintext, String email, String fieldName) {
        return encrypt(plaintext, "user", normalize(email), fieldName);
    }

    public String decryptUserField(String ciphertext, String email, String fieldName) {
        return decrypt(ciphertext, "user", normalize(email), fieldName);
    }

    public String encryptEnrollmentField(String plaintext, Long studentId, Long courseId, String fieldName) {
        return encrypt(plaintext, "enrollment", studentId + "|" + courseId, fieldName);
    }

    public String decryptEnrollmentField(String ciphertext, Long studentId, Long courseId, String fieldName) {
        return decrypt(ciphertext, "enrollment", studentId + "|" + courseId, fieldName);
    }

    public String encryptCertificateField(String plaintext, Long studentId, Long courseId, String fieldName) {
        return encrypt(plaintext, "certificate", studentId + "|" + courseId, fieldName);
    }

    public String decryptCertificateField(String ciphertext, Long studentId, Long courseId, String fieldName) {
        return decrypt(ciphertext, "certificate", studentId + "|" + courseId, fieldName);
    }

    public String encryptDemoValue(String plaintext, String contextReference) {
        return encrypt(plaintext, "demo", contextReference, "value");
    }

    public String decryptDemoValue(String ciphertext, String contextReference) {
        return decrypt(ciphertext, "demo", contextReference, "value");
    }

    private String encrypt(String plaintext, String contextType, String contextReference, String fieldName) {
        if (plaintext == null || plaintext.isBlank()) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));
            cipher.updateAAD(buildAad(contextType, contextReference, fieldName));

            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] ciphertext = Arrays.copyOf(encrypted, encrypted.length - TAG_BYTES);
            byte[] tag = Arrays.copyOfRange(encrypted, encrypted.length - TAG_BYTES, encrypted.length);

            return Base64.getEncoder().encodeToString(iv)
                    + ":"
                    + Base64.getEncoder().encodeToString(ciphertext)
                    + ":"
                    + Base64.getEncoder().encodeToString(tag);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to encrypt protected data.", exception);
        }
    }

    private String decrypt(String payload, String contextType, String contextReference, String fieldName) {
        if (payload == null || payload.isBlank()) {
            return null;
        }

        try {
            String[] parts = payload.split(":");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Ciphertext payload format is invalid.");
            }

            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[1]);
            byte[] tag = Base64.getDecoder().decode(parts[2]);
            if (iv.length != IV_BYTES || tag.length != TAG_BYTES) {
                throw new IllegalArgumentException("Ciphertext payload structure is invalid.");
            }

            byte[] encrypted = new byte[ciphertext.length + tag.length];
            System.arraycopy(ciphertext, 0, encrypted, 0, ciphertext.length);
            System.arraycopy(tag, 0, encrypted, ciphertext.length, tag.length);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));
            cipher.updateAAD(buildAad(contextType, contextReference, fieldName));

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("Failed to decrypt protected data.", exception);
        }
    }

    private byte[] buildAad(String contextType, String contextReference, String fieldName) {
        return (contextType + "|" + normalize(contextReference) + "|" + normalize(fieldName))
                .getBytes(StandardCharsets.UTF_8);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Encryption context must not be blank.");
        }
        return value.trim().toLowerCase();
    }

    private byte[] deriveKey(String encryptionKey) {
        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw new IllegalStateException("ENCRYPTION_KEY must not be blank.");
        }

        try {
            return MessageDigest.getInstance("SHA-256").digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available on this runtime.", exception);
        }
    }
}
