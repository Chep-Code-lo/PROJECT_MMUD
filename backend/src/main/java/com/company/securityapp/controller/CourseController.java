package com.company.securityapp.controller;

import com.company.securityapp.dto.CourseDetailResponse;
import com.company.securityapp.dto.CourseRequest;
import com.company.securityapp.dto.CourseSummaryResponse;
import com.company.securityapp.dto.MessageResponse;
import com.company.securityapp.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Course API", description = "Public course catalogue and admin/instructor course management endpoints.")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @Operation(summary = "List public courses")
    public List<CourseSummaryResponse> getCourses() {
        return courseService.getPublicCourses();
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Get course detail and lesson previews")
    public CourseDetailResponse getCourse(@PathVariable Long courseId) {
        return courseService.getCourseDetail(courseId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a course as instructor or admin")
    public CourseDetailResponse createCourse(@Valid @RequestBody CourseRequest request) {
        return courseService.createCourse(request);
    }

    @PutMapping("/{courseId}")
    @Operation(summary = "Update a managed course")
    public CourseDetailResponse updateCourse(@PathVariable Long courseId, @Valid @RequestBody CourseRequest request) {
        return courseService.updateCourse(courseId, request);
    }

    @DeleteMapping("/{courseId}")
    @Operation(summary = "Delete a course that has no enrollments or certificates")
    public MessageResponse deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return new MessageResponse("Course deleted successfully.");
    }
}
