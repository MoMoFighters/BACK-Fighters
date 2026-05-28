package com.wanted.cleanarchitecture;

import com.wanted.cleanarchitecture.catalog.application.command.AddModuleCommand;
import com.wanted.cleanarchitecture.catalog.application.command.AddSectionCommand;
import com.wanted.cleanarchitecture.catalog.application.command.CreateCourseCommand;
import com.wanted.cleanarchitecture.catalog.application.command.PublishCourseCommand;
import com.wanted.cleanarchitecture.catalog.application.usecase.CourseCommandUseCase;
import com.wanted.cleanarchitecture.enrollment.application.command.EnrollCourseCommand;
import com.wanted.cleanarchitecture.enrollment.application.usecase.EnrollmentCommandUseCase;
import com.wanted.cleanarchitecture.learning.application.command.CompleteModuleCommand;
import com.wanted.cleanarchitecture.learning.application.usecase.LearningCommandUseCase;
import com.wanted.cleanarchitecture.catalog.domain.model.ContentType;
import com.wanted.cleanarchitecture.catalog.domain.model.CourseStatus;
import com.wanted.cleanarchitecture.catalog.domain.repository.CourseRepository;
import com.wanted.cleanarchitecture.enrollment.domain.model.EnrollmentStatus;
import com.wanted.cleanarchitecture.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.cleanarchitecture.learning.domain.model.ProgressStatus;
import com.wanted.cleanarchitecture.learning.domain.repository.LearningProgressRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class Module00CleanArchitectureApplicationTests {

    @Autowired
    private CourseCommandUseCase courseCommandUseCase;

    @Autowired
    private EnrollmentCommandUseCase enrollmentCommandUseCase;

    @Autowired
    private LearningCommandUseCase learningCommandUseCase;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LearningProgressRepository learningProgressRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void courseLifecycleWorksAcrossBoundedContexts() {
        Long courseId = courseCommandUseCase.handle(new CreateCourseCommand(100L, "Clean Architecture", "DDD sample"));
        courseCommandUseCase.handle(new AddSectionCommand(courseId, "Introduction", 1));
        courseCommandUseCase.handle(new AddModuleCommand(courseId, 1, "Why Boundaries Matter", ContentType.TEXT, 1));

        Long moduleId = courseRepository.findById(courseId)
                .flatMap(course -> course.getSections().stream()
                        .filter(section -> section.getSectionOrder() == 1)
                        .findFirst()
                        .flatMap(section -> section.getModules().stream()
                                .filter(module -> module.getModuleOrder() == 1)
                                .findFirst()
                                .map(module -> module.getId())))
                .orElseThrow();

        courseCommandUseCase.handle(new PublishCourseCommand(courseId));
        Long enrollmentId = enrollmentCommandUseCase.handle(new EnrollCourseCommand(200L, courseId));
        learningCommandUseCase.handle(new CompleteModuleCommand(200L, courseId, moduleId));

        assertThat(courseRepository.findById(courseId)).get()
                .extracting(course -> course.getStatus())
                .isEqualTo(CourseStatus.PUBLISHED);

        assertThat(enrollmentRepository.findActiveEnrollment(200L, courseId)).get()
                .extracting(enrollment -> enrollment.getStatus(), enrollment -> enrollment.getId())
                .containsExactly(EnrollmentStatus.ACTIVE, enrollmentId);

        assertThat(learningProgressRepository.findByUserIdAndModuleId(200L, moduleId)).get()
                .extracting(progress -> progress.getStatus())
                .isEqualTo(ProgressStatus.COMPLETED);
    }

}
