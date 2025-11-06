package com.example.edu_platform.web.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollRequest(@NotNull Long userId) {
}