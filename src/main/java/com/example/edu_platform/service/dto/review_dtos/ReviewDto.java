package com.example.edu_platform.service.dto.review_dtos;

import java.time.LocalDateTime;

public record ReviewDto(
        Long id, Long courseId, Long studentId,
        Integer rating, String comment, LocalDateTime createdAt
) {}