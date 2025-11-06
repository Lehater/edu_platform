package com.example.edu_platform.web;

import com.example.edu_platform.service.AssignmentService;
import com.example.edu_platform.service.SubmissionService;
import com.example.edu_platform.service.dto.*;
import com.example.edu_platform.web.dto.CreateAssignmentRequest;
import com.example.edu_platform.web.dto.GradeSubmissionRequest;
import com.example.edu_platform.web.dto.SubmitAssignmentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;

    @PostMapping("/lessons/{lessonId}/assignments")
    public ResponseEntity<Long> create(@PathVariable Long lessonId, @RequestBody @Valid CreateAssignmentRequest req) {
        Long id = assignmentService.createAssignment(lessonId,
                new CreateAssignmentCmd(req.title(), req.description(), req.dueDate(), req.maxScore()));
        return ResponseEntity.created(URI.create("/api/assignments/" + id)).body(id);
    }

    @GetMapping("/lessons/{lessonId}/assignments")
    public ResponseEntity<List<AssignmentDto>> listByLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsForLesson(lessonId));
    }

    @PostMapping("/assignments/{assignmentId}/submit")
    public ResponseEntity<Long> submit(@PathVariable Long assignmentId, @RequestParam Long studentId,
                                       @RequestBody @Valid SubmitAssignmentRequest req) {
        Long id = submissionService.submit(studentId, assignmentId, new SubmitAssignmentCmd(req.content()));
        return ResponseEntity.created(URI.create("/api/submissions/" + id)).body(id);
    }

    @PostMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<SubmissionDto> grade(@PathVariable Long submissionId,
                                               @RequestBody @Valid GradeSubmissionRequest req) {
        return ResponseEntity.ok(submissionService.grade(submissionId, new GradeSubmissionCmd(req.score(), req.feedback())));
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<List<SubmissionDto>> listSubsByAssignment(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(submissionService.getSubmissionsForAssignment(assignmentId));
    }

    @GetMapping("/users/{studentId}/submissions")
    public ResponseEntity<List<SubmissionDto>> listSubsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(submissionService.getSubmissionsForStudent(studentId));
    }
}
