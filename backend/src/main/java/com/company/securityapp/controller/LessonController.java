package com.company.securityapp.controller;

import com.company.securityapp.dto.LessonDetailResponse;
import com.company.securityapp.dto.LessonRequest;
import com.company.securityapp.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
@Tag(name = "Lesson API", description = "Lesson preview and full-content APIs with ownership/enrollment checks.")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/{lessonId}")
    @Operation(summary = "Get full lesson content if the caller is enrolled or owns the course")
    public LessonDetailResponse getLesson(@PathVariable Long courseId, @PathVariable Long lessonId) {
        return lessonService.getLessonDetail(courseId, lessonId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a lesson for a managed course")
    public LessonDetailResponse createLesson(@PathVariable Long courseId, @Valid @RequestBody LessonRequest request) {
        return lessonService.createLesson(courseId, request);
    }

    @PutMapping("/{lessonId}")
    @Operation(summary = "Update a lesson for a managed course")
    public LessonDetailResponse updateLesson(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonRequest request) {
        return lessonService.updateLesson(courseId, lessonId, request);
    }
}
