package com.example.edu_platform.domain.entity;

import com.example.edu_platform.domain.enums.AssignmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "assignments",
        indexes = {
                @Index(name = "idx_assignment_lesson", columnList = "lesson_id"),
                @Index(name = "idx_assignment_due", columnList = "due_date")
        })
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_assignment_lesson"))
    private Lesson lesson;

    @NotBlank
    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    /**
     * Дедлайн (дата, как в ТЗ). Считаем, что сдача после dueDate запрещена (см. сервис).
     */
    @Column(name = "due_date")
    private LocalDate dueDate;

    @NotNull
    @Min(1)
    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AssignmentStatus status;

    /**
     * 1:N Assignment -> Submission (лениво, каскад не нужен)
     */
    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("submittedAt ASC, id ASC")
    private Set<Submission> submissions = new LinkedHashSet<>();
}
