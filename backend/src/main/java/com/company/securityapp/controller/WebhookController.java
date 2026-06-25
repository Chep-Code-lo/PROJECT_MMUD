package com.company.securityapp.controller;

import com.company.securityapp.dto.MessageResponse;
import com.company.securityapp.service.RateLimitService;
import com.company.securityapp.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Webhook API", description = "HMAC-SHA256 protected payment webhook endpoints.")
public class WebhookController {

    private final WebhookService webhookService;
    private final RateLimitService rateLimitService;

    public WebhookController(WebhookService webhookService, RateLimitService rateLimitService) {
        this.webhookService = webhookService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping(value = "/payment-success", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Process a payment-success webhook signed with HMAC-SHA256")
    public MessageResponse paymentSuccess(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Event-Id", required = false) String eventId,
            HttpServletRequest request) {
        rateLimitService.checkWebhookLimit(request);
        return webhookService.handlePaymentSuccess(rawBody, signature, timestamp, eventId);
    }
}
