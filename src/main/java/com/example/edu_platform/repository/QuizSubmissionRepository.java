package com.example.edu_platform.repository;

import com.example.edu_platform.domain.entity.QuizSubmission;
import com.example.edu_platform.domain.entity.Quiz;
import com.example.edu_platform.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
    List<QuizSubmission> findAllByQuizOrderByTakenAtDescIdDesc(Quiz quiz);
    List<QuizSubmission> findAllByStudentOrderByTakenAtDescIdDesc(User student);
}
