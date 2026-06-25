package com.company.securityapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.securityapp.entity.Course;
import com.company.securityapp.entity.Enrollment;
import com.company.securityapp.entity.EnrollmentStatus;
import com.company.securityapp.entity.User;
import com.company.securityapp.repository.CertificateRepository;
import com.company.securityapp.repository.CourseRepository;
import com.company.securityapp.repository.EnrollmentRepository;
import com.company.securityapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:webhook_security_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "app.jwt.secret=test-jwt-secret-123456789012345678901234567890",
        "app.encryption.key=test-encryption-key-123456789012345678901234567890",
        "app.webhook.hmac-secret=test-hmac-secret-123456789012345678901234567890",
        "app.seed.enabled=true",
        "app.seed.student-password=Password123!",
        "app.seed.instructor-password=Password123!",
        "app.seed.admin-password=Admin123!"
})
class WebhookSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Value("${app.webhook.hmac-secret}")
    private String webhookSecret;

    @Test
    void validWebhookUnlocksCourseAndReplayIsRejected() throws Exception {
        User student = userRepository.findByEmailIgnoreCase("student1@example.com").orElseThrow();
        Course course = courseRepository.findAllByPublishedTrueOrderByCreatedAtDesc()
                .stream()
                .filter(item -> "Secure RESTful API with Spring Boot".equals(item.getTitle()))
                .findFirst()
                .orElseThrow();
        Enrollment pendingEnrollment = enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId())
                .orElseThrow();

        String eventId = "evt_test_hmac_001";
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String body = objectMapper.writeValueAsString(Map.of(
                "enrollmentId", pendingEnrollment.getId(),
                "userId", student.getId(),
                "courseId", course.getId(),
                "paymentReference", "PAY-WEBHOOK-001",
                "amount", new BigDecimal("299000.00")));
        String signature = sign(eventId, timestamp, body);

        mockMvc.perform(post("/api/webhooks/payment-success")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-Event-Id", eventId)
                        .header("X-Timestamp", timestamp)
                        .header("X-Signature", signature))
                .andExpect(status().isOk());

        Enrollment activatedEnrollment = enrollmentRepository.findById(pendingEnrollment.getId()).orElseThrow();
        assertThat(activatedEnrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(certificateRepository.findByEnrollmentId(activatedEnrollment.getId())).isPresent();

        mockMvc.perform(post("/api/webhooks/payment-success")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-Event-Id", eventId)
                        .header("X-Timestamp", timestamp)
                        .header("X-Signature", signature))
                .andExpect(status().isConflict());
    }

    private String sign(String eventId, String timestamp, String rawBody)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal((eventId + "." + timestamp + "." + rawBody).getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}
