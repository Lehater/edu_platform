package com.example.edu_platform.unit;

import com.example.edu_platform.domain.entity.Category;
import com.example.edu_platform.domain.entity.Course;
import com.example.edu_platform.domain.entity.Tag;
import com.example.edu_platform.domain.entity.User;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.exception.DomainNotFoundException;
import com.example.edu_platform.repository.*;
import com.example.edu_platform.service.CourseService;
import com.example.edu_platform.service.dto.CreateCourseCmd;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceUnitTest {

    @Mock CourseRepository courseRepo;
    @Mock UserRepository userRepo;
    @Mock CategoryRepository categoryRepo;
    @Mock TagRepository tagRepo;
    @Mock ModuleRepository moduleRepo; // на всякий

    @InjectMocks CourseService service;

    @Test
    void createCourse_fails_when_teacher_not_found() {
        CreateCourseCmd cmd = new CreateCourseCmd(
                "T", "D", 1L, 99L, "4w", LocalDate.now(), Set.of()
        );
        when(userRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createCourse(cmd))
                .isInstanceOf(DomainNotFoundException.class)
                .hasMessageContaining("User not found");
        verify(courseRepo, never()).save(any());
    }

    @Test
    void createCourse_succeeds_with_tags_and_category() {
        long teacherId = 2L, categoryId = 3L;
        var tagIds = Set.of(10L, 11L);

        User teacher = User.builder().id(teacherId).role(Role.TEACHER).name("T").email("t@ex.com").build();
        Category category = Category.builder().id(categoryId).name("Programming").build();
        Tag t1 = Tag.builder().id(10L).name("Java").build();
        Tag t2 = Tag.builder().id(11L).name("Hibernate").build();

        when(userRepo.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(categoryRepo.findById(categoryId)).thenReturn(Optional.of(category));
        when(tagRepo.findAllById(tagIds)).thenReturn(List.of(t1, t2));
        when(courseRepo.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });

        CreateCourseCmd cmd = new CreateCourseCmd(
                "ORM 101", "Intro", categoryId, teacherId, "3w", LocalDate.now(), tagIds
        );

        Long id = service.createCourse(cmd);
        assertThat(id).isEqualTo(100L);
        verify(courseRepo).save(argThat(c ->
                c.getTitle().equals("ORM 101")
                        && c.getTeacher().getId().equals(teacherId)
                        && c.getCategory().getId().equals(categoryId)
                        && c.getTags().size() == 2
        ));
    }
}
