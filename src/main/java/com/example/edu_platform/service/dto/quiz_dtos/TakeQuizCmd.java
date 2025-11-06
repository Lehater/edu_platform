package com.example.edu_platform.service.dto.quiz_dtos;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record TakeQuizCmd(
        /**
         * Map<questionId, List<optionId>> — для SINGLE допускается размер 0..1,
         * для MULTIPLE — 0..N
         */
        @NotNull Map<Long, List<Long>> answers
) {
}