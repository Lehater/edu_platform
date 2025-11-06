package com.example.edu_platform.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import com.example.edu_platform.service.dto.CourseBriefDto;
import com.example.edu_platform.service.dto.EnrollmentDto;
import com.example.edu_platform.service.dto.UserBriefDto;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    /**
     * Зачисление студента на курс.
     * Бизнес-правила:
     * - пользователь существует и имеет роль STUDENT;
     * - курс существует;
     * - нет дубликата записи (unique user+course);
     * - создаём запись со статусом ACTIVE.
     */
    @Transactional
    public EnrollmentDto enrollStudent(Long courseId, Long studentId) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new DomainNotFoundException("User not found: " + studentId));
        if (user.getRole() != Role.STUDENT) {
            throw new BusinessRuleViolationException("Only users with STUDENT role can enroll. UserId=" + studentId);
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new DomainNotFoundException("Course not found: " + courseId));

        enrollmentRepository.findByUserAndCourse(user, course).ifPresent(e -> {
            throw new BusinessRuleViolationException(
                    "Student already enrolled to this course. userId=%d, courseId=%d".formatted(studentId, courseId)
            );
        });

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .enrollDate(LocalDate.now())
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        return new EnrollmentDto(saved.getId(), course.getId(), user.getId(), saved.getStatus(), saved.getEnrollDate());
    }

    /**
     * Смена статуса зачисления на COMPLETED (завершение курса).
     */
    @Transactional
    public EnrollmentDto completeCourse(Long courseId, Long studentId) {
        Enrollment e = getEnrollmentOrThrow(courseId, studentId);
        e.setStatus(EnrollmentStatus.COMPLETED);
        return new EnrollmentDto(e.getId(), e.getCourse().getId(), e.getUser().getId(), e.getStatus(), e.getEnrollDate());
    }

    /**
     * Отписка студента (меняет статус на DROPPED). Не удаляем запись, чтобы не терять историю.
     * Если нужен физический delete — можно добавить отдельный метод.
     */
    @Transactional
    public EnrollmentDto unenrollStudent(Long courseId, Long studentId) {
        Enrollment e = getEnrollmentOrThrow(courseId, studentId);
        e.setStatus(EnrollmentStatus.DROPPED);
        return new EnrollmentDto(e.getId(), e.getCourse().getId(), e.getUser().getId(), e.getStatus(), e.getEnrollDate());
    }

    /**
     * Список студентов, записанных на курс (актуальный статус не фильтруем, можно добавить параметр).
     */
    @Transactional
    public List<UserBriefDto> getStudentsForCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new DomainNotFoundException("Course not found: " + courseId));

        return enrollmentRepository.findAllByCourse(course).stream()
                .map(e -> {
                    User u = e.getUser(); // LAZY, но внутри транзакции безопасно
                    return new UserBriefDto(u.getId(), u.getName(), u.getEmail());
                })
                .toList();
    }

    /**
     * Список курсов, на которые записан студент.
     */
    @Transactional
    public List<CourseBriefDto> getCoursesForStudent(Long studentId) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new DomainNotFoundException("User not found: " + studentId));

        return enrollmentRepository.findAllByUser(user).stream()
                .map(e -> {
                    Course c = e.getCourse();
                    return new CourseBriefDto(c.getId(), c.getTitle());
                })
                .toList();
    }

    private Enrollment getEnrollmentOrThrow(Long courseId, Long studentId) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new DomainNotFoundException("User not found: " + studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new DomainNotFoundException("Course not found: " + courseId));

        return enrollmentRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new DomainNotFoundException(
                        "Enrollment not found for userId=%d, courseId=%d".formatted(studentId, courseId)));
    }
}
