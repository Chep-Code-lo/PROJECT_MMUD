package com.company.securityapp.controller;

import com.company.securityapp.dto.CourseDetailResponse;
import com.company.securityapp.dto.CourseSummaryResponse;
import com.company.securityapp.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Course API", description = "Public course catalogue endpoints used to demo enrollment and lesson protection.")
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
}
