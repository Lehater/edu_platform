package com.example.edu_platform.service.dto.quiz_dtos;


import com.example.edu_platform.domain.enums.QuestionType;

import java.util.List;

public record QuestionDto(Long id, String text, QuestionType type,
                          List<AnswerOptionDto> options) {}