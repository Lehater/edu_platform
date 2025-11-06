package com.example.edu_platform.it;

import com.example.edu_platform.domain.entity.Category;
import com.example.edu_platform.domain.entity.Course;
import com.example.edu_platform.domain.entity.User;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.repository.CategoryRepository;
import com.example.edu_platform.repository.CourseRepository;
import com.example.edu_platform.repository.UserRepository;
import com.example.edu_platform.test.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class CourseRepositoryIT extends AbstractIntegrationTest {

    @Autowired UserRepository userRepo;
    @Autowired CategoryRepository categoryRepo;
    @Autowired CourseRepository courseRepo;

    @Test
    void createCourse_withTeacherAndCategory() {
        User t = userRepo.save(User.builder().name("Teacher A").email("t@example.com").role(Role.TEACHER).build());
        Category cat = categoryRepo.save(Category.builder().name("Programming").build());

        Course c = Course.builder().title("Hibernate Basics").teacher(t).category(cat).build();
        Course saved = courseRepo.save(c);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTeacher().getId()).isEqualTo(t.getId());
        assertThat(saved.getCategory().getId()).isEqualTo(cat.getId());
    }
}
