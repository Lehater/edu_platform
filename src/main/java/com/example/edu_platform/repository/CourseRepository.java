package com.example.edu_platform.repository;

import com.example.edu_platform.domain.entity.Category;
import com.example.edu_platform.domain.entity.Course;
import com.example.edu_platform.domain.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByCategory(Category category);

    List<Course> findByTeacher(User teacher);

    // целевая подгрузка тегов вместе с курсом
    @EntityGraph(attributePaths = {"tags"})
    List<Course> findAllByTitleContainingIgnoreCase(String titlePart);

    // EntityGraph для 2-х уровней: modules + lessons
    @EntityGraph(attributePaths = {"courseModules", "courseModules.lessons"})
    Optional<Course> findWithModulesAndLessonsById(Long id);

}
