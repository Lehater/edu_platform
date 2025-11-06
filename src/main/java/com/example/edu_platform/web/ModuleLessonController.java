package com.example.edu_platform.web;


import com.example.edu_platform.service.CourseService;
import com.example.edu_platform.service.dto.AddLessonCmd;
import com.example.edu_platform.service.dto.AddModuleCmd;
import com.example.edu_platform.web.dto.AddLessonRequest;
import com.example.edu_platform.web.dto.AddModuleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ModuleLessonController {

    private final CourseService courseService;

    @PostMapping("/courses/{courseId}/modules")
    public ResponseEntity<?> addModule(@PathVariable Long courseId, @RequestBody @Valid AddModuleRequest req) {
        Long id = courseService.addModule(courseId, new AddModuleCmd(req.title(), req.orderIndex(), req.description()));
        return ResponseEntity.created(URI.create("/api/modules/" + id)).body(id);
    }

    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<?> addLesson(@PathVariable Long moduleId, @RequestBody @Valid AddLessonRequest req) {
        Long id = courseService.addLesson(moduleId, new AddLessonCmd(req.title(), req.content(), req.videoUrl()));
        return ResponseEntity.created(URI.create("/api/lessons/" + id)).body(id);
    }
}
