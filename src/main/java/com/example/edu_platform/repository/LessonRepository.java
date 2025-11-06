package com.example.edu_platform.repository;


import com.example.edu_platform.domain.entity.CourseModule;
import com.example.edu_platform.domain.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findAllByModuleOrderByIdAsc(CourseModule module);
}
