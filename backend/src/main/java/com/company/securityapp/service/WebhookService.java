package com.company.securityapp.service;

import com.company.securityapp.dto.MessageResponse;
import com.company.securityapp.dto.PaymentWebhookRequest;
import com.company.securityapp.entity.Enrollment;
import com.company.securityapp.entity.WebhookEvent;
import com.company.securityapp.exception.ApiException;
import com.company.securityapp.repository.WebhookEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WebhookService {

    private final ObjectMapper objectMapper;
    private final EnrollmentService enrollmentService;
    private final CertificateService certificateService;
    private final WebhookEventRepository webhookEventRepository;
    private final AuditLogService auditLogService;
    private final String hmacSecret;
    private final long maxAgeSeconds;

    public WebhookService(
            ObjectMapper objectMapper,
            EnrollmentService enrollmentService,
            CertificateService certificateService,
            WebhookEventRepository webhookEventRepository,
            AuditLogService auditLogService,
            @Value("${app.webhook.hmac-secret}") String hmacSecret,
            @Value("${app.webhook.max-age-seconds}") long maxAgeSeconds) {
        this.objectMapper = objectMapper;
        this.enrollmentService = enrollmentService;
        this.certificateService = certificateService;
        this.webhookEventRepository = webhookEventRepository;
        this.auditLogService = auditLogService;
        this.hmacSecret = hmacSecret;
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public MessageResponse handlePaymentSuccess(
            String rawBody,
            String signature,
            String timestampHeader,
            String eventId) {
        if (hmacSecret == null || hmacSecret.isBlank()) {
            throw new IllegalStateException("HMAC_WEBHOOK_SECRET must not be blank.");
        }

        if (signature == null || signature.isBlank() || timestampHeader == null || timestampHeader.isBlank()
                || eventId == null || eventId.isBlank()) {
            reject(null, HttpStatus.BAD_REQUEST, "Webhook requires X-Signature, X-Timestamp, and X-Event-Id headers.");
        }

        long timestamp = parseTimestamp(timestampHeader);
        if (Math.abs(Instant.now().getEpochSecond() - timestamp) > maxAgeSeconds) {
            reject(null, HttpStatus.UNAUTHORIZED, "Webhook timestamp is too old or too far in the future.");
        }

        if (webhookEventRepository.existsByEventId(eventId)) {
            auditLogService.logForActor(
                    null,
                    "payment-gateway",
                    "WEBHOOK_REPLAY_REJECTED",
                    "WebhookEvent",
                    null,
                    "FAILED",
                    "Replay attack detected for event " + eventId + ".");
            throw new ApiException(HttpStatus.CONFLICT, "Webhook event has already been processed.");
        }

        String expectedSignature = computeSignature(eventId, timestampHeader, rawBody);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                signature.trim().toLowerCase().getBytes(StandardCharsets.UTF_8))) {
            reject(null, HttpStatus.UNAUTHORIZED, "Webhook signature is invalid.");
        }

        PaymentWebhookRequest payload = parsePayload(rawBody);
        validatePayload(payload);

        Enrollment pendingEnrollment = enrollmentService.getEnrollmentForWebhook(payload.enrollmentId());
        if (payload.amount().compareTo(pendingEnrollment.getCourse().getPrice()) != 0) {
            reject(pendingEnrollment.getId(), HttpStatus.BAD_REQUEST, "Webhook amount does not match the course price.");
        }

        Enrollment enrollment = enrollmentService.activateEnrollment(
                payload.enrollmentId(),
                payload.userId(),
                payload.courseId(),
                payload.paymentReference());

        certificateService.issueCertificateForEnrollment(enrollment, BigDecimal.valueOf(92.50));

        WebhookEvent webhookEvent = new WebhookEvent();
        webhookEvent.setEventId(eventId.trim());
        webhookEvent.setEventType("payment.success");
        webhookEvent.setStatus("ACCEPTED");
        webhookEvent.setMessage("Webhook accepted and enrollment activated.");
        webhookEventRepository.save(webhookEvent);

        auditLogService.logForActor(
                null,
                "payment-gateway",
                "WEBHOOK_ACCEPTED",
                "Enrollment",
                enrollment.getId(),
                "SUCCESS",
                "Valid HMAC webhook activated an enrollment.");
        return new MessageResponse("Webhook accepted. Enrollment activated and certificate issued.");
    }

    private PaymentWebhookRequest parsePayload(String rawBody) {
        try {
            return objectMapper.readValue(rawBody, PaymentWebhookRequest.class);
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Webhook body is malformed JSON.");
        }
    }

    private void validatePayload(PaymentWebhookRequest payload) {
        if (payload.enrollmentId() == null || payload.userId() == null || payload.courseId() == null
                || payload.amount() == null || payload.amount().compareTo(BigDecimal.ZERO) < 0
                || payload.paymentReference() == null || payload.paymentReference().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Webhook body is missing required fields.");
        }
    }

    private long parseTimestamp(String timestampHeader) {
        try {
            return Long.parseLong(timestampHeader.trim());
        } catch (NumberFormatException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "X-Timestamp must be a UNIX epoch seconds value.");
        }
    }

    private String computeSignature(String eventId, String timestampHeader, String rawBody) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal((eventId.trim() + "." + timestampHeader.trim() + "." + rawBody)
                    .getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
            throw new IllegalStateException("Unable to compute HMAC-SHA256 signature.", exception);
        }
    }

    private void reject(Long enrollmentId, HttpStatus status, String message) {
        auditLogService.logForActor(
                null,
                "payment-gateway",
                "WEBHOOK_REJECTED",
                "Enrollment",
                enrollmentId,
                "FAILED",
                message);
        throw new ApiException(status, message);
    }
}
