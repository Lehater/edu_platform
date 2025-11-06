package com.example.edu_platform.service.dto;

import jakarta.validation.constraints.NotBlank;

public record AddLessonCmd(
        @NotBlank String title,
        String content,
        String videoUrl
) {
}