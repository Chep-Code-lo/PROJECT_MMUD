package com.company.securityapp.service;

import com.company.securityapp.dto.CourseDetailResponse;
import com.company.securityapp.dto.CourseRequest;
import com.company.securityapp.dto.CourseSummaryResponse;
import com.company.securityapp.dto.LessonPreviewResponse;
import com.company.securityapp.entity.Course;
import com.company.securityapp.entity.User;
import com.company.securityapp.exception.ApiException;
import com.company.securityapp.repository.CertificateRepository;
import com.company.securityapp.repository.CourseRepository;
import com.company.securityapp.repository.EnrollmentRepository;
import com.company.securityapp.repository.LessonRepository;
import com.company.securityapp.repository.UserRepository;
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
    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;

    public CourseService(
            CourseRepository courseRepository,
            LessonRepository lessonRepository,
            EnrollmentRepository enrollmentRepository,
            CertificateRepository certificateRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            AuthorizationService authorizationService) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
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

        boolean unlocked = actor != null && isCourseUnlockedForUser(actor, course);
        if (!course.isPublished() && (actor == null || (!authorizationService.canManageCourse(actor, course) && !unlocked))) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Course was not found.");
        }

        return toCourseDetail(course, unlocked);
    }

    public CourseDetailResponse createCourse(CourseRequest request) {
        User actor = currentUserService.getRequiredUser();
        authorizationService.assertCanCreateCourse(actor);

        Course course = new Course();
        applyCourseRequest(course, request, actor);
        Course savedCourse = courseRepository.save(course);
        return toCourseDetail(savedCourse, authorizationService.canManageCourse(actor, savedCourse));
    }

    public CourseDetailResponse updateCourse(Long courseId, CourseRequest request) {
        User actor = currentUserService.getRequiredUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course was not found."));
        authorizationService.assertCanManageCourse(actor, course);

        applyCourseRequest(course, request, actor);
        return toCourseDetail(courseRepository.save(course), isCourseUnlockedForUser(actor, course));
    }

    public void deleteCourse(Long courseId) {
        User actor = currentUserService.getRequiredUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course was not found."));
        authorizationService.assertCanManageCourse(actor, course);

        if (enrollmentRepository.countByCourseId(courseId) > 0 || certificateRepository.countByCourseId(courseId) > 0) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a course that already has enrollments or certificates. Unpublish it instead.");
        }

        lessonRepository.deleteAllByCourseId(courseId);
        courseRepository.delete(course);
    }

    @Transactional(readOnly = true)
    public Course getManagedCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course was not found."));
        authorizationService.assertCanManageCourse(currentUserService.getRequiredUser(), course);
        return course;
    }

    private void applyCourseRequest(Course course, CourseRequest request, User actor) {
        course.setTitle(request.title().trim());
        course.setSummary(request.summary().trim());
        course.setDescription(request.description().trim());
        course.setPrice(request.price());
        course.setPublished(request.published());

        if (authorizationService.isAdmin(actor) && request.instructorId() != null) {
            User instructor = userRepository.findById(request.instructorId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Instructor was not found."));
            if (!(authorizationService.isInstructor(instructor) || authorizationService.isAdmin(instructor))) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Assigned instructor must have INSTRUCTOR or ADMIN role.");
            }
            course.setInstructor(instructor);
            return;
        }

        course.setInstructor(actor);
    }

    private boolean isCourseUnlockedForUser(User actor, Course course) {
        return authorizationService.canManageCourse(actor, course)
                || enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                        actor.getId(),
                        course.getId(),
                        com.company.securityapp.entity.EnrollmentStatus.ACTIVE);
    }

    private CourseSummaryResponse toCourseSummary(Course course) {
        return new CourseSummaryResponse(
                course.getId(),
                course.getTitle(),
                course.getSummary(),
                course.getPrice(),
                course.isPublished(),
                course.getInstructor().getFullName());
    }

    private CourseDetailResponse toCourseDetail(Course course, boolean unlocked) {
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
                course.isPublished(),
                course.getInstructor().getFullName(),
                unlocked,
                lessons);
    }
}
