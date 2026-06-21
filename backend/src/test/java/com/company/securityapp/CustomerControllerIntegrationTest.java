package com.company.securityapp;

import static org.assertj.core.api.Assertions.assertThat;
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
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

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
    void createCustomerEncryptsSensitiveFieldsInDatabase() throws Exception {
        String staffToken = createTokenForRole(Role.STAFF, "staff-customer@example.test");
        String payload = objectMapper.writeValueAsString(Map.of(
                "name", "ACME Ltd",
                "email", "info@acme.test",
                "phone", "0909000999",
                "address", "123 Demo Street",
                "taxCode", "TAX-001"));

        mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phone").value("0909000999"))
                .andExpect(jsonPath("$.address").value("123 Demo Street"))
                .andExpect(jsonPath("$.taxCode").value("TAX-001"));

        Customer storedCustomer = customerRepository.findAll().get(0);
        assertThat(storedCustomer.getPhoneEncrypted()).isNotEqualTo("0909000999");
        assertThat(storedCustomer.getAddressEncrypted()).isNotEqualTo("123 Demo Street");
        assertThat(storedCustomer.getTaxCodeEncrypted()).isNotEqualTo("TAX-001");
        assertThat(storedCustomer.getKeyVersion()).isEqualTo(encryptionService.getCurrentKeyVersion());
    }

    @Test
    void createCustomerRejectsMissingRequiredFields() throws Exception {
        String staffToken = createTokenForRole(Role.STAFF, "staff-validation@example.test");
        String payload = objectMapper.writeValueAsString(Map.of(
                "name", " ",
                "email", "invalid",
                "phone", " ",
                "address", " ",
                "taxCode", " "));

        mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    @Test
    void legacyEncryptedCustomerWithoutKeyVersionCanStillBeRead() throws Exception {
        String staffToken = createTokenForRole(Role.STAFF, "staff-legacy@example.test");

        Customer customer = new Customer();
        customer.setName("Legacy Customer");
        customer.setEmail("legacy-customer@example.test");
        customer.setPhoneEncrypted(encryptionService.encrypt("0909111222"));
        customer.setAddressEncrypted(encryptionService.encrypt("Legacy Address"));
        customer.setTaxCodeEncrypted(encryptionService.encrypt("LEGACY-TAX"));
        customer.setKeyVersion(null);
        Customer savedCustomer = customerRepository.save(customer);

        mockMvc.perform(get("/api/customers/{id}", savedCustomer.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("0909111222"))
                .andExpect(jsonPath("$.address").value("Legacy Address"))
                .andExpect(jsonPath("$.taxCode").value("LEGACY-TAX"));
    }

    @Test
    void tamperedCiphertextReturnsGenericServerError() throws Exception {
        String staffToken = createTokenForRole(Role.STAFF, "staff-tamper@example.test");

        Customer customer = new Customer();
        customer.setName("Tampered Customer");
        customer.setEmail("tampered-customer@example.test");
        customer.setPhoneEncrypted(encryptionService.encryptCustomerField(
                "0909222333",
                customer.getEmail(),
                "phone"));
        customer.setAddressEncrypted(encryptionService.encryptCustomerField(
                "Tampered Address",
                customer.getEmail(),
                "address"));
        customer.setTaxCodeEncrypted(encryptionService.encryptCustomerField(
                "TAX-TAMPER",
                customer.getEmail(),
                "taxCode"));
        customer.setKeyVersion(encryptionService.getCurrentKeyVersion());
        Customer savedCustomer = customerRepository.save(customer);

        savedCustomer.setPhoneEncrypted(tamperBase64Payload(savedCustomer.getPhoneEncrypted()));
        customerRepository.save(savedCustomer);

        mockMvc.perform(get("/api/customers/{id}", savedCustomer.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected server error occurred."));
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

    private String tamperBase64Payload(String ciphertext) {
        char[] characters = ciphertext.toCharArray();
        int tamperIndex = Math.min(16, characters.length - 1);
        characters[tamperIndex] = characters[tamperIndex] == 'A' ? 'B' : 'A';
        return new String(characters);
    }
}
