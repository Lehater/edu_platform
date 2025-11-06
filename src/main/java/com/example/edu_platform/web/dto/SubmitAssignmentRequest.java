package com.example.edu_platform.web.dto;


import jakarta.validation.constraints.NotBlank;

public record SubmitAssignmentRequest(@NotBlank String content) {
}