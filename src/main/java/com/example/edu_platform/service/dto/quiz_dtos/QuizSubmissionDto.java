package com.example.edu_platform.service.dto.quiz_dtos;

public record QuizSubmissionDto(Long id, Long quizId, Long studentId, Integer scorePercent) {
}