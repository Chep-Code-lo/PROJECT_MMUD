package com.company.securityapp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.securityapp.entity.Customer;
import com.company.securityapp.entity.Role;
import com.company.securityapp.entity.User;
import com.company.securityapp.repository.CustomerRepository;
import com.company.securityapp.repository.UserRepository;
import com.company.securityapp.security.JwtService;
import com.company.securityapp.service.EncryptionService;
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
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerLoginAndMeFlowWorks() throws Exception {
        String registerPayload = objectMapper.writeValueAsString(Map.of(
                "fullName", "New User",
                "email", "newuser@example.test",
                "password", "Password@123"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.test"))
                .andExpect(jsonPath("$.role").value("USER"));

        String loginPayload = objectMapper.writeValueAsString(Map.of(
                "email", "newuser@example.test",
                "password", "Password@123"));

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.user.email").value("newuser@example.test"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newuser@example.test"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void missingOrInvalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void roleAuthorizationMatrixIsApplied() throws Exception {
        String adminToken = createTokenForRole(Role.ADMIN, "admin-role@example.test");
        String staffToken = createTokenForRole(Role.STAFF, "staff-role@example.test");
        String userToken = createTokenForRole(Role.USER, "user-role@example.test");

        Customer customer = new Customer();
        customer.setName("Role Customer");
        customer.setEmail("role-customer@example.test");
        customer.setPhoneEncrypted(encryptionService.encrypt("0900000000"));
        customer.setAddressEncrypted(encryptionService.encrypt("Demo address"));
        customer.setTaxCodeEncrypted(encryptionService.encrypt("TAX-ROLE"));
        customerRepository.save(customer);

        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(get("/api/admin/summary")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(get("/api/admin/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isNumber());
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
