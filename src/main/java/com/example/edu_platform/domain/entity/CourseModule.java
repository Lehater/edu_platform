package com.example.edu_platform.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "modules",
        indexes = {
                @Index(name = "idx_module_course", columnList = "course_id"),
                @Index(name = "idx_module_order", columnList = "course_id, order_index")
        })
public class CourseModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_module_course"))
    private Course course;

    @NotBlank
    @Column(nullable = false, length = 300)
    private String title;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(columnDefinition = "text")
    private String description;

    @OneToOne(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Quiz quiz;

    // 1:N Module -> Lesson
    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private Set<Lesson> lessons = new LinkedHashSet<>();
}
