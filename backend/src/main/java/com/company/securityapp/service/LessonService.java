package com.company.securityapp.service;

import com.company.securityapp.dto.LessonDetailResponse;
import com.company.securityapp.entity.Lesson;
import com.company.securityapp.entity.User;
import com.company.securityapp.exception.ApiException;
import com.company.securityapp.repository.LessonRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;
    private final EnrollmentService enrollmentService;
    private final AuditLogService auditLogService;

    public LessonService(
            LessonRepository lessonRepository,
            CurrentUserService currentUserService,
            AuthorizationService authorizationService,
            EnrollmentService enrollmentService,
            AuditLogService auditLogService) {
        this.lessonRepository = lessonRepository;
        this.currentUserService = currentUserService;
        this.authorizationService = authorizationService;
        this.enrollmentService = enrollmentService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public LessonDetailResponse getLessonDetail(Long courseId, Long lessonId) {
        User actor = currentUserService.getRequiredUser();
        Lesson lesson = findLesson(courseId, lessonId);

        boolean unlocked = enrollmentService.hasActiveEnrollment(actor.getId(), lesson.getCourse().getId());
        authorizationService.assertCanViewLesson(actor, lesson, unlocked);

        auditLogService.logForActor(
                actor.getId(),
                actor.getEmail(),
                "LESSON_VIEWED",
                "Lesson",
                lesson.getId(),
                "SUCCESS",
                "Lesson content accessed by an authorized user.");
        return toLessonDetail(lesson, true);
    }
    @Transactional(readOnly = true)
    protected Lesson findLesson(Long courseId, Long lessonId) {
        return lessonRepository.findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lesson was not found."));
    }

    private LessonDetailResponse toLessonDetail(Lesson lesson, boolean unlocked) {
        return new LessonDetailResponse(
                lesson.getId(),
                lesson.getCourse().getId(),
                lesson.getCourse().getTitle(),
                lesson.getTitle(),
                lesson.getPreviewText(),
                unlocked ? lesson.getContent() : null,
                lesson.getSortOrder(),
                unlocked);
    }
}
