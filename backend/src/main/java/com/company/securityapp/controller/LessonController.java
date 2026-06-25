package com.company.securityapp.controller;

import com.company.securityapp.dto.LessonDetailResponse;
import com.company.securityapp.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    @Operation(summary = "Get full lesson content only for an enrolled student")
    public LessonDetailResponse getLesson(@PathVariable Long courseId, @PathVariable Long lessonId) {
        return lessonService.getLessonDetail(courseId, lessonId);
    }
}
