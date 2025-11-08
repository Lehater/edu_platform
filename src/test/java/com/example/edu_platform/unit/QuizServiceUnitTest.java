package com.example.edu_platform.unit;

import com.example.edu_platform.domain.entity.AnswerOption;
import com.example.edu_platform.domain.entity.Question;
import com.example.edu_platform.domain.entity.Quiz;
import com.example.edu_platform.domain.entity.User;
import com.example.edu_platform.domain.enums.QuestionType;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.exception.BusinessRuleViolationException;
import com.example.edu_platform.exception.DomainNotFoundException;
import com.example.edu_platform.repository.*;
import com.example.edu_platform.service.QuizService;
import com.example.edu_platform.service.dto.quiz_dtos.QuizSubmissionDto;
import com.example.edu_platform.service.dto.quiz_dtos.TakeQuizCmd;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceUnitTest {

    @Mock QuizRepository quizRepository;
    @Mock ModuleRepository moduleRepository;
    @Mock QuestionRepository questionRepository;
    @Mock AnswerOptionRepository answerOptionRepository;
    @Mock QuizSubmissionRepository quizSubmissionRepository;
    @Mock UserRepository userRepository;

    @InjectMocks QuizService service;

    @Test
    void takeQuiz_fails_when_single_choice_has_multiple_answers() {
        long quizId = 1L, studentId = 2L;

        User student = User.builder().id(studentId).role(Role.STUDENT).build();

        // Quiz with ONE single-choice question
        Quiz quiz = Quiz.builder().id(quizId).questions(new LinkedHashSet<>()).build();
        Question q = Question.builder().id(10L).quiz(quiz).type(QuestionType.SINGLE_CHOICE)
                .options(new LinkedHashSet<>()).text("Q1").build();
        quiz.getQuestions().add(q);

        // options (one correct)
        AnswerOption o1 = AnswerOption.builder().id(100L).question(q).text("A").isCorrect(true).build();
        AnswerOption o2 = AnswerOption.builder().id(101L).question(q).text("B").isCorrect(false).build();
        q.getOptions().add(o1);
        q.getOptions().add(o2);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(quizRepository.fetchTreeById(quizId)).thenReturn(Optional.of(quiz));

        Map<Long, List<Long>> answers = Map.of(q.getId(), List.of(o1.getId(), o2.getId())); // 2 варианта

        assertThatThrownBy(() -> service.takeQuiz(studentId, quizId, new TakeQuizCmd(answers)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Multiple answers");

        verify(quizSubmissionRepository, never()).save(any());
    }

    @Test
    void takeQuiz_fails_when_quiz_has_no_questions() {
        long quizId = 3L, studentId = 4L;

        User student = User.builder().id(studentId).role(Role.STUDENT).build();
        Quiz empty = Quiz.builder().id(quizId).questions(new LinkedHashSet<>()).build();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(quizRepository.fetchTreeById(quizId)).thenReturn(Optional.of(empty));

        assertThatThrownBy(() -> service.takeQuiz(studentId, quizId, new TakeQuizCmd(Map.of())))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("no questions");

        verify(quizSubmissionRepository, never()).save(any());
    }

    @Test
    void takeQuiz_fails_when_user_not_found() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.takeQuiz(999L, 1L, new TakeQuizCmd(Map.of())))
                .isInstanceOf(DomainNotFoundException.class);
    }

    @Test
    void takeQuiz_success_100_percent() {
        long quizId = 5L, studentId = 6L;

        User student = User.builder().id(studentId).role(Role.STUDENT).build();
        Quiz quiz = Quiz.builder().id(quizId).questions(new LinkedHashSet<>()).build();

        Question q1 = Question.builder().id(11L).quiz(quiz).type(QuestionType.SINGLE_CHOICE)
                .options(new LinkedHashSet<>()).text("2+2").build();
        AnswerOption q1o = AnswerOption.builder().id(111L).question(q1).text("4").isCorrect(true).build();
        q1.getOptions().add(q1o);
        quiz.getQuestions().add(q1);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(quizRepository.fetchTreeById(quizId)).thenReturn(Optional.of(quiz));
        when(quizSubmissionRepository.save(any())).thenAnswer(inv -> {
            var s = (com.example.edu_platform.domain.entity.QuizSubmission) inv.getArgument(0);
            s.setId(777L);
            return s;
        });

        QuizSubmissionDto dto = service.takeQuiz(studentId, quizId, new TakeQuizCmd(Map.of(q1.getId(), List.of(q1o.getId()))));
        assertThat(dto.scorePercent()).isEqualTo(100);
        assertThat(dto.quizId()).isEqualTo(quizId);
        assertThat(dto.studentId()).isEqualTo(studentId);
    }
}
