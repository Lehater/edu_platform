package com.example.edu_platform.service.dto;

import com.example.edu_platform.domain.enums.EnrollmentStatus;

import java.time.LocalDate;

public record EnrollmentDto(Long id, Long courseId, Long studentId,
                            EnrollmentStatus status, LocalDate enrollDate) {
}