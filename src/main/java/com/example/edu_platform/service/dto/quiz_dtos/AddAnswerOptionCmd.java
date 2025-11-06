package com.example.edu_platform.service.dto.quiz_dtos;

import jakarta.validation.constraints.NotBlank;

public record AddAnswerOptionCmd(
        @NotBlank String text,
        boolean isCorrect
) {}