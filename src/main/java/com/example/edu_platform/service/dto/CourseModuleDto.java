package com.example.edu_platform.service.dto;

import java.util.List;

public record CourseModuleDto(
        Long id, String title, Integer orderIndex, String description,
        List<CourseLessonDto> lessons
) {}
