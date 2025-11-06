package com.example.edu_platform.web.dto.quiz_web_dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateQuizRequest(@NotBlank String title, Integer timeLimitMinutes) {}