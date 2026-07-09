package com.company.securityapp.service;

import com.company.securityapp.dto.EnrollmentRequestResponse;
import com.company.securityapp.dto.EnrollmentResponse;
import com.company.securityapp.entity.Course;
import com.company.securityapp.entity.Enrollment;
import com.company.securityapp.entity.EnrollmentStatus;
import com.company.securityapp.entity.User;
import com.company.securityapp.exception.ApiException;
import com.company.securityapp.repository.CertificateRepository;
import com.company.securityapp.repository.CourseRepository;
import com.company.securityapp.repository.EnrollmentRepository;
import java.util.List;
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

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository,
            CertificateRepository certificateRepository,
            CurrentUserService currentUserService,
            AuthorizationService authorizationService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.certificateRepository = certificateRepository;
        this.currentUserService = currentUserService;
        this.authorizationService = authorizationService;
    }

    public EnrollmentRequestResponse requestEnrollment(Long courseId) {
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

        if (existing != null && existing.getStatus() == EnrollmentStatus.PENDING) {
            return new EnrollmentRequestResponse(
                    existing.getId(),
                    course.getId(),
                    course.getTitle(),
                    existing.getStatus(),
                    "Your enrollment request is already pending approval.");
        }

        Enrollment enrollment = existing == null ? new Enrollment() : existing;
        if (existing == null) {
            enrollment.setStudent(student);
            enrollment.setCourse(course);
        }

        enrollment.setStatus(EnrollmentStatus.PENDING);
        enrollment.setActivatedAt(null);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return new EnrollmentRequestResponse(
                savedEnrollment.getId(),
                course.getId(),
                course.getTitle(),
                savedEnrollment.getStatus(),
                "Enrollment request created successfully.");
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
