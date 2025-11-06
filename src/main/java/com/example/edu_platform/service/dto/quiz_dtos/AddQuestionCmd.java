package com.example.edu_platform.service.dto.quiz_dtos;

import com.example.edu_platform.domain.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddQuestionCmd(
        @NotNull QuestionType type,
        @NotBlank String text
) {}