package com.company.securityapp.service;

import com.company.securityapp.dto.CourseDetailResponse;
import com.company.securityapp.dto.CourseSummaryResponse;
import com.company.securityapp.dto.LessonPreviewResponse;
import com.company.securityapp.entity.Course;
import com.company.securityapp.entity.EnrollmentStatus;
import com.company.securityapp.entity.User;
import com.company.securityapp.exception.ApiException;
import com.company.securityapp.repository.CourseRepository;
import com.company.securityapp.repository.EnrollmentRepository;
import com.company.securityapp.repository.LessonRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;

    public CourseService(
            CourseRepository courseRepository,
            LessonRepository lessonRepository,
            EnrollmentRepository enrollmentRepository,
            CurrentUserService currentUserService,
            AuthorizationService authorizationService) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.currentUserService = currentUserService;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> getPublicCourses() {
        return courseRepository.findAllByPublishedTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toCourseSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseDetail(Long courseId) {
        User actor = currentUserService.getCurrentUserOrNull();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course was not found."));

        boolean enrolled = actor != null && hasStudentEnrollment(actor, course);
        boolean unlocked = actor != null && (authorizationService.canManageCourse(actor, course) || enrolled);
        if (!course.isPublished() && (actor == null || (!authorizationService.canManageCourse(actor, course) && !unlocked))) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Course was not found.");
        }

        return toCourseDetail(course, enrolled, unlocked);
    }

    private boolean hasStudentEnrollment(User actor, Course course) {
        return actor.getRole() == com.company.securityapp.entity.Role.STUDENT
                && enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                        actor.getId(),
                        course.getId(),
                        EnrollmentStatus.ACTIVE);
    }

    private CourseSummaryResponse toCourseSummary(Course course) {
        return new CourseSummaryResponse(
                course.getId(),
                course.getTitle(),
                course.getSummary(),
                course.getPrice(),
                course.getInstructor().getFullName());
    }

    private CourseDetailResponse toCourseDetail(Course course, boolean enrolled, boolean unlocked) {
        List<LessonPreviewResponse> lessons = lessonRepository.findAllByCourseIdOrderBySortOrderAsc(course.getId())
                .stream()
                .map(lesson -> new LessonPreviewResponse(
                        lesson.getId(),
                        lesson.getTitle(),
                        lesson.getPreviewText(),
                        lesson.getSortOrder(),
                        unlocked))
                .toList();

        return new CourseDetailResponse(
                course.getId(),
                course.getTitle(),
                course.getSummary(),
                course.getDescription(),
                course.getPrice(),
                course.getInstructor().getFullName(),
                enrolled,
                lessons);
    }
}
