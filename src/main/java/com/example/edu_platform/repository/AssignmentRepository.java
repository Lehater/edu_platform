package com.example.edu_platform.repository;


import com.example.edu_platform.domain.entity.Assignment;
import com.example.edu_platform.domain.entity.Lesson;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findAllByLessonOrderByIdAsc(Lesson lesson);

    /**
     * Целевая подгрузка сабмитов для экрана проверки, чтобы избежать N+1
     */
    @EntityGraph(attributePaths = {"submissions", "submissions.student"})
    Optional<Assignment> findWithSubmissionsById(Long id);

    /**
     * Альтернативно — явный JPQL join fetch
     */
    @Query("""
            select a from Assignment a
              left join fetch a.submissions s
              left join fetch s.student
            where a.id = :id
            """)
    Optional<Assignment> fetchWithSubmissions(@Param("id") Long id);
}
