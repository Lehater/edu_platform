package com.example.edu_platform.repository;

import com.example.edu_platform.domain.entity.CourseModule;
import com.example.edu_platform.domain.entity.Quiz;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByModule(CourseModule module);

    /** Глубокая загрузка дерева: quiz -> questions -> options */
    @EntityGraph(attributePaths = {"questions", "questions.options"})
    Optional<Quiz> findWithQuestionsAndOptionsById(Long id);

    /** Альтернативно — JPQL join fetch */
    @Query("""
        select distinct qz from Quiz qz
          left join fetch qz.questions qu
          left join fetch qu.options ao
        where qz.id = :id
        """)
    Optional<Quiz> fetchTreeById(@Param("id") Long id);
}
