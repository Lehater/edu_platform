package com.example.edu_platform.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public record CreateCourseCmd(
        @NotBlank String title,
        String description,
        Long categoryId,
        @NotNull Long teacherId,
        String duration,
        LocalDate startDate,
        Set<Long> tagIds
) {
}