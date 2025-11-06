package com.example.edu_platform.repository;

import com.example.edu_platform.domain.entity.AnswerOption;
import com.example.edu_platform.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
    List<AnswerOption> findAllByQuestionOrderByIdAsc(Question question);
}
