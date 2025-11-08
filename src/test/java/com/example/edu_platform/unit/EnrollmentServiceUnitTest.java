package com.example.edu_platform.unit;

import com.example.edu_platform.domain.entity.Course;
import com.example.edu_platform.domain.entity.Enrollment;
import com.example.edu_platform.domain.entity.User;
import com.example.edu_platform.domain.enums.EnrollmentStatus;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.exception.BusinessRuleViolationException;
import com.example.edu_platform.exception.DomainNotFoundException;
import com.example.edu_platform.repository.CourseRepository;
import com.example.edu_platform.repository.EnrollmentRepository;
import com.example.edu_platform.repository.UserRepository;
import com.example.edu_platform.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceUnitTest {

    @Mock EnrollmentRepository enrollmentRepository;
    @Mock UserRepository userRepository;
    @Mock CourseRepository courseRepository;

    @InjectMocks EnrollmentService service;

    @Test
    void enroll_fails_when_user_not_student() {
        // given
        long userId = 10L, courseId = 5L;
        User teacher = User.builder().id(userId).name("T").email("t@ex.com").role(Role.TEACHER).build();
        Course course = Course.builder().id(courseId).title("C").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(teacher));
//        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // when / then
        assertThatThrownBy(() -> service.enrollStudent(courseId, userId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("STUDENT");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enroll_fails_when_duplicate_enrollment() {
        long userId = 11L, courseId = 7L;
        User student = User.builder().id(userId).name("S").email("s@ex.com").role(Role.STUDENT).build();
        Course course = Course.builder().id(courseId).title("C").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByUserAndCourse(student, course))
                .thenReturn(Optional.of(Enrollment.builder()
                        .id(1L).user(student).course(course)
                        .status(EnrollmentStatus.ACTIVE).enrollDate(LocalDate.now()).build()));

        assertThatThrownBy(() -> service.enrollStudent(courseId, userId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("already enrolled");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void completeCourse_fails_when_enrollment_not_found() {
        long userId = 1L, courseId = 2L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).role(Role.STUDENT).build()));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(Course.builder().id(courseId).build()));
        when(enrollmentRepository.findByUserAndCourse(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.completeCourse(courseId, userId))
                .isInstanceOf(DomainNotFoundException.class);
    }
}
