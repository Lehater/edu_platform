package com.example.edu_platform.repository;


import com.example.edu_platform.domain.entity.Course;
import com.example.edu_platform.domain.entity.CourseModule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<CourseModule, Long> {
    List<CourseModule> findAllByCourseOrderByOrderIndexAscIdAsc(Course course);

    // Для, когда сразу нужны уроки одного курса:
    @EntityGraph(attributePaths = {"lessons"})
    List<CourseModule> findAllByCourse(Course course);
}
