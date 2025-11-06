package com.example.edu_platform.service;

import com.example.edu_platform.domain.entity.Course;
import com.example.edu_platform.domain.entity.CourseReview;
import com.example.edu_platform.domain.entity.Enrollment;
import com.example.edu_platform.domain.entity.User;
import com.example.edu_platform.domain.enums.EnrollmentStatus;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.exception.BusinessRuleViolationException;
import com.example.edu_platform.exception.DomainNotFoundException;
import com.example.edu_platform.repository.*;
import com.example.edu_platform.service.dto.review_dtos.CourseRatingDto;
import com.example.edu_platform.service.dto.review_dtos.ReviewDto;
import com.example.edu_platform.service.dto.review_dtos.UpsertReviewCmd;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseReviewService {

    private final CourseReviewRepository courseReviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * Создать или обновить отзыв.
     * Правила:
     * - студент существует и имеет роль STUDENT;
     * - курс существует;
     * - студент зачислен на курс (есть Enrollment; статус можно не проверять, но желательно ACTIVE/COMPLETED);
     * - один отзыв на курс от студента: при повторном вызове обновляем rating/comment и createdAt.
     */
    @Transactional
    public ReviewDto upsertReview(Long courseId, Long studentId, UpsertReviewCmd cmd) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new DomainNotFoundException("Course not found: " + courseId));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new DomainNotFoundException("User not found: " + studentId));

        if (student.getRole() != Role.STUDENT) {
            throw new BusinessRuleViolationException("Only STUDENT can leave reviews. userId=" + studentId);
        }

        Enrollment enrollment = enrollmentRepository.findByUserAndCourse(student, course)
                .orElseThrow(() -> new BusinessRuleViolationException("Student is not enrolled to this course."));

        // (Опционально) Требовать статус ACTIVE или COMPLETED:
        if (enrollment.getStatus() == EnrollmentStatus.DROPPED) {
            throw new BusinessRuleViolationException("Dropped students cannot leave reviews.");
        }

        CourseReview review = courseReviewRepository.findByCourseAndStudent(course, student)
                .orElseGet(() -> CourseReview.builder()
                        .course(course)
                        .student(student)
                        .createdAt(LocalDateTime.now())
                        .rating(cmd.rating())
                        .comment(cmd.comment())
                        .build()
                );

        // Если отзыв новый — просто сохраняем; если существующий — обновляем поля
        if (review.getId() != null) {
            review.setRating(cmd.rating());
            review.setComment(cmd.comment());
            review.setCreatedAt(LocalDateTime.now());
        }

        CourseReview saved = courseReviewRepository.save(review);
        return new ReviewDto(saved.getId(), course.getId(), student.getId(),
                saved.getRating(), saved.getComment(), saved.getCreatedAt());
    }

    /**
     * Удалить отзыв (например, модерация).
     */
    @Transactional
    public void deleteReview(Long reviewId) {
        if (!courseReviewRepository.existsById(reviewId)) {
            throw new DomainNotFoundException("Review not found: " + reviewId);
        }
        courseReviewRepository.deleteById(reviewId);
    }

    /**
     * Список отзывов по курсу (новые сверху).
     */
    @Transactional
    public List<ReviewDto> getReviewsForCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new DomainNotFoundException("Course not found: " + courseId));
        return courseReviewRepository.findAllByCourseOrderByCreatedAtDesc(course).stream()
                .map(r -> new ReviewDto(r.getId(), course.getId(), r.getStudent().getId(),
                        r.getRating(), r.getComment(), r.getCreatedAt()))
                .toList();
    }

    /**
     * Средняя оценка и количество отзывов по курсу.
     */
    @Transactional
    public CourseRatingDto getCourseRating(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new DomainNotFoundException("Course not found: " + courseId));

        Double avg = courseReviewRepository.findAverageRating(course);
        long total = courseReviewRepository.countByCourse(course);
        double average = avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0; // округлим до 0.1

        return new CourseRatingDto(course.getId(), average, total);
    }
}
