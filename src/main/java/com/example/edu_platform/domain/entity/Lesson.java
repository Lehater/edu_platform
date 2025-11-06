package com.example.edu_platform.domain.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "lessons",
        indexes = @Index(name = "idx_lesson_module", columnList = "module_id"))
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lesson_module"))
    private CourseModule module;

    @NotBlank
    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "text")
    private String content;

    @Column(length = 1024)
    private String videoUrl;
}
