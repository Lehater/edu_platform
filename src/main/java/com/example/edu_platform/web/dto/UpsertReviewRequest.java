package com.example.edu_platform.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpsertReviewRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        String comment
) {
}