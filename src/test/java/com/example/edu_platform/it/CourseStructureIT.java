package com.example.edu_platform.it;

import com.example.edu_platform.domain.entity.*;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.repository.*;
import com.example.edu_platform.service.CourseService;
import com.example.edu_platform.service.dto.AddLessonCmd;
import com.example.edu_platform.service.dto.AddModuleCmd;
import com.example.edu_platform.service.dto.CreateCourseCmd;
import com.example.edu_platform.service.dto.CourseTreeDto;
import com.example.edu_platform.test.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class CourseStructureIT extends AbstractIntegrationTest {

    @Autowired UserRepository userRepo;
    @Autowired CategoryRepository categoryRepo;
    @Autowired TagRepository tagRepo;
    @Autowired CourseRepository courseRepo;
    @Autowired ModuleRepository moduleRepo;
    @Autowired LessonRepository lessonRepo;
    @Autowired CourseService courseService;
    @PersistenceContext EntityManager em;

    @Test
    void create_course_add_modules_lessons_and_fetch_tree() {
        User teacher = userRepo.save(User.builder().name("Bob").email("bob@ex.com").role(Role.TEACHER).build());
        Category cat = categoryRepo.save(Category.builder().name("DB").build());
        Tag t1 = tagRepo.save(Tag.builder().name("JPA").build());

        Long courseId = courseService.createCourse(new CreateCourseCmd(
                "ORM 101", "Intro", cat.getId(), teacher.getId(), "3w", LocalDate.now(), Set.of(t1.getId())
        ));

        Long m1 = courseService.addModule(courseId, new AddModuleCmd("Module 1", 1, "Basics"));
        Long m2 = courseService.addModule(courseId, new AddModuleCmd("Module 2", 2, "Relations"));

        courseService.addLesson(m1, new AddLessonCmd("L1", "content 1", null));
        courseService.addLesson(m1, new AddLessonCmd("L2", "content 2", null));
        courseService.addLesson(m2, new AddLessonCmd("L3", "content 3", null));

        // Проверяем LAZY-ошибку вне транзакции (специально очищаем контекст)
        Course detached = courseRepo.findById(courseId).orElseThrow();
        em.detach(detached);
        assertThatThrownBy(() -> detached.getCourseModules().size())
                .isInstanceOf(LazyInitializationException.class);

        // Корректная выборка дерева без N+1
        CourseTreeDto tree = courseService.getCourseTree(courseId);
        assertThat(tree.modules()).hasSize(2);
        assertThat(tree.modules().get(0).lessons()).hasSize(2);
        assertThat(tree.modules().get(1).lessons()).hasSize(1);
    }
}
