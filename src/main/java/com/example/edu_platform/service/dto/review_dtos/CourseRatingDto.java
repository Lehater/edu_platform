package com.example.edu_platform.service.dto.review_dtos;

public record CourseRatingDto(
        Long courseId, double average, long total
) {
}