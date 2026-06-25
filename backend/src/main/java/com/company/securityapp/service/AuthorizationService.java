package com.company.securityapp.service;

import com.company.securityapp.entity.Certificate;
import com.company.securityapp.entity.Course;
import com.company.securityapp.entity.Enrollment;
import com.company.securityapp.entity.Lesson;
import com.company.securityapp.entity.Role;
import com.company.securityapp.entity.User;
import com.company.securityapp.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthorizationService {

    private final AuditLogService auditLogService;

    public AuthorizationService(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    public boolean isAdmin(User actor) {
        return actor.getRole() == Role.ADMIN;
    }

    public boolean canManageCourse(User actor, Course course) {
        return isAdmin(actor)
                || (course.getInstructor() != null && course.getInstructor().getId().equals(actor.getId()));
    }

    public void assertSelfOnly(User actor, Long requestedUserId) {
        if (actor.getRole() == Role.STUDENT && actor.getId().equals(requestedUserId)) {
            return;
        }
        deny(actor, "User", requestedUserId, "Only the owning student can access this profile.");
    }

    public void assertStudentOnly(User actor, String targetType, Long targetId, String message) {
        if (actor.getRole() == Role.STUDENT) {
            return;
        }
        deny(actor, targetType, targetId, message);
    }

    public void assertCanViewEnrollment(User actor, Enrollment enrollment) {
        if (actor.getRole() == Role.STUDENT && enrollment.getStudent().getId().equals(actor.getId())) {
            return;
        }
        deny(actor, "Enrollment", enrollment.getId(), "Only the owning student can access this enrollment.");
    }

    public void assertCanViewCertificate(User actor, Certificate certificate) {
        if (actor.getRole() == Role.STUDENT && certificate.getStudent().getId().equals(actor.getId())) {
            return;
        }
        deny(actor, "Certificate", certificate.getId(), "Only the owning student can access this certificate.");
    }

    public void assertCanViewLesson(User actor, Lesson lesson, boolean unlocked) {
        if (actor.getRole() == Role.STUDENT && unlocked) {
            return;
        }
        deny(
                actor,
                "Lesson",
                lesson.getId(),
                "Course lessons are only available to the enrolled student.");
    }

    private void deny(User actor, String targetType, Long targetId, String message) {
        auditLogService.logForActor(actor.getId(), actor.getEmail(), "ACCESS_DENIED", targetType, targetId, "FAILED", message);
        throw new ApiException(HttpStatus.FORBIDDEN, message);
    }
}
