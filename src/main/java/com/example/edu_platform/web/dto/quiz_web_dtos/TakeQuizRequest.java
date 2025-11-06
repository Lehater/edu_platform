package com.example.edu_platform.web.dto.quiz_web_dtos;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record TakeQuizRequest(@NotNull Map<Long, List<Long>> answers) {
}