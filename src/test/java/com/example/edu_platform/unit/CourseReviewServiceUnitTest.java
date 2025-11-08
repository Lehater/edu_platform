package com.example.edu_platform.unit;

import com.example.edu_platform.domain.entity.Course;
import com.example.edu_platform.domain.entity.Enrollment;
import com.example.edu_platform.domain.entity.User;
import com.example.edu_platform.domain.enums.EnrollmentStatus;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.exception.BusinessRuleViolationException;
import com.example.edu_platform.exception.DomainNotFoundException;
import com.example.edu_platform.repository.CourseRepository;
import com.example.edu_platform.repository.CourseReviewRepository;
import com.example.edu_platform.repository.EnrollmentRepository;
import com.example.edu_platform.repository.UserRepository;
import com.example.edu_platform.service.CourseReviewService;
import com.example.edu_platform.service.dto.review_dtos.UpsertReviewCmd;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseReviewServiceUnitTest {

    @Mock CourseReviewRepository reviewRepo;
    @Mock CourseRepository courseRepo;
    @Mock UserRepository userRepo;
    @Mock EnrollmentRepository enrollmentRepo;

    @InjectMocks CourseReviewService service;

    @Test
    void upsert_fails_when_user_not_student() {
        long courseId = 1L, userId = 2L;
        Course c = Course.builder().id(courseId).build();
        User u = User.builder().id(userId).role(Role.TEACHER).build();

        when(courseRepo.findById(courseId)).thenReturn(Optional.of(c));
        when(userRepo.findById(userId)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> service.upsertReview(courseId, userId, new UpsertReviewCmd(5, "ok")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Only STUDENT");
    }

    @Test
    void upsert_fails_when_not_enrolled() {
        long courseId = 3L, userId = 4L;
        Course c = Course.builder().id(courseId).build();
        User s = User.builder().id(userId).role(Role.STUDENT).build();

        when(courseRepo.findById(courseId)).thenReturn(Optional.of(c));
        when(userRepo.findById(userId)).thenReturn(Optional.of(s));
        when(enrollmentRepo.findByUserAndCourse(s, c)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsertReview(courseId, userId, new UpsertReviewCmd(4, null)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("not enrolled");
    }

    @Test
    void upsert_fails_when_dropped() {
        long courseId = 5L, userId = 6L;
        Course c = Course.builder().id(courseId).build();
        User s = User.builder().id(userId).role(Role.STUDENT).build();
        Enrollment e = Enrollment.builder().id(1L).course(c).user(s).enrollDate(LocalDate.now()).status(EnrollmentStatus.DROPPED).build();

        when(courseRepo.findById(courseId)).thenReturn(Optional.of(c));
        when(userRepo.findById(userId)).thenReturn(Optional.of(s));
        when(enrollmentRepo.findByUserAndCourse(s, c)).thenReturn(Optional.of(e));

        assertThatThrownBy(() -> service.upsertReview(courseId, userId, new UpsertReviewCmd(4, "text")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Dropped");
    }

    @Test
    void upsert_fails_when_course_not_found() {
        when(courseRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.upsertReview(99L, 1L, new UpsertReviewCmd(5, null)))
                .isInstanceOf(DomainNotFoundException.class);
    }
}

