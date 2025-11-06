package com.example.edu_platform.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public record CreateCourseRequest(
        @NotBlank String title,
        String description,
        Long categoryId,
        @NotNull Long teacherId,
        String duration,
        LocalDate startDate,
        Set<Long> tagIds
) {}
