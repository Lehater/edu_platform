package com.example.edu_platform.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "quiz_submissions",
        indexes = {
                @Index(name = "idx_qs_quiz", columnList = "quiz_id"),
                @Index(name = "idx_qs_student", columnList = "student_id")
        }
        // Если нужна ровно одна попытка: добавь UNIQUE(quiz_id, student_id)
)
public class QuizSubmission {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Какой квиз проходили */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_quiz_submission_quiz"))
    private Quiz quiz;

    /** Кто проходил (User с ролью STUDENT) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_quiz_submission_student"))
    private User student;

    /** Итоговый результат в процентах 0..100 */
    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private LocalDateTime takenAt;
}
