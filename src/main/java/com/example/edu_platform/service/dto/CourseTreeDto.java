package com.example.edu_platform.service.dto;

import java.util.List;

public record CourseTreeDto(
        Long id, String title, String description,
        String categoryName, String teacherName,
        List<CourseModuleDto> modules
) {}
