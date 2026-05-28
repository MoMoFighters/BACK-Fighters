package com.wanted.cleanarchitecture.catalog.application.service;

import com.wanted.cleanarchitecture.catalog.application.command.AddModuleCommand;
import com.wanted.cleanarchitecture.catalog.application.command.AddSectionCommand;
import com.wanted.cleanarchitecture.catalog.application.command.CreateCourseCommand;
import com.wanted.cleanarchitecture.catalog.application.command.PublishCourseCommand;
import com.wanted.cleanarchitecture.catalog.application.usecase.CourseCommandUseCase;
import com.wanted.cleanarchitecture.catalog.domain.model.Course;
import com.wanted.cleanarchitecture.catalog.domain.repository.CourseRepository;
import com.wanted.cleanarchitecture.global.domain.common.event.DomainEvent;
import com.wanted.cleanarchitecture.global.domain.common.exception.DomainRuleViolationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/*
 * CourseCommandService는 catalog 컨텍스트의 Application Service다.
 *
 * Application Service의 책임:
 * - 하나의 UseCase 흐름을 조립한다.
 * - 트랜잭션 경계를 가진다.
 * - Repository Port로 Aggregate를 조회하고 저장한다.
 * - Aggregate의 도메인 행위를 호출한다.
 * - 저장 후 DomainEvent를 발행한다.
 *
 * Clock을 사용하는 이유:
 * - "현재 시간"은 외부 환경에 따라 달라지는 값이다.
 * - 도메인 객체 안에서 Instant.now()를 직접 호출하면 테스트에서 시간을 고정하기 어렵다.
 * - Application Service가 Clock을 통해 현재 시각을 구하고, Domain에는 Instant 값을 전달한다.
 * - 이렇게 하면 테스트에서는 고정 Clock을 사용할 수 있고, 운영에서는 시스템 Clock을 사용할 수 있다.
 *
 * 현재 예제에서는 생성자에서 Clock.systemUTC()를 사용하지만,
 * 실무나 테스트 확장 시 Clock을 Bean으로 주입받도록 바꾸면 더 테스트하기 쉬워진다.
 */
@Service
@Transactional
public class CourseCommandService implements CourseCommandUseCase {

    private final CourseRepository courseRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public CourseCommandService(CourseRepository courseRepository, ApplicationEventPublisher eventPublisher) {
        this.courseRepository = courseRepository;
        this.eventPublisher = eventPublisher;
        this.clock = Clock.systemUTC();
    }

    @Override
    public Long handle(CreateCourseCommand command) {
        Course course = Course.create(command.authorId(), command.title(), command.description());
        Course saved = courseRepository.save(course);
        return saved.getId();
    }

    @Override
    public void handle(AddSectionCommand command) {
        Course course = getCourse(command.courseId());
        course.addSection(command.title(), command.sectionOrder());
        courseRepository.save(course);
    }

    @Override
    public void handle(AddModuleCommand command) {
        Course course = getCourse(command.courseId());
        course.addModule(command.sectionOrder(), command.title(), command.contentType(), command.moduleOrder());
        courseRepository.save(course);
    }

    @Override
    public void handle(PublishCourseCommand command) {
        Course course = getCourse(command.courseId());

        /*
         * clock.instant()로 현재 시각을 만든 뒤 Course.publish()에 전달한다.
         *
         * Course는 "언제 공개되었는가"라는 결과 시각은 필요하지만,
         * 현재 시각을 어떻게 구하는지는 몰라도 된다.
         */
        course.publish(clock.instant());
        courseRepository.save(course);

        /*
         * 저장 후 이벤트를 발행한다.
         *
         * Course.publish()는 이벤트를 기록만 한다.
         * 이 Service는 @Transactional 경계를 알고 있으므로 저장과 발행 순서를 조립한다.
         */
        publishAll(course.pullDomainEvents());
    }

    /* command class 에서도 필요시 findById 같은 영속성 객체 추출 작업은 진행할 수 있다.
     * 이벤트스토밍의 Read-Model 로 판단한다.
     * */
    private Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new DomainRuleViolationException("Course not found: " + courseId));
    }

    /*
     * publishAll()은 Aggregate에서 꺼낸 DomainEvent들을 Spring Event로 발행하는 메서드다.
     *
     * pullDomainEvents(): Aggregate에서 이벤트를 꺼낸다.
     * publishAll(): 꺼낸 이벤트를 외부 발행 메커니즘에 넘긴다.
     */
    private void publishAll(Iterable<DomainEvent> domainEvents) {
        domainEvents.forEach(eventPublisher::publishEvent);
    }
}
