package com.example.edu_platform.it;

import com.example.edu_platform.domain.entity.Assignment;
import com.example.edu_platform.domain.entity.Course;
import com.example.edu_platform.domain.entity.Lesson;
import com.example.edu_platform.domain.entity.CourseModule;
import com.example.edu_platform.domain.entity.User;
import com.example.edu_platform.domain.enums.Role;
import com.example.edu_platform.exception.BusinessRuleViolationException;
import com.example.edu_platform.repository.AssignmentRepository;
import com.example.edu_platform.repository.LessonRepository;
import com.example.edu_platform.repository.UserRepository;
import com.example.edu_platform.repository.CourseRepository;
import com.example.edu_platform.repository.ModuleRepository;
import com.example.edu_platform.service.SubmissionService;
import com.example.edu_platform.service.dto.GradeSubmissionCmd;
import com.example.edu_platform.service.dto.SubmissionDto;
import com.example.edu_platform.service.dto.SubmitAssignmentCmd;
import com.example.edu_platform.service.AssignmentService;
import com.example.edu_platform.service.dto.CreateAssignmentCmd;
import com.example.edu_platform.test.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;


class AssignmentSubmissionIT extends AbstractIntegrationTest {

    @Autowired
    UserRepository userRepo;
    @Autowired
    CourseRepository courseRepo;
    @Autowired
    ModuleRepository moduleRepo;
    @Autowired
    LessonRepository lessonRepo;
    @Autowired
    AssignmentRepository assignmentRepo;

    @Autowired
    AssignmentService assignmentService;
    @Autowired
    SubmissionService submissionService;

    @PersistenceContext
    EntityManager em;



    private Lesson newLesson() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        User teacher = userRepo.save(User.builder()
                .name("Teacher")
                .email("teacher-" + suffix + "@example.com")
                .role(Role.TEACHER)
                .build());

        Course course = courseRepo.save(Course.builder()
                .title("Course " + suffix)
                .teacher(teacher)
                .build());

        CourseModule module = moduleRepo.save(new CourseModule(
                null, course, "Module 1", 1, "Description", null, null));

        return lessonRepo.save(new Lesson(null, module, "Lesson 1", "Content", null));
    }

    @Test
    void create_submit_and_grade_with_validations_and_lazy_demo() {
        var lesson = newLesson();

        Long aId = assignmentService.createAssignment(
                lesson.getId(),
                new CreateAssignmentCmd("HW1", "do it", LocalDate.now().plusDays(1), 100)
        );

        User student = userRepo.save(
                User.builder().name("Stud").email("stud2@example.com").role(Role.STUDENT).build());

        Long subId = submissionService.submit(
                student.getId(), aId, new SubmitAssignmentCmd("my answer"));

        SubmissionDto graded = submissionService.grade(
                subId, new GradeSubmissionCmd(95, "well done"));
        assertThat(graded.score()).isEqualTo(95);
        assertThat(graded.feedback()).isEqualTo("well done");

        // invalid: score > max
        assertThatThrownBy(() -> submissionService.grade(subId, new GradeSubmissionCmd(101, null)))
                .isInstanceOf(BusinessRuleViolationException.class);

        // Demonstrate LAZY failure outside tx
        Assignment detached = assignmentRepo.findById(aId).orElseThrow();
        em.detach(detached);
        assertThatThrownBy(() -> detached.getSubmissions().size())
                .isInstanceOf(LazyInitializationException.class);

        // Proper fetch via service methods (no N+1)
        assertThat(submissionService.getSubmissionsForAssignment(aId)).hasSize(1);
        assertThat(submissionService.getSubmissionsForStudent(student.getId())).isNotEmpty();
    }

    @Test
    void submit_after_due_date_is_rejected() {
        var lesson = newLesson();
        Long aId = assignmentService.createAssignment(
                lesson.getId(),
                new CreateAssignmentCmd("HW2", "late case", LocalDate.now().minusDays(1), 10)
        );
        User student = userRepo.save(
                User.builder().name("Late").email("late@example.com").role(Role.STUDENT).build());

        assertThatThrownBy(() -> submissionService.submit(
                student.getId(), aId, new SubmitAssignmentCmd("too late")))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
