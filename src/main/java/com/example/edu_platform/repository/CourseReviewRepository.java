package com.example.edu_platform.repository;

import com.example.edu_platform.domain.entity.CourseReview;
import com.example.edu_platform.domain.entity.Course;
import com.example.edu_platform.domain.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    Optional<CourseReview> findByCourseAndStudent(Course course, User student);

    List<CourseReview> findAllByCourseOrderByCreatedAtDesc(Course course);

    @Query("select avg(cr.rating) from CourseReview cr where cr.course = :course")
    Double findAverageRating(@Param("course") Course course);

    @Query("select count(cr) from CourseReview cr where cr.course = :course")
    long countByCourse(@Param("course") Course course);
}
