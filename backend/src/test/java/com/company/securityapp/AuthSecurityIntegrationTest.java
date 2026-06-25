package com.company.securityapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.securityapp.entity.User;
import com.company.securityapp.repository.AuditLogRepository;
import com.company.securityapp.repository.PasswordResetTokenRepository;
import com.company.securityapp.repository.RefreshTokenRepository;
import com.company.securityapp.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
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
        "spring.datasource.url=jdbc:h2:mem:auth_security_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "app.jwt.secret=test-jwt-secret-123456789012345678901234567890",
        "app.encryption.key=test-encryption-key-123456789012345678901234567890",
        "app.webhook.hmac-secret=test-hmac-secret-123456789012345678901234567890",
        "app.seed.enabled=false"
})
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        auditLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerLoginRefreshAndMeFlowUseBcryptAndJwt() throws Exception {
        Map<String, Object> registerPayload = new LinkedHashMap<>();
        registerPayload.put("fullName", "New Student");
        registerPayload.put("email", "newstudent@example.test");
        registerPayload.put("password", "Password123!");
        registerPayload.put("phoneNumber", "0909555666");
        registerPayload.put("billingAddress", "123 Demo Street");

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.user.email").value("newstudent@example.test"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        User storedUser = userRepository.findByEmailIgnoreCase("newstudent@example.test").orElseThrow();
        assertThat(storedUser.getPasswordHash()).startsWith("$2");
        assertThat(storedUser.getPasswordHash()).doesNotContain("Password123!");
        assertThat(storedUser.getPhoneNumberEncrypted()).isNotEqualTo("0909555666");

        Map<String, Object> loginPayload = Map.of(
                "email", "newstudent@example.test",
                "password", "Password123!");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String accessToken = loginJson.get("accessToken").asText();
        String refreshToken = loginJson.get("refreshToken").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newstudent@example.test"))
                .andExpect(jsonPath("$.phoneNumber").value("0909555666"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString());

        JsonNode registerJson = objectMapper.readTree(registerResponse);
        assertThat(registerJson.get("tokenType").asText()).isEqualTo("Bearer");
    }

    @Test
    void tamperedJwtPayloadIsRejected() throws Exception {
        Map<String, Object> registerPayload = Map.of(
                "fullName", "Tamper Student",
                "email", "tamper@example.test",
                "password", "Password123!");

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(registerResponse).get("accessToken").asText();
        String tamperedToken = tamperPayload(token);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("JWT signature is invalid."));
    }

    @Test
    void forgotPasswordResetFlowChangesPasswordAndRevokesRefreshTokens() throws Exception {
        Map<String, Object> registerPayload = Map.of(
                "fullName", "Reset Student",
                "email", "resetstudent@example.test",
                "password", "Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "resetstudent@example.test",
                                "password", "Password123!"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String originalRefreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        String forgotPasswordResponse = mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "resetstudent@example.test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email exists, a password reset instruction has been issued."))
                .andExpect(jsonPath("$.demoResetToken").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String resetToken = objectMapper.readTree(forgotPasswordResponse).get("demoResetToken").asText();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", resetToken,
                                "newPassword", "NewPassword123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset completed successfully."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "resetstudent@example.test",
                                "password", "Password123!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", originalRefreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token has been revoked."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "resetstudent@example.test",
                                "password", "NewPassword123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", resetToken,
                                "newPassword", "AnotherPassword123!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Password reset token is invalid, expired, or already used."));
    }

    @Test
    void forgotPasswordResponseIsUniformForKnownAndUnknownEmails() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Known Student",
                                "email", "knownstudent@example.test",
                                "password", "Password123!"))))
                .andExpect(status().isCreated());

        String knownResponse = mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "knownstudent@example.test"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String unknownResponse = mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "unknown@example.test"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode knownJson = objectMapper.readTree(knownResponse);
        JsonNode unknownJson = objectMapper.readTree(unknownResponse);

        assertThat(knownJson.get("message").asText())
                .isEqualTo("If the email exists, a password reset instruction has been issued.");
        assertThat(unknownJson.get("message").asText())
                .isEqualTo(knownJson.get("message").asText());
        assertThat(knownJson.get("expiresInSeconds").asLong())
                .isEqualTo(unknownJson.get("expiresInSeconds").asLong());
        assertThat(knownJson.get("demoResetToken").asText()).isNotBlank();
        assertThat(unknownJson.get("demoResetToken").asText()).isNotBlank();
    }

    private String tamperPayload(String token) throws Exception {
        String[] parts = token.split("\\.");
        JsonNode payload = objectMapper.readTree(Base64.getUrlDecoder().decode(parts[1]));
        ((com.fasterxml.jackson.databind.node.ObjectNode) payload).put("role", "ADMIN");
        parts[1] = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(objectMapper.writeValueAsBytes(payload));
        return String.join(".", parts);
    }
}
