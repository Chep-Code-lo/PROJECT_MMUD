package com.company.securityapp.service;

import com.company.securityapp.dto.AdminAddStudentRequest;
import com.company.securityapp.dto.AdminCourseOverviewResponse;
import com.company.securityapp.dto.AdminCourseRosterResponse;
import com.company.securityapp.dto.AdminEnrollmentManagementResponse;
import com.company.securityapp.dto.AuditLogResponse;
import com.company.securityapp.dto.UserResponse;
import com.company.securityapp.entity.Course;
import com.company.securityapp.entity.Enrollment;
import com.company.securityapp.entity.EnrollmentStatus;
import com.company.securityapp.entity.Role;
import com.company.securityapp.entity.User;
import com.company.securityapp.exception.ApiException;
import com.company.securityapp.repository.CertificateRepository;
import com.company.securityapp.repository.CourseRepository;
import com.company.securityapp.repository.EnrollmentRepository;
import com.company.securityapp.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminService {

    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CertificateRepository certificateRepository;
    private final EncryptionService encryptionService;

    public AdminService(
            AuditLogService auditLogService,
            CurrentUserService currentUserService,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            CertificateRepository certificateRepository,
            EncryptionService encryptionService) {
        this.auditLogService = auditLogService;
        this.currentUserService = currentUserService;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.certificateRepository = certificateRepository;
        this.encryptionService = encryptionService;
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogs() {
        User admin = currentUserService.getRequiredUser();
        auditLogService.logForActor(
                admin.getId(),
                admin.getEmail(),
                "AUDIT_LOG_VIEWED",
                "AuditLog",
                null,
                "SUCCESS",
                "Administrator viewed audit logs.");
        return auditLogService.getAuditLogs();
    }

    @Transactional(readOnly = true)
    public List<AdminCourseOverviewResponse> getCourseManagementOverview() {
        User admin = currentUserService.getRequiredUser();
        auditLogService.logForActor(
                admin.getId(),
                admin.getEmail(),
                "COURSE_ENROLLMENT_OVERVIEW_VIEWED",
                "Course",
                null,
                "SUCCESS",
                "Administrator viewed course enrollment overview.");
        return courseRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toCourseOverview)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminCourseRosterResponse getCourseRoster(Long courseId) {
        User admin = currentUserService.getRequiredUser();
        Course course = getRequiredCourse(courseId);
        List<AdminEnrollmentManagementResponse> activeStudents = enrollmentRepository
                .findAllByCourseIdAndStatusOrderByCreatedAtDesc(courseId, EnrollmentStatus.ACTIVE)
                .stream()
                .map(this::toAdminEnrollmentResponse)
                .toList();
        List<AdminEnrollmentManagementResponse> pendingRequests = enrollmentRepository
                .findAllByCourseIdAndStatusOrderByCreatedAtDesc(courseId, EnrollmentStatus.PENDING)
                .stream()
                .map(this::toAdminEnrollmentResponse)
                .toList();

        auditLogService.logForActor(
                admin.getId(),
                admin.getEmail(),
                "COURSE_ENROLLMENT_DETAIL_VIEWED",
                "Course",
                courseId,
                "SUCCESS",
                "Administrator viewed the enrollment roster of a course.");

        return new AdminCourseRosterResponse(
                course.getId(),
                course.getTitle(),
                course.getSummary(),
                course.getPrice(),
                course.getInstructor().getFullName(),
                activeStudents.size(),
                pendingRequests.size(),
                activeStudents,
                pendingRequests);
    }

    public AdminEnrollmentManagementResponse approveEnrollment(Long enrollmentId) {
        User admin = currentUserService.getRequiredUser();
        Enrollment enrollment = getRequiredEnrollment(enrollmentId);

        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Only pending enrollments can be approved.");
        }

        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setActivatedAt(Instant.now());
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        auditLogService.logForActor(
                admin.getId(),
                admin.getEmail(),
                "ENROLLMENT_APPROVED",
                "Enrollment",
                enrollmentId,
                "SUCCESS",
                "Administrator approved a student's course enrollment request.");
        return toAdminEnrollmentResponse(savedEnrollment);
    }

    public void removeEnrollment(Long enrollmentId) {
        User admin = currentUserService.getRequiredUser();
        Enrollment enrollment = getRequiredEnrollment(enrollmentId);

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new ApiException(HttpStatus.CONFLICT, "Enrollment has already been removed.");
        }

        boolean certificateIssued = certificateRepository.existsByEnrollmentId(enrollmentId);
        certificateRepository.findByEnrollmentId(enrollmentId).ifPresent(certificateRepository::delete);
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollment.setActivatedAt(null);
        enrollmentRepository.save(enrollment);

        auditLogService.logForActor(
                admin.getId(),
                admin.getEmail(),
                "ENROLLMENT_REMOVED",
                "Enrollment",
                enrollmentId,
                "SUCCESS",
                certificateIssued
                        ? "Administrator removed a student from the course and revoked the existing certificate."
                        : "Administrator removed a student from the course or rejected the pending request.");
    }

    public AdminEnrollmentManagementResponse addStudentToCourse(Long courseId, AdminAddStudentRequest addStudentRequest) {
        User admin = currentUserService.getRequiredUser();
        Course course = getRequiredCourse(courseId);
        String email = normalizeEmail(addStudentRequest.email());
        User student = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student account was not found."));

        if (student.getRole() != Role.STUDENT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only student accounts can be added to a course.");
        }

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseGet(() -> {
                    Enrollment createdEnrollment = new Enrollment();
                    createdEnrollment.setStudent(student);
                    createdEnrollment.setCourse(course);
                    return createdEnrollment;
                });

        if (enrollment.getId() != null && enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            throw new ApiException(HttpStatus.CONFLICT, "Student is already active in this course.");
        }

        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setActivatedAt(Instant.now());
        enrollment.setPaymentReferenceEncrypted(encryptionService.encryptEnrollmentField(
                "ADMIN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                student.getId(),
                courseId,
                "paymentReference"));
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        auditLogService.logForActor(
                admin.getId(),
                admin.getEmail(),
                "STUDENT_ADDED_TO_COURSE",
                "Enrollment",
                savedEnrollment.getId(),
                "SUCCESS",
                "Administrator added a student directly into a course.");
        return toAdminEnrollmentResponse(savedEnrollment);
    }

    @Transactional(readOnly = true)
    public UserResponse getStudentProfile(Long userId) {
        User admin = currentUserService.getRequiredUser();
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student profile was not found."));

        auditLogService.logForActor(
                admin.getId(),
                admin.getEmail(),
                "STUDENT_PROFILE_VIEWED",
                "User",
                userId,
                "SUCCESS",
                "Administrator viewed a student profile.");
        return new UserResponse(
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                student.getRole(),
                encryptionService.decryptUserField(student.getPhoneNumberEncrypted(), student.getEmail(), "phoneNumber"),
                encryptionService.decryptUserField(student.getBillingAddressEncrypted(), student.getEmail(), "billingAddress"),
                student.getCreatedAt());
    }

    private Course getRequiredCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course was not found."));
    }

    private Enrollment getRequiredEnrollment(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enrollment was not found."));
    }

    private AdminCourseOverviewResponse toCourseOverview(Course course) {
        return new AdminCourseOverviewResponse(
                course.getId(),
                course.getTitle(),
                course.getSummary(),
                course.getPrice(),
                course.getInstructor().getFullName(),
                enrollmentRepository.countByCourseIdAndStatus(course.getId(), EnrollmentStatus.ACTIVE),
                enrollmentRepository.countByCourseIdAndStatus(course.getId(), EnrollmentStatus.PENDING));
    }

    private AdminEnrollmentManagementResponse toAdminEnrollmentResponse(Enrollment enrollment) {
        return new AdminEnrollmentManagementResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getFullName(),
                enrollment.getStudent().getEmail(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getStatus(),
                enrollment.getCreatedAt(),
                enrollment.getActivatedAt(),
                certificateRepository.existsByEnrollmentId(enrollment.getId()));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
