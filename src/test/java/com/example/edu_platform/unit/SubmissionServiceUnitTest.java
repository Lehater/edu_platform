package com.example.edu_platform.unit;

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
import com.example.edu_platform.service.SubmissionService;
import com.example.edu_platform.service.dto.GradeSubmissionCmd;
import com.example.edu_platform.service.dto.SubmitAssignmentCmd;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceUnitTest {

    @Mock SubmissionRepository submissionRepository;
    @Mock AssignmentRepository assignmentRepository;
    @Mock UserRepository userRepository;

    @InjectMocks SubmissionService service;

    @Test
    void grade_fails_when_score_exceeds_max() {
        // given
        long submissionId = 100L;
        Assignment a = Assignment.builder().id(1L).maxScore(100).status(AssignmentStatus.OPEN).build();
        Submission s = Submission.builder().id(submissionId).assignment(a).build();

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(s));

        // when / then
        assertThatThrownBy(() -> service.grade(submissionId, new GradeSubmissionCmd(101, null)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("≤ maxScore");

        verify(submissionRepository, never()).save(any());
    }

    @Test
    void submit_fails_when_assignment_closed() {
        long studentId = 10L, assignmentId = 5L;
        User u = User.builder().id(studentId).role(Role.STUDENT).build();
        Assignment a = Assignment.builder().id(assignmentId).status(AssignmentStatus.CLOSED).maxScore(10).build();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(u));
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> service.submit(studentId, assignmentId, new SubmitAssignmentCmd("x")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("not open");

        verify(submissionRepository, never()).save(any());
    }

    @Test
    void submit_fails_when_past_due_date() {
        long studentId = 10L, assignmentId = 6L;
        User u = User.builder().id(studentId).role(Role.STUDENT).build();
        Assignment a = Assignment.builder()
                .id(assignmentId).status(AssignmentStatus.OPEN).maxScore(10)
                .dueDate(LocalDate.now().minusDays(1)).build();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(u));
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> service.submit(studentId, assignmentId, new SubmitAssignmentCmd("work")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("past due");
    }

    @Test
    void submit_fails_when_user_not_student() {
        long studentId = 9L, assignmentId = 1L;
        User nonStudent = User.builder().id(studentId).role(Role.TEACHER).build();
        when(userRepository.findById(studentId)).thenReturn(Optional.of(nonStudent));

        assertThatThrownBy(() -> service.submit(studentId, assignmentId, new SubmitAssignmentCmd("work")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Only STUDENT");

        verify(assignmentRepository, never()).findById(any());
    }

    @Test
    void grade_fails_when_submission_not_found() {
        when(submissionRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.grade(999L, new GradeSubmissionCmd(1, "ok")))
                .isInstanceOf(DomainNotFoundException.class);
    }
}
