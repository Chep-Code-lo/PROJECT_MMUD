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

    public boolean isInstructor(User actor) {
        return actor.getRole() == Role.INSTRUCTOR;
    }

    public boolean canManageCourse(User actor, Course course) {
        return isAdmin(actor)
                || (isInstructor(actor) && course.getInstructor() != null
                        && course.getInstructor().getId().equals(actor.getId()));
    }

    public void assertSelfOrAdmin(User actor, Long requestedUserId) {
        if (isAdmin(actor) || actor.getId().equals(requestedUserId)) {
            return;
        }
        deny(actor, "User", requestedUserId, "Students cannot access another user's profile.");
    }

    public void assertCanCreateCourse(User actor) {
        if (isAdmin(actor) || isInstructor(actor)) {
            return;
        }
        deny(actor, "Course", null, "Only instructors or admins can create courses.");
    }

    public void assertCanManageCourse(User actor, Course course) {
        if (canManageCourse(actor, course)) {
            return;
        }
        deny(actor, "Course", course.getId(), "You do not have permission to manage this course.");
    }

    public void assertCanViewEnrollment(User actor, Enrollment enrollment) {
        if (isAdmin(actor) || enrollment.getStudent().getId().equals(actor.getId())) {
            return;
        }
        deny(actor, "Enrollment", enrollment.getId(), "Students cannot access another student's enrollment.");
    }

    public void assertCanViewCertificate(User actor, Certificate certificate) {
        if (isAdmin(actor) || certificate.getStudent().getId().equals(actor.getId())) {
            return;
        }
        deny(actor, "Certificate", certificate.getId(), "Students cannot access another student's certificate.");
    }

    public void assertCanViewLesson(User actor, Lesson lesson, boolean unlocked) {
        if (unlocked || canManageCourse(actor, lesson.getCourse())) {
            return;
        }
        deny(
                actor,
                "Lesson",
                lesson.getId(),
                "Course lessons are only available to enrolled students or the course owner.");
    }

    private void deny(User actor, String targetType, Long targetId, String message) {
        auditLogService.logForActor(actor.getId(), actor.getEmail(), "ACCESS_DENIED", targetType, targetId, "FAILED", message);
        throw new ApiException(HttpStatus.FORBIDDEN, message);
    }
}
