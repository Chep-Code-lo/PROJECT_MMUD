package com.company.securityapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.securityapp.entity.Certificate;
import com.company.securityapp.entity.User;
import com.company.securityapp.repository.CertificateRepository;
import com.company.securityapp.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
        "spring.datasource.url=jdbc:h2:mem:bola_security_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "app.jwt.secret=test-jwt-secret-123456789012345678901234567890",
        "app.encryption.key=test-encryption-key-123456789012345678901234567890",
        "app.seed.enabled=true",
        "app.seed.student-password=Password123!",
        "app.seed.admin-password=Admin123!"
})
class ApiBolaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Test
    void studentCannotReadAnotherStudentsCertificateAndAdminCanSeeAuditLog() throws Exception {
        String student1Token = loginAndGetAccessToken("student1@example.com", "Password123!");
        String student2Token = loginAndGetAccessToken("student2@example.com", "Password123!");
        String adminToken = loginAndGetAccessToken("admin@example.com", "Admin123!");

        User student1 = userRepository.findByEmailIgnoreCase("student1@example.com").orElseThrow();
        Certificate certificate = certificateRepository.findAllByStudentIdOrderByIssuedAtDesc(student1.getId())
                .stream()
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/certificates/{certificateId}", certificate.getId())
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/certificates/{certificateId}", certificate.getId())
                        .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isForbidden());

        String auditResponse = mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode auditLogs = objectMapper.readTree(auditResponse);
        boolean foundDeniedCertificateLog = false;
        Iterator<JsonNode> iterator = auditLogs.elements();
        while (iterator.hasNext()) {
            JsonNode log = iterator.next();
            if ("ACCESS_DENIED".equals(log.path("action").asText())
                    && "Certificate".equals(log.path("targetType").asText())) {
                foundDeniedCertificateLog = true;
                break;
            }
        }

        assertThat(foundDeniedCertificateLog).isTrue();
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("accessToken").asText();
    }
}
