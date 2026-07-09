package com.company.securityapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.securityapp.entity.Course;
import com.company.securityapp.entity.Enrollment;
import com.company.securityapp.entity.EnrollmentStatus;
import com.company.securityapp.entity.User;
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
        "spring.datasource.url=jdbc:h2:mem:enrollment_request_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "app.jwt.secret=test-jwt-secret-123456789012345678901234567890",
        "app.encryption.key=test-encryption-key-123456789012345678901234567890",
        "app.seed.enabled=true",
        "app.seed.student-password=Password123!",
        "app.seed.admin-password=Admin123!"
})
class EnrollmentRequestIntegrationTest {

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

    @Test
    void studentCanCreatePendingEnrollmentRequestAndRepeatCallDoesNotDuplicate() throws Exception {
        String studentToken = loginAndGetAccessToken("student2@example.com", "Password123!");
        User student = userRepository.findByEmailIgnoreCase("student2@example.com").orElseThrow();
        Course targetCourse = courseRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(item -> "Secure RESTful API with Spring Boot".equals(item.getTitle()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/api/courses/{courseId}/enrollment-requests", targetCourse.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(targetCourse.getId()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        Enrollment pendingEnrollment = enrollmentRepository.findByStudentIdAndCourseId(student.getId(), targetCourse.getId())
                .orElseThrow();
        assertThat(pendingEnrollment.getStatus()).isEqualTo(EnrollmentStatus.PENDING);

        mockMvc.perform(post("/api/courses/{courseId}/enrollment-requests", targetCourse.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrollmentId").value(pendingEnrollment.getId()))
                .andExpect(jsonPath("$.message").value("Your enrollment request is already pending approval."));

        assertThat(enrollmentRepository.findAllByCourseIdAndStatusOrderByCreatedAtDesc(
                        targetCourse.getId(), EnrollmentStatus.PENDING))
                .filteredOn(item -> item.getStudent().getId().equals(student.getId()))
                .hasSize(1);
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
