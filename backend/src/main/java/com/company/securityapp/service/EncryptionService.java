package com.company.securityapp.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

    private static final String AES_ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String HKDF_ALGORITHM = "HmacSHA256";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int AES_KEY_LENGTH_BYTES = 32;
    private static final int LEGACY_KEY_VERSION = 1;

    private final SecretKey legacySecretKey;
    private final byte[] masterSecret;
    private final byte[] hkdfSalt;
    private final int currentKeyVersion;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<Integer, SecretKey> derivedKeys = new ConcurrentHashMap<>();

    public EncryptionService(
            @Value("${app.aes.secret}") String secret,
            @Value("${app.aes.current-key-version}") int currentKeyVersion,
            @Value("${app.aes.hkdf-salt}") String hkdfSalt) {
        this.legacySecretKey = new SecretKeySpec(deriveLegacyKey(secret), AES_ALGORITHM);
        this.masterSecret = secret == null ? null : secret.getBytes(StandardCharsets.UTF_8);
        this.hkdfSalt = hkdfSalt == null ? new byte[0] : hkdfSalt.getBytes(StandardCharsets.UTF_8);
        this.currentKeyVersion = currentKeyVersion;

        if (currentKeyVersion < 2) {
            throw new IllegalStateException("app.aes.current-key-version must be at least 2.");
        }
    }

    public int getCurrentKeyVersion() {
        return currentKeyVersion;
    }

    /**
     * Legacy AES-GCM encryption kept only for compatibility with existing data/tests.
     */
    public String encrypt(String plaintext) {
        return encryptInternal(plaintext, null, LEGACY_KEY_VERSION);
    }

    /**
     * Legacy AES-GCM decryption kept only for compatibility with existing data/tests.
     */
    public String decrypt(String ciphertext) {
        return decryptInternal(ciphertext, null, LEGACY_KEY_VERSION);
    }

    public String encryptCustomerField(String plaintext, String customerReference, String fieldName) {
        return encryptInternal(plaintext, buildCustomerAad(customerReference, fieldName, currentKeyVersion), currentKeyVersion);
    }

    public String decryptCustomerField(
            String ciphertext,
            String customerReference,
            String fieldName,
            Integer keyVersion) {
        int effectiveKeyVersion = keyVersion == null ? LEGACY_KEY_VERSION : keyVersion;
        byte[] aad = effectiveKeyVersion <= LEGACY_KEY_VERSION
                ? null
                : buildCustomerAad(customerReference, fieldName, effectiveKeyVersion);
        return decryptInternal(ciphertext, aad, effectiveKeyVersion);
    }

    private String encryptInternal(String plaintext, byte[] aad, int keyVersion) {
        if (plaintext == null) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, resolveKey(keyVersion), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            if (aad != null && aad.length > 0) {
                cipher.updateAAD(aad);
            }

            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] payload = ByteBuffer.allocate(iv.length + encrypted.length)
                    .put(iv)
                    .put(encrypted)
                    .array();

            return Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to encrypt protected data.", exception);
        }
    }

    private String decryptInternal(String ciphertext, byte[] aad, int keyVersion) {
        if (ciphertext == null) {
            return null;
        }

        try {
            byte[] payload = Base64.getDecoder().decode(ciphertext);
            if (payload.length <= IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Ciphertext payload is invalid.");
            }

            byte[] iv = Arrays.copyOfRange(payload, 0, IV_LENGTH_BYTES);
            byte[] encrypted = Arrays.copyOfRange(payload, IV_LENGTH_BYTES, payload.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, resolveKey(keyVersion), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            if (aad != null && aad.length > 0) {
                cipher.updateAAD(aad);
            }

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("Failed to decrypt protected data.", exception);
        }
    }

    private SecretKey resolveKey(int keyVersion) {
        if (keyVersion <= LEGACY_KEY_VERSION) {
            return legacySecretKey;
        }

        return derivedKeys.computeIfAbsent(
                keyVersion,
                version -> new SecretKeySpec(deriveVersionedKey(version), AES_ALGORITHM));
    }

    private byte[] buildCustomerAad(String customerReference, String fieldName, int keyVersion) {
        if (customerReference == null || customerReference.isBlank()) {
            throw new IllegalStateException("Customer encryption reference must not be blank.");
        }
        if (fieldName == null || fieldName.isBlank()) {
            throw new IllegalStateException("Customer encryption field name must not be blank.");
        }

        String normalizedReference = customerReference.trim().toLowerCase();
        String normalizedFieldName = fieldName.trim().toLowerCase();
        String aad = "customers|" + normalizedReference + "|" + normalizedFieldName + "|v" + keyVersion;
        return aad.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] deriveVersionedKey(int keyVersion) {
        if (masterSecret == null || masterSecret.length == 0) {
            throw new IllegalStateException("app.aes.secret must not be blank.");
        }

        byte[] prk = hkdfExtract(hkdfSalt, masterSecret);
        return hkdfExpand(prk, ("securityapp:customer-data:v" + keyVersion).getBytes(StandardCharsets.UTF_8), AES_KEY_LENGTH_BYTES);
    }

    private byte[] hkdfExtract(byte[] salt, byte[] inputKeyMaterial) {
        try {
            Mac mac = Mac.getInstance(HKDF_ALGORITHM);
            byte[] effectiveSalt = (salt == null || salt.length == 0) ? new byte[mac.getMacLength()] : salt;
            mac.init(new SecretKeySpec(effectiveSalt, HKDF_ALGORITHM));
            return mac.doFinal(inputKeyMaterial);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to run HKDF extract.", exception);
        }
    }

    private byte[] hkdfExpand(byte[] pseudoRandomKey, byte[] info, int length) {
        try {
            Mac mac = Mac.getInstance(HKDF_ALGORITHM);
            mac.init(new SecretKeySpec(pseudoRandomKey, HKDF_ALGORITHM));

            byte[] result = new byte[length];
            byte[] previousBlock = new byte[0];
            int offset = 0;
            byte counter = 1;

            while (offset < length) {
                mac.reset();
                mac.update(previousBlock);
                if (info != null && info.length > 0) {
                    mac.update(info);
                }
                mac.update(counter);

                previousBlock = mac.doFinal();
                int bytesToCopy = Math.min(previousBlock.length, length - offset);
                System.arraycopy(previousBlock, 0, result, offset, bytesToCopy);

                offset += bytesToCopy;
                counter++;
            }

            return result;
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to run HKDF expand.", exception);
        }
    }

    private byte[] deriveLegacyKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.aes.secret must not be blank.");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available on this runtime.", exception);
        }
    }
}
