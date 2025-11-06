package com.example.edu_platform.service.dto;

import com.example.edu_platform.domain.enums.AssignmentStatus;

import java.time.LocalDate;

public record AssignmentDto(
        Long id, Long lessonId, String title, String description,
        LocalDate dueDate, Integer maxScore, AssignmentStatus status
) {
}