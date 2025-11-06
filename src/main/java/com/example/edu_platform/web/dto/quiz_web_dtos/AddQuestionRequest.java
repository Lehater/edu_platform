package com.example.edu_platform.web.dto.quiz_web_dtos;

import com.example.edu_platform.domain.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddQuestionRequest(@NotNull QuestionType type, @NotBlank String text) {
}