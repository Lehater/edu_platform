package com.example.edu_platform.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddModuleRequest(
        @NotBlank String title,
        Integer orderIndex,
        String description
) {}