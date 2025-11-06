package com.example.edu_platform.service.dto.quiz_dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateQuizCmd(
        @NotBlank String title,
        Integer timeLimitMinutes
) {}