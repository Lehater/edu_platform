package com.example.edu_platform.service.dto.review_dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpsertReviewCmd(
        @NotNull @Min(1) @Max(5) Integer rating,
        String comment
) {}