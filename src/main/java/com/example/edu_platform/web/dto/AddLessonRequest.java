package com.example.edu_platform.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddLessonRequest(
        @NotBlank String title,
        String content,
        String videoUrl
) {}