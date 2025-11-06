package com.example.edu_platform.service.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateAssignmentCmd(
        @NotBlank String title,
        String description,
        LocalDate dueDate,
        @NotNull @Min(1) Integer maxScore
) {
}
