package com.example.edu_platform.web.dto.quiz_web_dtos;

import jakarta.validation.constraints.NotBlank;

public record AddAnswerOptionRequest(@NotBlank String text, boolean isCorrect) {
}