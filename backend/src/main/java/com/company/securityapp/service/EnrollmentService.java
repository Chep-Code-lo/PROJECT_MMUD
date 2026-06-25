package com.company.securityapp.service;

import com.company.securityapp.dto.CheckoutResponse;
import com.company.securityapp.dto.EnrollmentResponse;
import com.company.securityapp.entity.Course;
import com.company.securityapp.entity.Enrollment;
import com.company.securityapp.entity.EnrollmentStatus;
import com.company.securityapp.entity.User;
import com.company.securityapp.exception.ApiException;
import com.company.securityapp.repository.CertificateRepository;
import com.company.securityapp.repository.CourseRepository;
import com.company.securityapp.repository.EnrollmentRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final CertificateRepository certificateRepository;
    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;
    private final EncryptionService encryptionService;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository,
            CertificateRepository certificateRepository,
            CurrentUserService currentUserService,
            AuthorizationService authorizationService,
            EncryptionService encryptionService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.certificateRepository = certificateRepository;
        this.currentUserService = currentUserService;
        this.authorizationService = authorizationService;
        this.encryptionService = encryptionService;
    }

    public CheckoutResponse checkout(Long courseId) {
        User student = currentUserService.getRequiredUser();
        authorizationService.assertStudentOnly(
                student,
                "Course",
                courseId,
                "Administrator accounts cannot create course enrollments.");
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course was not found."));

        if (!course.isPublished() && !authorizationService.canManageCourse(student, course)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Course was not found.");
        }

        Enrollment existing = enrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId).orElse(null);
        if (existing != null && existing.getStatus() == EnrollmentStatus.ACTIVE) {
            throw new ApiException(HttpStatus.CONFLICT, "You are already enrolled in this course.");
        }

        Enrollment enrollment = existing == null ? new Enrollment() : existing;
        if (existing == null) {
            enrollment.setStudent(student);
            enrollment.setCourse(course);
        }

        String paymentReference = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        enrollment.setStatus(EnrollmentStatus.PENDING);
        enrollment.setActivatedAt(null);
        enrollment.setPaymentReferenceEncrypted(encryptionService.encryptEnrollmentField(
                paymentReference,
                student.getId(),
                course.getId(),
                "paymentReference"));
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return new CheckoutResponse(
                savedEnrollment.getId(),
                course.getId(),
                course.getTitle(),
                savedEnrollment.getStatus(),
                paymentReference,
                "evt_" + UUID.randomUUID().toString().replace("-", ""),
                Instant.now().getEpochSecond(),
                "/api/webhooks/payment-success",
                "Checkout request created successfully.");
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments() {
        User currentUser = currentUserService.getRequiredUser();
        authorizationService.assertStudentOnly(
                currentUser,
                "Enrollment",
                null,
                "Administrator accounts do not use the student enrollment area.");
        return enrollmentRepository.findAllByStudentIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollment(Long enrollmentId) {
        User actor = currentUserService.getRequiredUser();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enrollment was not found."));
        authorizationService.assertCanViewEnrollment(actor, enrollment);
        return toEnrollmentResponse(enrollment);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveEnrollment(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(studentId, courseId, EnrollmentStatus.ACTIVE);
    }

    public Enrollment activateEnrollment(Long enrollmentId, Long userId, Long courseId, String paymentReference) {
        Enrollment enrollment = getEnrollmentForWebhook(enrollmentId);

        if (!enrollment.getStudent().getId().equals(userId) || !enrollment.getCourse().getId().equals(courseId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Webhook payload does not match the target enrollment.");
        }

        if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            throw new ApiException(HttpStatus.CONFLICT, "Enrollment is already active.");
        }

        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setActivatedAt(Instant.now());
        enrollment.setPaymentReferenceEncrypted(encryptionService.encryptEnrollmentField(
                paymentReference,
                userId,
                courseId,
                "paymentReference"));
        return enrollmentRepository.save(enrollment);
    }

    @Transactional(readOnly = true)
    public Enrollment getEnrollmentForWebhook(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enrollment was not found."));
    }

    private EnrollmentResponse toEnrollmentResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getStatus(),
                enrollment.getCreatedAt(),
                enrollment.getActivatedAt(),
                certificateRepository.findByEnrollmentId(enrollment.getId()).isPresent());
    }
}
