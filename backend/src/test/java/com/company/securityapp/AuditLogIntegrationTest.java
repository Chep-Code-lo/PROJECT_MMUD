package com.company.securityapp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.securityapp.entity.Role;
import com.company.securityapp.entity.User;
import com.company.securityapp.repository.AuditLogRepository;
import com.company.securityapp.repository.CustomerRepository;
import com.company.securityapp.repository.UserRepository;
import com.company.securityapp.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AuditLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void loginAndBusinessActionsProduceAuditLogs() throws Exception {
        User loginUser = new User();
        loginUser.setFullName("Login User");
        loginUser.setEmail("login-audit@example.test");
        loginUser.setPasswordHash(passwordEncoder.encode("Password@123"));
        loginUser.setRole(Role.USER);
        userRepository.save(loginUser);

        String successLoginPayload = objectMapper.writeValueAsString(Map.of(
                "email", "login-audit@example.test",
                "password", "Password@123"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(successLoginPayload))
                .andExpect(status().isOk());

        String failedLoginPayload = objectMapper.writeValueAsString(Map.of(
                "email", "login-audit@example.test",
                "password", "WrongPassword@123"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(failedLoginPayload))
                .andExpect(status().isUnauthorized());

        String staffToken = createTokenForRole(Role.STAFF, "staff-audit@example.test");
        String adminToken = createTokenForRole(Role.ADMIN, "admin-audit@example.test");

        String customerCreatePayload = objectMapper.writeValueAsString(Map.of(
                "name", "Audit Customer",
                "email", "audit-customer@example.test",
                "phone", "0909000111",
                "address", "Audit Street",
                "taxCode", "AUDIT-001"));

        String customerResponse = mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerCreatePayload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long customerId = objectMapper.readTree(customerResponse).get("id").asLong();

        String customerUpdatePayload = objectMapper.writeValueAsString(Map.of(
                "name", "Audit Customer Updated",
                "email", "audit-customer@example.test",
                "phone", "0909000222",
                "address", "Audit Street Updated",
                "taxCode", "AUDIT-002"));

        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerUpdatePayload))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/customers/{id}", customerId)
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.action=='LOGIN_SUCCESS')]").exists())
                .andExpect(jsonPath("$[?(@.action=='LOGIN_FAILED')]").exists())
                .andExpect(jsonPath("$[?(@.action=='CREATE_CUSTOMER')]").exists())
                .andExpect(jsonPath("$[?(@.action=='UPDATE_CUSTOMER')]").exists())
                .andExpect(jsonPath("$[?(@.action=='DELETE_CUSTOMER')]").exists());
    }

    private String createTokenForRole(Role role, String email) {
        User user = new User();
        user.setFullName(role.name() + " Demo");
        user.setEmail(email.toLowerCase());
        user.setPasswordHash(passwordEncoder.encode("Password@123"));
        user.setRole(role);
        User savedUser = userRepository.save(user);
        return jwtService.generateToken(savedUser);
    }
}
