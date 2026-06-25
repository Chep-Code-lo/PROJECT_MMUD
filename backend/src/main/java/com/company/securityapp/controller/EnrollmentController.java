package com.company.securityapp.controller;

import com.company.securityapp.dto.CheckoutResponse;
import com.company.securityapp.dto.EnrollmentResponse;
import com.company.securityapp.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Enrollment API", description = "Checkout and enrollment APIs used to demo payment/webhook security and BOLA protection.")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/courses/{courseId}/checkout")
    @Operation(summary = "Create or refresh a pending enrollment for mock checkout")
    public CheckoutResponse checkout(@PathVariable Long courseId) {
        return enrollmentService.checkout(courseId);
    }

    @GetMapping("/enrollments/me")
    @Operation(summary = "List the current user's enrollments")
    public List<EnrollmentResponse> myEnrollments() {
        return enrollmentService.getMyEnrollments();
    }

    @GetMapping("/enrollments/{enrollmentId}")
    @Operation(summary = "Get an enrollment with ownership checks to block BOLA/IDOR")
    public EnrollmentResponse getEnrollment(@PathVariable Long enrollmentId) {
        return enrollmentService.getEnrollment(enrollmentId);
    }
}
