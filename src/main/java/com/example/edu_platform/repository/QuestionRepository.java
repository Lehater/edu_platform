package com.example.edu_platform.repository;

import com.example.edu_platform.domain.entity.Question;
import com.example.edu_platform.domain.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByQuizOrderByIdAsc(Quiz quiz);
}
