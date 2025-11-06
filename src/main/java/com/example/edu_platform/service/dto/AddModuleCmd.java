package com.example.edu_platform.service.dto;

import jakarta.validation.constraints.NotBlank;

public record AddModuleCmd(
        @NotBlank String title,
        Integer orderIndex,
        String description
) {
}