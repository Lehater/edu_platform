package com.example.edu_platform.domain.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",
            foreignKey = @ForeignKey(name = "fk_course_category"))
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id",
            foreignKey = @ForeignKey(name = "fk_course_teacher"))
    private User teacher;

    @Column(length = 100)
    private String duration;

    private LocalDate startDate;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "course_tag",
            joinColumns = @JoinColumn(name = "course_id",
                    foreignKey = @ForeignKey(name = "fk_ct_course")),
            inverseJoinColumns = @JoinColumn(name = "tag_id",
                    foreignKey = @ForeignKey(name = "fk_ct_tag")),
            uniqueConstraints = @UniqueConstraint(name = "uk_course_tag", columnNames = {"course_id", "tag_id"})
    )
    private Set<Tag> tags = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC, id ASC")
    private Set<CourseModule> courseModules = new LinkedHashSet<>();
}
