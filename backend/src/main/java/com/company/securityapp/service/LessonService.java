package com.company.securityapp.service;

import com.company.securityapp.dto.LessonDetailResponse;
import com.company.securityapp.dto.LessonRequest;
import com.company.securityapp.entity.Course;
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
    private final CourseService courseService;

    public LessonService(
            LessonRepository lessonRepository,
            CurrentUserService currentUserService,
            AuthorizationService authorizationService,
            EnrollmentService enrollmentService,
            AuditLogService auditLogService,
            CourseService courseService) {
        this.lessonRepository = lessonRepository;
        this.currentUserService = currentUserService;
        this.authorizationService = authorizationService;
        this.enrollmentService = enrollmentService;
        this.auditLogService = auditLogService;
        this.courseService = courseService;
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

    public LessonDetailResponse createLesson(Long courseId, LessonRequest request) {
        Course course = courseService.getManagedCourse(courseId);

        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        applyRequest(lesson, request);
        return toLessonDetail(lessonRepository.save(lesson), true);
    }

    public LessonDetailResponse updateLesson(Long courseId, Long lessonId, LessonRequest request) {
        courseService.getManagedCourse(courseId);
        Lesson lesson = findLesson(courseId, lessonId);
        applyRequest(lesson, request);
        return toLessonDetail(lessonRepository.save(lesson), true);
    }

    @Transactional(readOnly = true)
    protected Lesson findLesson(Long courseId, Long lessonId) {
        return lessonRepository.findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lesson was not found."));
    }

    private void applyRequest(Lesson lesson, LessonRequest request) {
        lesson.setTitle(request.title().trim());
        lesson.setPreviewText(request.previewText().trim());
        lesson.setContent(request.content().trim());
        lesson.setSortOrder(request.sortOrder());
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
