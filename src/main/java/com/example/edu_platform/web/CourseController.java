package com.example.edu_platform.web;

import com.example.edu_platform.service.CourseService;
import com.example.edu_platform.service.dto.CourseTreeDto;
import com.example.edu_platform.service.dto.CreateCourseCmd;
import com.example.edu_platform.web.dto.CreateCourseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CreateCourseRequest req) {
        Long id = courseService.createCourse(new CreateCourseCmd(
                req.title(), req.description(), req.categoryId(), req.teacherId(),
                req.duration(), req.startDate(), req.tagIds()
        ));
        return ResponseEntity.created(URI.create("/api/courses/" + id)).body(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseTreeDto> get(@PathVariable Long id) {
        // отдадим дерево курса (с модулями и уроками)
        return ResponseEntity.ok(courseService.getCourseTree(id));
    }
}
