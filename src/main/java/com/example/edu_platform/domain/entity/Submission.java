package com.example.edu_platform.domain.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "submissions",
        uniqueConstraints = @UniqueConstraint(name = "uk_submission_student_assignment",
                columnNames = {"student_id", "assignment_id"}),
        indexes = {
                @Index(name = "idx_submission_assignment", columnList = "assignment_id"),
                @Index(name = "idx_submission_student", columnList = "student_id")
        })
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * За какое задание
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_submission_assignment"))
    private Assignment assignment;

    /**
     * Кто сдал (User с ролью STUDENT)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_submission_student"))
    private User student;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    /**
     * Текст ответа или ссылка на файл/репозиторий
     */
    @NotBlank
    @Column(columnDefinition = "text", nullable = false)
    private String content;

    /**
     * Оценка: null до проверки; должна быть <= assignment.maxScore
     */
    @Column
    private Integer score;

    /**
     * Комментарий преподавателя к оценке
     */
    @Column(columnDefinition = "text")
    private String feedback;
}
