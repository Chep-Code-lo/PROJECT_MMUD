package com.company.securityapp;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.securityapp.entity.Customer;
import com.company.securityapp.entity.Role;
import com.company.securityapp.entity.User;
import com.company.securityapp.repository.CustomerRepository;
import com.company.securityapp.repository.TicketRepository;
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
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void ticketCrudFlowWorks() throws Exception {
        Customer customer = createCustomer("customer1@example.test", "Customer One");
        String userToken = createTokenForRole(Role.USER, "user-ticket@example.test");

        String createPayload = objectMapper.writeValueAsString(Map.of(
                "customerId", customer.getId(),
                "title", "Install VPN",
                "description", "Configure VPN access for branch office",
                "priority", "HIGH",
                "createdById", 101));

        String createResponse = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customer.getId()))
                .andExpect(jsonPath("$.customerName").value("Customer One"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.phone").doesNotExist())
                .andExpect(jsonPath("$.address").doesNotExist())
                .andExpect(jsonPath("$.taxCode").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long ticketId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/tickets")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ticketId))
                .andExpect(jsonPath("$[0].customerId").value(customer.getId()));

        mockMvc.perform(get("/api/tickets/{id}", ticketId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId))
                .andExpect(jsonPath("$.title").value("Install VPN"))
                .andExpect(jsonPath("$.customerName").value("Customer One"))
                .andExpect(jsonPath("$.assignedToId").value(nullValue()));

        String updatePayload = objectMapper.writeValueAsString(Map.of(
                "customerId", customer.getId(),
                "title", "Install VPN - Updated",
                "description", "Configure VPN access for head office and branch office",
                "priority", "MEDIUM",
                "status", "PROCESSING",
                "createdById", 101,
                "assignedToId", 202));

        mockMvc.perform(put("/api/tickets/{id}", ticketId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Install VPN - Updated"))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.assignedToId").value(202));

        mockMvc.perform(delete("/api/tickets/{id}", ticketId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tickets")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void patchTicketStatusUpdatesStatus() throws Exception {
        Customer customer = createCustomer("customer2@example.test", "Customer Two");
        String userToken = createTokenForRole(Role.USER, "user-status@example.test");

        String createPayload = objectMapper.writeValueAsString(Map.of(
                "customerId", customer.getId(),
                "title", "Reset account",
                "description", "Reset remote account for employee",
                "priority", "LOW",
                "createdById", 55));

        String createResponse = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long ticketId = objectMapper.readTree(createResponse).get("id").asLong();

        String patchPayload = objectMapper.writeValueAsString(Map.of(
                "status", "RESOLVED"));

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId))
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.customerName").value("Customer Two"));
    }

    @Test
    void createTicketRejectsNonExistentCustomerId() throws Exception {
        String userToken = createTokenForRole(Role.USER, "user-missing-customer@example.test");
        String payload = objectMapper.writeValueAsString(Map.of(
                "customerId", 999999,
                "title", "Ticket without valid customer",
                "description", "This should fail",
                "priority", "HIGH",
                "createdById", 1));

        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found."))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    private Customer createCustomer(String email, String name) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhoneEncrypted(encryptionService.encrypt("0900000000"));
        customer.setAddressEncrypted(encryptionService.encrypt("Demo address"));
        customer.setTaxCodeEncrypted(encryptionService.encrypt("TAX-DEMO"));
        return customerRepository.save(customer);
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
