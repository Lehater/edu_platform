package com.example.edu_platform.service;

import com.example.edu_platform.exception.BusinessRuleViolationException;
import com.example.edu_platform.exception.DomainNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.edu_platform.domain.entity.CourseModule;
import com.example.edu_platform.domain.entity.*;
import com.example.edu_platform.repository.*;
import com.example.edu_platform.service.dto.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public Long createCourse(CreateCourseCmd cmd) {
        User teacher = userRepository.findById(cmd.teacherId())
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found: " + cmd.teacherId()));

        Category category = null;
        if (cmd.categoryId() != null) {
            category = categoryRepository.findById(cmd.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found: " + cmd.categoryId()));
        }

        Course course = Course.builder()
                .title(cmd.title())
                .description(cmd.description())
                .category(category)
                .teacher(teacher)
                .duration(cmd.duration())
                .startDate(cmd.startDate())
                .build();

        if (cmd.tagIds() != null && !cmd.tagIds().isEmpty()) {
            var tags = new LinkedHashSet<Tag>(tagRepository.findAllById(cmd.tagIds()));
            // (опционально) проверить, что все id найдены:
            if (tags.size() != cmd.tagIds().size()) {
                throw new EntityNotFoundException("Some tags not found");
            }
            course.setTags(tags);           // <- вместо course.getTags().add(...)
        }

        return courseRepository.save(course).getId();
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new DomainNotFoundException("Course not found: " + courseId));

        long enrollments = enrollmentRepository.countByCourse(course);
        if (enrollments > 0) {
            throw new BusinessRuleViolationException(
                    "Cannot delete course with enrollments (" + enrollments + " found)");
        }

        courseRepository.delete(course);
    }

    @Transactional
    public Long addModule(Long courseId, AddModuleCmd cmd) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found: " + courseId));
        CourseModule module = CourseModule.builder()
                .course(course)
                .title(cmd.title())
                .orderIndex(cmd.orderIndex())
                .description(cmd.description())
                .build();
        CourseModule saved = moduleRepository.save(module);
        course.getCourseModules().add(saved); // поддерживаем двунаправленность
        return saved.getId();
    }

    @Transactional
    public Long addLesson(Long moduleId, AddLessonCmd cmd) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found: " + moduleId));
        Lesson lesson = Lesson.builder()
                .module(module)
                .title(cmd.title())
                .content(cmd.content())
                .videoUrl(cmd.videoUrl())
                .build();
        Lesson saved = lessonRepository.save(lesson);
        module.getLessons().add(saved); // поддерживаем двунаправленность
        return saved.getId();
    }

    /**
     * Безопасное чтение структуры без N+1.
     * Использует EntityGraph (или можно переключиться на JPQL fetchTreeById).
     */
    @Transactional
    public CourseTreeDto getCourseTree(Long courseId) {
        Course course = courseRepository.findWithModulesAndLessonsById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found: " + courseId));

        // Маппинг в простые DTO — исключаем случайную дальнейшую ленивую подгрузку вне транзакции.
        List<CourseModuleDto> moduleDtos = new ArrayList<>();
        for (CourseModule m : course.getCourseModules()) {
            List<CourseLessonDto> lessonDtos = new ArrayList<>();
            for (Lesson l : m.getLessons()) {
                lessonDtos.add(new CourseLessonDto(
                        l.getId(), l.getTitle(), l.getContent(), l.getVideoUrl()
                ));
            }
            moduleDtos.add(new CourseModuleDto(
                    m.getId(), m.getTitle(), m.getOrderIndex(), m.getDescription(), lessonDtos
            ));
        }
        String categoryName = course.getCategory() != null ? course.getCategory().getName() : null;
        String teacherName = course.getTeacher() != null ? course.getTeacher().getName() : null;

        return new CourseTreeDto(
                course.getId(), course.getTitle(), course.getDescription(),
                categoryName, teacherName, moduleDtos
        );
    }
}
