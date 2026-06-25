package com.company.securityapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
        "spring.datasource.url=jdbc:h2:mem:admin_role_separation_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "app.jwt.secret=test-jwt-secret-123456789012345678901234567890",
        "app.encryption.key=test-encryption-key-123456789012345678901234567890",
        "app.webhook.hmac-secret=test-hmac-secret-123456789012345678901234567890",
        "app.seed.enabled=true",
        "app.seed.student-password=Password123!",
        "app.seed.admin-password=Admin123!"
})
class AdminRoleSegregationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Test
    void adminCanManageEnrollmentsButCannotUseStudentOnlyFlows() throws Exception {
        String adminToken = loginAndGetAccessToken("admin@example.com", "Admin123!");
        String studentToken = loginAndGetAccessToken("student1@example.com", "Password123!");

        Course course = courseRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(item -> item.getTitle().equals("Secure RESTful API with Spring Boot"))
                .findFirst()
                .orElseThrow();
        User student1 = userRepository.findByEmailIgnoreCase("student1@example.com").orElseThrow();
        User student2 = userRepository.findByEmailIgnoreCase("student2@example.com").orElseThrow();
        Long certificateId = certificateRepository.findAllByStudentIdOrderByIssuedAtDesc(student1.getId())
                .stream()
                .findFirst()
                .orElseThrow()
                .getId();
        Enrollment pendingEnrollment = enrollmentRepository
                .findAllByCourseIdAndStatusOrderByCreatedAtDesc(course.getId(), EnrollmentStatus.PENDING)
                .stream()
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/admin/courses")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/courses/{courseId}/enrollments", course.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(course.getId()))
                .andExpect(jsonPath("$.pendingRequestCount").value(1));

        mockMvc.perform(post("/api/admin/enrollments/{enrollmentId}/approve", pendingEnrollment.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrollmentId").value(pendingEnrollment.getId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/admin/courses/{courseId}/students", course.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "student2@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentEmail").value("student2@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        Enrollment addedEnrollment = enrollmentRepository
                .findByStudentIdAndCourseId(student2.getId(), course.getId())
                .orElseThrow();

        mockMvc.perform(delete("/api/admin/enrollments/{enrollmentId}", addedEnrollment.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Enrollment was removed successfully."));

        Enrollment removedEnrollment = enrollmentRepository.findById(addedEnrollment.getId()).orElseThrow();
        assertThat(removedEnrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);

        mockMvc.perform(get("/api/admin/users/{userId}", student1.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student1.getId()))
                .andExpect(jsonPath("$.email").value("student1@example.com"));

        mockMvc.perform(get("/api/admin/courses")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/courses/{courseId}/checkout", course.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/enrollments/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/certificates/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users/{userId}/profile", student1.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/certificates/{certificateId}", certificateId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
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
