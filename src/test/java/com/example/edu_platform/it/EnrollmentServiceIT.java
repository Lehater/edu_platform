package com.example.edu_platform.it;

import com.example.edu_platform.domain.entity.Course;
import com.example.edu_platform.domain.entity.User;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.exception.BusinessRuleViolationException;
import com.example.edu_platform.repository.CourseRepository;
import com.example.edu_platform.repository.UserRepository;
import com.example.edu_platform.service.EnrollmentService;
import com.example.edu_platform.service.dto.CourseBriefDto;
import com.example.edu_platform.service.dto.EnrollmentDto;
import com.example.edu_platform.service.dto.UserBriefDto;
import com.example.edu_platform.test.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class EnrollmentServiceIT extends AbstractIntegrationTest {

    @Autowired EnrollmentService enrollmentService;
    @Autowired UserRepository userRepo;
    @Autowired CourseRepository courseRepo;

    @Test
    void enroll_unenroll_complete_and_lists() {
        User teacher = userRepo.save(User.builder().name("Teach").email("t@ex.com").role(Role.TEACHER).build());
        User student = userRepo.save(User.builder().name("Stud").email("s@ex.com").role(Role.STUDENT).build());
        Course course = courseRepo.save(Course.builder().title("Course A").teacher(teacher).build());

        EnrollmentDto e = enrollmentService.enrollStudent(course.getId(), student.getId());
        assertThat(e.status().name()).isEqualTo("ACTIVE");

        // повторная запись запрещена
        assertThatThrownBy(() -> enrollmentService.enrollStudent(course.getId(), student.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);

        // списки
        List<UserBriefDto> students = enrollmentService.getStudentsForCourse(course.getId());
        assertThat(students).extracting(UserBriefDto::email).contains("s@ex.com");

        List<CourseBriefDto> courses = enrollmentService.getCoursesForStudent(student.getId());
        assertThat(courses).extracting(CourseBriefDto::title).contains("Course A");

        // завершение и отписка
        EnrollmentDto completed = enrollmentService.completeCourse(course.getId(), student.getId());
        assertThat(completed.status().name()).isEqualTo("COMPLETED");

        EnrollmentDto dropped = enrollmentService.unenrollStudent(course.getId(), student.getId());
        assertThat(dropped.status().name()).isEqualTo("DROPPED");
    }
}
