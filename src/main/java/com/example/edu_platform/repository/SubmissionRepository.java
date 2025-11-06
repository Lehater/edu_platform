package com.example.edu_platform.repository;


import com.example.edu_platform.domain.entity.Assignment;
import com.example.edu_platform.domain.entity.Submission;
import com.example.edu_platform.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByStudentAndAssignment(User student, Assignment assignment);

    List<Submission> findAllByAssignmentOrderBySubmittedAtAscIdAsc(Assignment assignment);

    List<Submission> findAllByStudentOrderBySubmittedAtDescIdDesc(User student);
}
