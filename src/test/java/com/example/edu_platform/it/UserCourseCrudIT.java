package com.example.edu_platform.it;

import com.example.edu_platform.domain.entity.*;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.repository.*;
import com.example.edu_platform.test.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

class UserCourseCrudIT extends AbstractIntegrationTest {

    @Autowired
    UserRepository userRepo;
    @Autowired
    ProfileRepository profileRepo;
    @Autowired
    CategoryRepository categoryRepo;
    @Autowired
    TagRepository tagRepo;
    @Autowired
    CourseRepository courseRepo;

    @Test
    void user_profile_category_tag_course_crud() {
        // Create User + Profile
        User teacher = userRepo.save(User.builder().name("Alice Teacher").email("alice@ex.com").role(Role.TEACHER).build());
        Profile p = profileRepo.save(Profile.builder().user(teacher).bio("Senior").avatarUrl("http://.../a.png").build());
        assertThat(p.getId()).isNotNull();
        assertThat(p.getUser().getId()).isEqualTo(teacher.getId());

        // Update Profile
        p.setBio("Principal Lecturer");
        profileRepo.save(p);
        assertThat(profileRepo.findById(p.getId()).orElseThrow().getBio()).isEqualTo("Principal Lecturer");

        // Category + Tag
        Category cat = categoryRepo.save(Category.builder().name("Programming").build());
        Tag java = tagRepo.save(Tag.builder().name("Java").build());
        Tag hiber = tagRepo.save(Tag.builder().name("Hibernate").build());

        // Create Course with teacher/category/tags
        Course c = Course.builder().title("Hibernate Basics").teacher(teacher).category(cat).duration("4w").build();
        c.getTags().add(java);
        c.getTags().add(hiber);
        Course saved = courseRepo.save(c);

        // Read
        Course found = courseRepo.findById(saved.getId()).orElseThrow();
        assertThat(found.getId()).isNotNull();
        assertThat(found.getTeacher().getId()).isEqualTo(teacher.getId());
        assertThat(found.getCategory().getId()).isEqualTo(cat.getId());

        // Update
        found.setTitle("Hibernate & JPA Basics");
        courseRepo.save(found);
        assertThat(courseRepo.findById(found.getId()).orElseThrow().getTitle()).contains("JPA");

        // Delete (курс без структуры/артефактов удаляется)
        courseRepo.delete(found);
        assertThat(courseRepo.findById(found.getId())).isEmpty();
    }
}
