package com.example.edu_platform.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "quizzes",
        indexes = @Index(name = "idx_quiz_module", columnList = "module_id"))
public class Quiz {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 1:1 с Module, опционально */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", unique = true, nullable = false,
            foreignKey = @ForeignKey(name = "fk_quiz_module"))
    private CourseModule module;

    @NotBlank
    @Column(nullable = false, length = 300)
    private String title;

    /** Лимит времени (минуты), опционально */
    @Column(name = "time_limit")
    private Integer timeLimit;

    /** Вопросы квиза */
    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private Set<Question> questions = new LinkedHashSet<>();

    /** Результаты прохождения */
    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<QuizSubmission> submissions = new LinkedHashSet<>();
}
