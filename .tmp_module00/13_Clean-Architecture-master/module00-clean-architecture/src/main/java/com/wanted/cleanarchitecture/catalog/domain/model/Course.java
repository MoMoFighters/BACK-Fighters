package com.wanted.cleanarchitecture.catalog.domain.model;

import com.wanted.cleanarchitecture.catalog.domain.event.CoursePublishedEvent;
import com.wanted.cleanarchitecture.global.domain.common.event.DomainEvent;
import com.wanted.cleanarchitecture.global.domain.common.exception.DomainRuleViolationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/*
 * Course는 catalog Bounded Context의 Aggregate Root다.
 *
 * 계층별 호출 흐름:
 * 1. CourseController가 HTTP 요청을 Command로 변환한다.
 * 2. CourseCommandService가 CourseRepository로 Course를 조회한다.
 * 3. CourseCommandService가 Course.publish() 같은 도메인 행위를 호출한다.
 * 4. Course는 자기 상태를 기준으로 비즈니스 규칙을 검증하고 상태를 바꾼다.
 * 5. Course는 중요한 결과가 발생하면 DomainEvent를 내부에 기록한다.
 * 6. CourseCommandService가 Course를 저장한 뒤 pullDomainEvents()로 이벤트를 꺼내 발행한다.
 *
 * 중요한 점:
 * - Course는 HTTP, JPA, Spring Event 발행 방식을 모른다.
 * - Course는 "강의가 공개 가능한가?" 같은 순수 도메인 규칙만 책임진다.
 * - CourseSection과 ContentModule은 Course를 통해서만 변경되어야 Aggregate 규칙이 보호된다.
 *
 * 도메인 클래스의 메서드 기준:
 * - publish(), addSection(), addModule()처럼 업무 행위를 표현한다.
 * - ensureDraft(), validatePublishable()처럼 비즈니스 규칙을 검증한다.
 * - CoursePublishedEvent처럼 업무적으로 의미 있는 결과를 기록한다.
 *
 * JPA Entity 메서드와의 차이:
 * - CourseJpaEntity.changeStatus()는 DB 컬럼 값을 맞추기 위한 저장 모델 메서드다.
 * - Course.publish()는 공개 규칙 검증, 상태 변경, 이벤트 기록을 포함하는 도메인 행위다.
 * - 따라서 Application Service는 가능하면 CourseJpaEntity가 아니라 Course의 도메인 메서드를 호출해야 한다.
 */
public class Course {

    private final Long id;
    private final Long authorId;
    private String title;
    private String description;
    private CourseStatus status;
    private final List<CourseSection> sections;

    /*
     * domainEvents는 Aggregate 안에서 발생한 DomainEvent를 잠시 보관하는 버퍼다.
     *
     * 왜 바로 발행하지 않는가?
     * - Domain 계층은 Spring의 ApplicationEventPublisher를 몰라야 한다.
     * - 저장이 실패했는데 이벤트만 발행되면 잘못된 후속 처리가 실행될 수 있다.
     * - 따라서 Aggregate는 기록만 하고, 저장/트랜잭션 경계를 아는 Application Service가 발행한다.
     */
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Course(Long id, Long authorId, String title, String description, CourseStatus status, List<CourseSection> sections) {
        if (authorId == null) {
            throw new DomainRuleViolationException("Author id is required.");
        }
        if (title == null || title.isBlank()) {
            throw new DomainRuleViolationException("Course title is required.");
        }
        if (status == null) {
            throw new DomainRuleViolationException("Course status is required.");
        }
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.sections = new ArrayList<>(sections);
    }

    public static Course create(Long authorId, String title, String description) {
        return new Course(null, authorId, title, description, CourseStatus.DRAFT, List.of());
    }

    public static Course restore(Long id, Long authorId, String title, String description, CourseStatus status, List<CourseSection> sections) {
        return new Course(id, authorId, title, description, status, sections);
    }

    public void addSection(String title, int sectionOrder) {
        ensureDraft();
        boolean duplicatedOrder = sections.stream()
                .anyMatch(section -> section.getSectionOrder() == sectionOrder);
        if (duplicatedOrder) {
            throw new DomainRuleViolationException("Section order must be unique within a course.");
        }
        sections.add(CourseSection.create(title, sectionOrder));
    }

    public void addModule(int sectionOrder, String title, ContentType contentType, int moduleOrder) {
        ensureDraft();
        CourseSection section = sections.stream()
                .filter(candidate -> candidate.hasSectionOrder(sectionOrder))
                .findFirst()
                .orElseThrow(() -> new DomainRuleViolationException("Section not found for order: " + sectionOrder));
        section.addModule(title, contentType, moduleOrder);
    }

    /*
     * publish()는 "강의 공개"라는 도메인 행위다.
     *
     * 이 메서드 안에 있어야 하는 것:
     * - 초안 상태인지 검증
     * - 공개에 필요한 섹션/모듈이 있는지 검증
     * - 상태를 PUBLISHED로 변경
     * - CoursePublishedEvent 기록
     *
     * 이 메서드 안에 있으면 안 되는 것:
     * - DB 저장
     * - HTTP 응답 생성
     * - Spring Event 발행
     * - 알림 발송
     */
    /*
     * occurredAt을 Instant로 받는 이유:
     * - 강의 공개라는 도메인 결과가 발생한 정확한 시점을 기록하기 위해서다.
     * - Course 내부에서 Instant.now()를 직접 호출하지 않으면 테스트에서 시간을 고정할 수 있다.
     * - 현재 시간을 구하는 책임은 Application Service의 Clock이 담당하고,
     *   Course는 전달받은 시각을 Domain Event에 기록하기만 한다.
     */
    public void publish(Instant occurredAt) {
        ensureDraft();
        validatePublishable();
        this.status = CourseStatus.PUBLISHED;
        this.domainEvents.add(new CoursePublishedEvent(this.id, occurredAt));
    }

    private void ensureDraft() {
        if (status != CourseStatus.DRAFT) {
            throw new DomainRuleViolationException("Only draft courses can be modified.");
        }
    }

    private void validatePublishable() {
        if (sections.isEmpty()) {
            throw new DomainRuleViolationException("Course requires at least one section.");
        }
        boolean hasAnyModule = sections.stream().anyMatch(CourseSection::hasModules);
        if (!hasAnyModule) {
            throw new DomainRuleViolationException("Course requires at least one module.");
        }
    }

    /*
     * pullDomainEvents()는 Aggregate가 기록한 이벤트를 Application Service로 넘겨주는 출구다.
     *
     * 이름이 pull인 이유:
     * - Application Service가 저장 후 필요한 시점에 이벤트를 "꺼내 간다".
     * - Aggregate가 외부 발행기를 직접 호출하지 않는다.
     *
     * 왜 꺼낸 뒤 비우는가?
     * - 같은 DomainEvent가 여러 번 발행되면 알림, 통계, 후속 처리도 중복 실행될 수 있다.
     * - 따라서 한 번 꺼낸 이벤트는 내부 목록에서 제거한다.
     *
     * 흐름:
     * Course.publish() -> domainEvents에 CoursePublishedEvent 기록
     * CourseCommandService -> courseRepository.save(course)
     * CourseCommandService -> course.pullDomainEvents()
     * CourseCommandService -> publishAll(events)
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public Long getId() {
        return id;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public List<CourseSection> getSections() {
        return List.copyOf(sections);
    }
}
