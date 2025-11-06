package com.example.edu_platform.web;

import com.example.edu_platform.service.EnrollmentService;
import com.example.edu_platform.service.dto.CourseBriefDto;
import com.example.edu_platform.service.dto.EnrollmentDto;
import com.example.edu_platform.service.dto.UserBriefDto;
import com.example.edu_platform.web.dto.EnrollRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService service;

    @PostMapping("/courses/{courseId}")
    public ResponseEntity<EnrollmentDto> enroll(@PathVariable Long courseId, @RequestBody @Valid EnrollRequest req) {
        return ResponseEntity.ok(service.enrollStudent(courseId, req.userId()));
    }

    @PostMapping("/courses/{courseId}/complete")
    public ResponseEntity<EnrollmentDto> complete(@PathVariable Long courseId, @RequestBody @Valid EnrollRequest req) {
        return ResponseEntity.ok(service.completeCourse(courseId, req.userId()));
    }

    @PostMapping("/courses/{courseId}/unenroll")
    public ResponseEntity<EnrollmentDto> unenroll(@PathVariable Long courseId, @RequestBody @Valid EnrollRequest req) {
        return ResponseEntity.ok(service.unenrollStudent(courseId, req.userId()));
    }

    @GetMapping("/courses/{courseId}/students")
    public ResponseEntity<List<UserBriefDto>> students(@PathVariable Long courseId) {
        return ResponseEntity.ok(service.getStudentsForCourse(courseId));
    }

    @GetMapping("/users/{userId}/courses")
    public ResponseEntity<List<CourseBriefDto>> courses(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getCoursesForStudent(userId));
    }
}
