package com.company.securityapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentWebhookRequest(
        @NotNull Long enrollmentId,
        @NotNull Long userId,
        @NotNull Long courseId,
        @NotBlank String paymentReference,
        @NotNull BigDecimal amount) {
}
