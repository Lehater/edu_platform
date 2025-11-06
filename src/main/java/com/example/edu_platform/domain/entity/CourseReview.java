package com.example.edu_platform.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "course_reviews",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_review_course_student", columnNames = {"course_id", "student_id"}
        ),
        indexes = {
                @Index(name = "idx_review_course", columnList = "course_id"),
                @Index(name = "idx_review_student", columnList = "student_id")
        })
public class CourseReview {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Какой курс оценивается */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_course"))
    private Course course;

    /** Кто оставил отзыв (User с ролью STUDENT) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_student"))
    private User student;

    /** Оценка 1..5 */
    @NotNull @Min(1) @Max(5)
    @Column(nullable = false)
    private Integer rating;

    /** Текст отзыва (опционально) */
    @Column(columnDefinition = "text")
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
