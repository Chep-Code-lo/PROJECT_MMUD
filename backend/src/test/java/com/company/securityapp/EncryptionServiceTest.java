package com.company.securityapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.securityapp.service.EncryptionService;
import org.junit.jupiter.api.Test;

class EncryptionServiceTest {

    private final EncryptionService encryptionService =
            new EncryptionService("test-encryption-key-123456789012345678901234567890");

    @Test
    void aesGcmRoundTripWorksAndTamperingFails() {
        String ciphertext = encryptionService.encryptDemoValue("secret-demo-value", "demo-context");
        String plaintext = encryptionService.decryptDemoValue(ciphertext, "demo-context");

        assertThat(ciphertext).isNotEqualTo("secret-demo-value");
        assertThat(plaintext).isEqualTo("secret-demo-value");

        String tampered = ciphertext.substring(0, ciphertext.length() - 1)
                + (ciphertext.endsWith("A") ? "B" : "A");

        assertThatThrownBy(() -> encryptionService.decryptDemoValue(tampered, "demo-context"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to decrypt protected data.");
    }
}
