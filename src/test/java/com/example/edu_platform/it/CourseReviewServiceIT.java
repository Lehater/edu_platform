package com.example.edu_platform.it;

import com.example.edu_platform.domain.entity.Course;
import com.example.edu_platform.domain.entity.User;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.repository.CourseRepository;
import com.example.edu_platform.repository.UserRepository;
import com.example.edu_platform.service.CourseReviewService;
import com.example.edu_platform.service.EnrollmentService;
import com.example.edu_platform.service.dto.review_dtos.CourseRatingDto;
import com.example.edu_platform.service.dto.review_dtos.ReviewDto;
import com.example.edu_platform.service.dto.review_dtos.UpsertReviewCmd;
import com.example.edu_platform.test.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class CourseReviewServiceIT extends AbstractIntegrationTest {

    @Autowired UserRepository userRepo;
    @Autowired CourseRepository courseRepo;
    @Autowired EnrollmentService enrollmentService;
    @Autowired CourseReviewService reviewService;

    @Test
    void upsert_list_and_rating() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User teacher = userRepo.save(
                User.builder()
                        .name("T-Review")
                        .email("t-review" + suffix + "@example.com")
                        .role(Role.TEACHER).build());
        User s1 = userRepo.save(
                User.builder()
                        .name("S1")
                        .email("s1-review" + suffix + "@example.com")
                        .role(Role.STUDENT).build());
        User s2 = userRepo.save(
                User.builder()
                        .name("S2")
                        .email("s2-review" + suffix + "@example.com")
                        .role(Role.STUDENT).build());
        Course course = courseRepo.save(
                Course.builder().title("Review Course").teacher(teacher).build());

        enrollmentService.enrollStudent(course.getId(), s1.getId());
        enrollmentService.enrollStudent(course.getId(), s2.getId());

        ReviewDto r1 = reviewService.upsertReview(
                course.getId(), s1.getId(), new UpsertReviewCmd(5, "great"));
        assertThat(r1.rating()).isEqualTo(5);

        reviewService.upsertReview(
                course.getId(), s2.getId(), new UpsertReviewCmd(4, "good"));
        ReviewDto updated = reviewService.upsertReview(
                course.getId(), s2.getId(), new UpsertReviewCmd(3, "ok"));

        assertThat(updated.rating()).isEqualTo(3);

        assertThat(reviewService.getReviewsForCourse(course.getId()))
                .hasSize(2);

        CourseRatingDto rating = reviewService.getCourseRating(course.getId());
        assertThat(rating.total()).isEqualTo(2);
        assertThat(rating.average()).isBetween(3.9, 4.1);
    }
}
