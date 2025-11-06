package com.example.edu_platform.it;

import com.example.edu_platform.domain.entity.Course;
import com.example.edu_platform.domain.entity.CourseModule;
import com.example.edu_platform.domain.entity.Lesson;
import com.example.edu_platform.domain.entity.User;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.exception.BusinessRuleViolationException;
import com.example.edu_platform.repository.CourseRepository;
import com.example.edu_platform.repository.EnrollmentRepository;
import com.example.edu_platform.repository.LessonRepository;
import com.example.edu_platform.repository.ModuleRepository;
import com.example.edu_platform.repository.UserRepository;
import com.example.edu_platform.service.CourseService;
import com.example.edu_platform.service.EnrollmentService;
import com.example.edu_platform.test.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class CourseDeletionPolicyIT extends AbstractIntegrationTest {

    @Autowired CourseRepository courseRepo;
    @Autowired ModuleRepository moduleRepo;
    @Autowired LessonRepository lessonRepo;
    @Autowired UserRepository userRepo;
    @Autowired EnrollmentService enrollmentService;
    @Autowired EnrollmentRepository enrollmentRepo;
    @Autowired CourseService courseService;

    @PersistenceContext
    EntityManager em;

    /**
     * A) Удаление курса без «чувствительных» артефактов должно каскадно удалить структуру:
     * Course -> Module(s) -> Lesson(s)
     */
    @Test
    void deleting_course_without_enrollments_cascades_structure() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User teacher = userRepo.save(
                User.builder()
                        .name("T-Del")
                        .email("t-del" + suffix + "@example.com")
                        .role(Role.TEACHER).build());

        Course course = courseRepo.save(Course.builder().title("ToDelete").teacher(teacher).build());

        // Создаём структуру: Course -> CourseModule -> Lesson
        CourseModule m1 = CourseModule.builder().course(course).title("M1").orderIndex(1).build();
        moduleRepo.save(m1);
        Lesson l1 = Lesson.builder().module(m1).title("L1").content("c1").build();
        lessonRepo.save(l1);

        // sanity-check
        assertThat(moduleRepo.count()).isGreaterThanOrEqualTo(1);
        assertThat(lessonRepo.count()).isGreaterThanOrEqualTo(1);

        // Удаляем через сервис — структура должна удалиться каскадом
        courseService.deleteCourse(course.getId());

        assertThat(courseRepo.findById(course.getId())).isEmpty();
        assertThat(moduleRepo.findById(m1.getId())).isEmpty();
        assertThat(lessonRepo.findById(l1.getId())).isEmpty();
    }

    /**
     * B) При наличии Enrollment удаление курса запрещено доменной логикой.
     * Ожидаем BusinessRuleViolationException из CourseService.deleteCourse(...).
     */
    @Test
    void deleting_course_with_enrollment_should_fail_with_domain_error() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        User teacher = userRepo.save(
                User.builder().name("T-Del2").email("t-del2" + suffix + "@example.com").role(Role.TEACHER).build());
        User student = userRepo.save(
                User.builder().name("S-Del2").email("s-del2" + suffix + "@example.com").role(Role.STUDENT).build());

        Course course = courseRepo.save(
                Course.builder().title("Has Enroll").teacher(teacher).build());

        // записываем студента (status ACTIVE)
        enrollmentService.enrollStudent(course.getId(), student.getId());
        assertThat(enrollmentRepo.findAllByCourse(course)).hasSize(1);

        // Пытаемся удалить курс → получаем доменную ошибку
        assertThatThrownBy(() -> courseService.deleteCourse(course.getId()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot delete course with enrollments");

        // Запись о зачислении остаётся
        assertThat(enrollmentRepo.findAllByCourse(course)).hasSize(1);
        // Сам курс тоже остаётся
        assertThat(courseRepo.findById(course.getId())).isPresent();
    }
}
