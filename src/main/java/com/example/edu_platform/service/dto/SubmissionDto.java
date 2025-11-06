package com.example.edu_platform.service.dto;

import java.time.LocalDateTime;

public record SubmissionDto(
        Long id, Long assignmentId, Long studentId,
        LocalDateTime submittedAt, String content, Integer score, String feedback
) {
}