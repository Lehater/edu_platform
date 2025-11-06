package com.example.edu_platform.service.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitAssignmentCmd(
        @NotBlank String content
) {}