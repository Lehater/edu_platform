package com.example.edu_platform.repository;

import com.example.edu_platform.domain.entity.Course;
import com.example.edu_platform.domain.entity.Enrollment;
import com.example.edu_platform.domain.entity.User;
import com.example.edu_platform.domain.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByUserAndCourse(User user, Course course);

    List<Enrollment> findAllByUser(User user);

    List<Enrollment> findAllByCourse(Course course);

    List<Enrollment> findAllByUserAndStatus(User user, EnrollmentStatus status);

    long countByCourse(Course course);
}
