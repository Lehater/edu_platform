package com.example.edu_platform.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.edu_platform.domain.entity.Assignment;
import com.example.edu_platform.domain.entity.Lesson;
import com.example.edu_platform.domain.enums.AssignmentStatus;
import com.example.edu_platform.exception.DomainNotFoundException;
import com.example.edu_platform.repository.AssignmentRepository;
import com.example.edu_platform.repository.LessonRepository;
import com.example.edu_platform.service.dto.AssignmentDto;
import com.example.edu_platform.service.dto.CreateAssignmentCmd;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public Long createAssignment(Long lessonId, CreateAssignmentCmd cmd) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new DomainNotFoundException("Lesson not found: " + lessonId));

        Assignment assignment = Assignment.builder()
                .lesson(lesson)
                .title(cmd.title())
                .description(cmd.description())
                .dueDate(cmd.dueDate())
                .maxScore(cmd.maxScore())
                .status(AssignmentStatus.OPEN) // по умолчанию открыто
                .build();

        return assignmentRepository.save(assignment).getId();
    }

    @Transactional
    public void openAssignment(Long assignmentId) {
        Assignment a = findByIdOrThrow(assignmentId);
        a.setStatus(AssignmentStatus.OPEN);
    }

    @Transactional
    public void closeAssignment(Long assignmentId) {
        Assignment a = findByIdOrThrow(assignmentId);
        a.setStatus(AssignmentStatus.CLOSED);
    }

    @Transactional
    public List<AssignmentDto> getAssignmentsForLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new DomainNotFoundException("Lesson not found: " + lessonId));
        return assignmentRepository.findAllByLessonOrderByIdAsc(lesson).stream()
                .map(a -> new AssignmentDto(
                        a.getId(), lesson.getId(), a.getTitle(), a.getDescription(),
                        a.getDueDate(), a.getMaxScore(), a.getStatus()
                ))
                .toList();
    }

    @Transactional
    public AssignmentDto getById(Long assignmentId) {
        Assignment a = findByIdOrThrow(assignmentId);
        return new AssignmentDto(
                a.getId(), a.getLesson().getId(), a.getTitle(), a.getDescription(),
                a.getDueDate(), a.getMaxScore(), a.getStatus()
        );
    }

    private Assignment findByIdOrThrow(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new DomainNotFoundException("Assignment not found: " + id));
    }
}
