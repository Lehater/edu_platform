package com.example.edu_platform.web;

import com.example.edu_platform.service.QuizService;
import com.example.edu_platform.service.dto.*;
import com.example.edu_platform.service.dto.quiz_dtos.*;
import com.example.edu_platform.web.dto.*;
import com.example.edu_platform.web.dto.quiz_web_dtos.AddAnswerOptionRequest;
import com.example.edu_platform.web.dto.quiz_web_dtos.AddQuestionRequest;
import com.example.edu_platform.web.dto.quiz_web_dtos.CreateQuizRequest;
import com.example.edu_platform.web.dto.quiz_web_dtos.TakeQuizRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/modules/{moduleId}/quiz")
    public ResponseEntity<Long> create(@PathVariable Long moduleId, @RequestBody @Valid CreateQuizRequest req) {
        Long id = quizService.createQuiz(moduleId, new CreateQuizCmd(req.title(), req.timeLimitMinutes()));
        return ResponseEntity.created(URI.create("/api/quizzes/" + id)).body(id);
    }

    @PostMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<Long> addQuestion(@PathVariable Long quizId, @RequestBody @Valid AddQuestionRequest req) {
        Long id = quizService.addQuestion(quizId, new AddQuestionCmd(req.type(), req.text()));
        return ResponseEntity.created(URI.create("/api/questions/" + id)).body(id);
    }

    @PostMapping("/questions/{questionId}/options")
    public ResponseEntity<Long> addOption(@PathVariable Long questionId, @RequestBody @Valid AddAnswerOptionRequest req) {
        Long id = quizService.addAnswerOption(questionId, new AddAnswerOptionCmd(req.text(), req.isCorrect()));
        return ResponseEntity.created(URI.create("/api/answer-options/" + id)).body(id);
    }

    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<List<QuestionDto>> getTree(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizTree(quizId));
    }

    @PostMapping("/quizzes/{quizId}/take")
    public ResponseEntity<QuizSubmissionDto> take(@PathVariable Long quizId,
                                                  @RequestParam Long studentId,
                                                  @RequestBody @Valid TakeQuizRequest req) {
        return ResponseEntity.ok(quizService.takeQuiz(studentId, quizId, new TakeQuizCmd(req.answers())));
    }

    @GetMapping("/quizzes/{quizId}/results")
    public ResponseEntity<List<QuizSubmissionDto>> resultsForQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getResultsForQuiz(quizId));
    }

    @GetMapping("/users/{studentId}/quiz-results")
    public ResponseEntity<List<QuizSubmissionDto>> resultsForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(quizService.getResultsForStudent(studentId));
    }
}
