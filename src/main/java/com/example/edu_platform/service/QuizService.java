package com.example.edu_platform.service;

import com.example.edu_platform.domain.entity.*;
import com.example.edu_platform.domain.enums.QuestionType;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.exception.BusinessRuleViolationException;
import com.example.edu_platform.exception.DomainNotFoundException;
import com.example.edu_platform.repository.*;
import com.example.edu_platform.service.dto.quiz_dtos.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final ModuleRepository moduleRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final UserRepository userRepository;

    /** Создать квиз для модуля (один модуль — один квиз). */
    @Transactional
    public Long createQuiz(Long moduleId, CreateQuizCmd cmd) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new DomainNotFoundException("Module not found: " + moduleId));
        if (module.getQuiz() != null) {
            throw new BusinessRuleViolationException("Quiz already exists for module " + moduleId);
        }
        Quiz quiz = Quiz.builder()
                .module(module)
                .title(cmd.title())
                .timeLimit(cmd.timeLimitMinutes())
                .build();
        module.setQuiz(quiz);
        return quizRepository.save(quiz).getId();
    }

    /** Добавить вопрос к квизу. */
    @Transactional
    public Long addQuestion(Long quizId, AddQuestionCmd cmd) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new DomainNotFoundException("Quiz not found: " + quizId));
        Question q = Question.builder()
                .quiz(quiz)
                .text(cmd.text())
                .type(cmd.type())
                .build();
        Question saved = questionRepository.save(q);
        quiz.getQuestions().add(saved);
        return saved.getId();
    }

    /** Добавить вариант ответа к вопросу. */
    @Transactional
    public Long addAnswerOption(Long questionId, AddAnswerOptionCmd cmd) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new DomainNotFoundException("Question not found: " + questionId));
        AnswerOption ao = AnswerOption.builder()
                .question(q)
                .text(cmd.text())
                .isCorrect(cmd.isCorrect())
                .build();
        AnswerOption saved = answerOptionRepository.save(ao);
        q.getOptions().add(saved);
        return saved.getId();
    }

    /**
     * Пройти квиз: answers = Map<questionId, List<optionId>>.
     * Подсчёт: процент правильных ответов (округление вниз).
     */
    @Transactional
    public QuizSubmissionDto takeQuiz(Long studentId, Long quizId, TakeQuizCmd cmd) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new DomainNotFoundException("User not found: " + studentId));
        if (student.getRole() != Role.STUDENT) {
            throw new BusinessRuleViolationException("Only STUDENT can take quizzes. userId=" + studentId);
        }

        Quiz quiz = quizRepository.fetchTreeById(quizId)
                .orElseThrow(() -> new DomainNotFoundException("Quiz not found: " + quizId));

        if (quiz.getQuestions().isEmpty()) {
            throw new BusinessRuleViolationException("Quiz has no questions.");
        }

        Map<Long, List<Long>> answers = new HashMap<>(cmd.answers());
        int total = quiz.getQuestions().size();
        int correctCount = 0;

        for (Question q : quiz.getQuestions()) {
            List<Long> selected = answers.getOrDefault(q.getId(), Collections.emptyList());
            // нормализуем к множеству
            Set<Long> selectedSet = new HashSet<>(selected);

            // набор правильных опций
            Set<Long> correctSet = q.getOptions().stream()
                    .filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId)
                    .collect(java.util.stream.Collectors.toSet());

            // Валидация для SINGLE: разрешим 0..1 выбора
            if (q.getType() == QuestionType.SINGLE_CHOICE && selectedSet.size() > 1) {
                throw new BusinessRuleViolationException("Multiple answers provided for SINGLE_CHOICE question " + q.getId());
            }

            // Ответ считается верным, если множества равны
            if (selectedSet.equals(correctSet)) {
                correctCount++;
            }
        }

        int scorePercent = (int) Math.floor((correctCount * 100.0) / total);

        QuizSubmission submission = QuizSubmission.builder()
                .quiz(quiz)
                .student(student)
                .score(scorePercent)
                .takenAt(LocalDateTime.now())
                .build();

        QuizSubmission saved = quizSubmissionRepository.save(submission);
        return new QuizSubmissionDto(saved.getId(), quiz.getId(), student.getId(), saved.getScore());
    }

    /** Получить дерево квиза без N+1 (для выдачи теста студенту). */
    @Transactional
    public List<QuestionDto> getQuizTree(Long quizId) {
        Quiz quiz = quizRepository.fetchTreeById(quizId)
                .orElseThrow(() -> new DomainNotFoundException("Quiz not found: " + quizId));

        return quiz.getQuestions().stream()
                .map(q -> new QuestionDto(
                        q.getId(), q.getText(), q.getType(),
                        q.getOptions().stream()
                                .map(o -> new AnswerOptionDto(o.getId(), o.getText(), o.isCorrect()))
                                .toList()
                ))
                .toList();
    }

    /** Результаты данного квиза (для преподавателя). */
    @Transactional
    public List<QuizSubmissionDto> getResultsForQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new DomainNotFoundException("Quiz not found: " + quizId));
        return quizSubmissionRepository.findAllByQuizOrderByTakenAtDescIdDesc(quiz).stream()
                .map(s -> new QuizSubmissionDto(s.getId(), quiz.getId(), s.getStudent().getId(), s.getScore()))
                .toList();
    }

    /** Все результаты студента (для личного кабинета). */
    @Transactional
    public List<QuizSubmissionDto> getResultsForStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new DomainNotFoundException("User not found: " + studentId));
        return quizSubmissionRepository.findAllByStudentOrderByTakenAtDescIdDesc(student).stream()
                .map(s -> new QuizSubmissionDto(s.getId(), s.getQuiz().getId(), student.getId(), s.getScore()))
                .toList();
    }
}
