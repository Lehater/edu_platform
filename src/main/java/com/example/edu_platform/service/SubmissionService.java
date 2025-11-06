package com.example.edu_platform.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.edu_platform.domain.entity.Assignment;
import com.example.edu_platform.domain.entity.Submission;
import com.example.edu_platform.domain.entity.User;
import com.example.edu_platform.domain.enums.AssignmentStatus;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.exception.BusinessRuleViolationException;
import com.example.edu_platform.exception.DomainNotFoundException;
import com.example.edu_platform.repository.AssignmentRepository;
import com.example.edu_platform.repository.SubmissionRepository;
import com.example.edu_platform.repository.UserRepository;
import com.example.edu_platform.service.dto.GradeSubmissionCmd;
import com.example.edu_platform.service.dto.SubmissionDto;
import com.example.edu_platform.service.dto.SubmitAssignmentCmd;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    /**
     * Создать отправку решения студентом.
     * Правила:
     * - user существует и имеет роль STUDENT;
     * - assignment существует и открыт (OPEN);
     * - не просрочен дедлайн (today <= dueDate, если dueDate задан);
     * - нет уже существующей отправки (уникальность student+assignment).
     */
    @Transactional
    public Long submit(Long studentId, Long assignmentId, SubmitAssignmentCmd cmd) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new DomainNotFoundException("User not found: " + studentId));
        if (student.getRole() != Role.STUDENT) {
            throw new BusinessRuleViolationException("Only STUDENT can submit assignments. userId=" + studentId);
        }

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new DomainNotFoundException("Assignment not found: " + assignmentId));

        if (assignment.getStatus() != AssignmentStatus.OPEN) {
            throw new BusinessRuleViolationException("Assignment is not open for submissions.");
        }

        LocalDate dueDate = assignment.getDueDate();
        if (dueDate != null && LocalDate.now().isAfter(dueDate)) {
            throw new BusinessRuleViolationException("Submission is past due date.");
        }

        submissionRepository.findByStudentAndAssignment(student, assignment).ifPresent(s -> {
            throw new BusinessRuleViolationException("Submission already exists for this student and assignment.");
        });

        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .submittedAt(LocalDateTime.now())
                .content(cmd.content())
                .score(null)
                .feedback(null)
                .build();

        return submissionRepository.save(submission).getId();
    }

    /**
     * Преподаватель оценивает отправленную работу.
     * Правила:
     * - submission существует;
     * - score не должен превышать assignment.maxScore;
     * - допускаем переоценивание (перезапись score/feedback).
     */
    @Transactional
    public SubmissionDto grade(Long submissionId, GradeSubmissionCmd cmd) {
        Submission s = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new DomainNotFoundException("Submission not found: " + submissionId));

        Integer score = cmd.score();
        if (score != null) {
            Integer max = s.getAssignment().getMaxScore();
            if (max != null && score > max) {
                throw new BusinessRuleViolationException("Score must be ≤ maxScore (" + max + ")");
            }
            if (score < 0) {
                throw new BusinessRuleViolationException("Score must be ≥ 0");
            }
        }

        s.setScore(score);
        s.setFeedback(cmd.feedback());
        return toDto(s);
    }

    /**
     * Все решения по заданию (для преподавателя). LAZY без N+1 — см. repo метод with submissions.
     */
    @Transactional
    public List<SubmissionDto> getSubmissionsForAssignment(Long assignmentId) {
        Assignment a = assignmentRepository.fetchWithSubmissions(assignmentId)
                .orElseThrow(() -> new DomainNotFoundException("Assignment not found: " + assignmentId));

        return a.getSubmissions().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Все решения студента (для личного кабинета студента).
     */
    @Transactional
    public List<SubmissionDto> getSubmissionsForStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new DomainNotFoundException("User not found: " + studentId));

        return submissionRepository.findAllByStudentOrderBySubmittedAtDescIdDesc(student).stream()
                .map(this::toDto)
                .toList();
    }

    private SubmissionDto toDto(Submission s) {
        return new SubmissionDto(
                s.getId(),
                s.getAssignment().getId(),
                s.getStudent().getId(),
                s.getSubmittedAt(),
                s.getContent(),
                s.getScore(),
                s.getFeedback()
        );
    }
}
