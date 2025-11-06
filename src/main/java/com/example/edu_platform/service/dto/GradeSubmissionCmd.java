package com.example.edu_platform.service.dto;

public record GradeSubmissionCmd(
        Integer score,
        String feedback
) {}