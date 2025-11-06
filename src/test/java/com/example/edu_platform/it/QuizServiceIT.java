package com.example.edu_platform.it;

import com.example.edu_platform.domain.entity.CourseModule;
import com.example.edu_platform.domain.entity.User;
import com.example.edu_platform.domain.entity.Course;
import com.example.edu_platform.domain.enums.QuestionType;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.repository.CourseRepository;
import com.example.edu_platform.repository.ModuleRepository;
import com.example.edu_platform.repository.QuizRepository;
import com.example.edu_platform.repository.UserRepository;
import com.example.edu_platform.service.QuizService;
import com.example.edu_platform.service.dto.quiz_dtos.*;
import com.example.edu_platform.test.AbstractIntegrationTest;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class QuizServiceIT extends AbstractIntegrationTest {

    @Autowired
    UserRepository userRepo;
    @Autowired
    CourseRepository courseRepo;
    @Autowired
    ModuleRepository moduleRepo;
    @Autowired
    QuizRepository quizRepo;
    @Autowired
    QuizService quizService;

    private Long newModule() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User t = userRepo.save(
                User.builder()
                        .name("TQ")
                        .email("t-quiz" + suffix + "@example.com")
                        .role(Role.TEACHER).build());
        Course c = courseRepo.save(
                Course.builder().title("Course Q").teacher(t).build());
        CourseModule m = moduleRepo.save(
                new CourseModule(null, c, "MQ", 1, "q desc", null, null));
        return m.getId();
    }

    @Test
    void create_tree_take_and_list_results_with_lazy_demo() {
        Long moduleId = newModule();
        Long quizId = quizService.createQuiz(moduleId, new CreateQuizCmd("Quiz 1", 30));

        Long q1 = quizService.addQuestion(quizId, new AddQuestionCmd(QuestionType.SINGLE_CHOICE, "2+2=?"));
        Long q2 = quizService.addQuestion(quizId, new AddQuestionCmd(QuestionType.MULTIPLE_CHOICE, "Select primes"));

        Long a11 = quizService.addAnswerOption(q1, new AddAnswerOptionCmd("4", true));
        quizService.addAnswerOption(q1, new AddAnswerOptionCmd("3", false));

        Long a21 = quizService.addAnswerOption(q2, new AddAnswerOptionCmd("2", true));
        Long a22 = quizService.addAnswerOption(q2, new AddAnswerOptionCmd("3", true));
        quizService.addAnswerOption(q2, new AddAnswerOptionCmd("4", false));


        List<QuestionDto> tree = quizService.getQuizTree(quizId);
        assertThat(tree).hasSize(2);

        User s = userRepo.save(
                User.builder().name("S-quiz").email("s-quiz@example.com").role(Role.STUDENT).build());

        QuizSubmissionDto res = quizService.takeQuiz(
                s.getId(), quizId,
                new TakeQuizCmd(Map.of(
                        q1, List.of(a11),
                        q2, List.of(a21, a22)
                )));
        assertThat(res.scorePercent()).isEqualTo(100);

        assertThat(quizService.getResultsForQuiz(quizId)).isNotEmpty();
        assertThat(quizService.getResultsForStudent(s.getId())).isNotEmpty();

        // LAZY demo: loading quiz without fetch and touching questions outside tx should fail
        var quiz = quizRepo.findById(quizId).orElseThrow();
        assertThatThrownBy(() -> quiz.getQuestions().size())
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void single_choice_with_multiple_answers_is_error() {
        Long moduleId = newModule();
        Long quizId = quizService.createQuiz(moduleId, new CreateQuizCmd("Quiz 2", null));
        Long q1 = quizService.addQuestion(quizId, new AddQuestionCmd(QuestionType.SINGLE_CHOICE, "1+1=?"));
        Long a1 = quizService.addAnswerOption(q1, new AddAnswerOptionCmd("2", true));
        Long a2 = quizService.addAnswerOption(q1, new AddAnswerOptionCmd("3", false));

        User s = userRepo.save(
                User.builder().name("S2").email("s2-quiz@example.com").role(Role.STUDENT).build());

        assertThatThrownBy(() -> quizService.takeQuiz(
                s.getId(), quizId, new TakeQuizCmd(Map.of(q1, List.of(a1, a2)))
        )).isInstanceOf(RuntimeException.class); // BusinessRuleViolationException
    }
}
