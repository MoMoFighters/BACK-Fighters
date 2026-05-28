package com.wanted.cleanarchitecture.catalog.infrastructure.persistence;

import com.wanted.cleanarchitecture.catalog.domain.model.*;
import com.wanted.cleanarchitecture.catalog.domain.repository.CourseRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * CourseRepositoryAdapter는 Domain 계층의 CourseRepository Port를 JPA로 구현하는 Adapter다.
 *
 * 계층별 호출 흐름:
 * CourseCommandService
 *   -> CourseRepository Port
 *      -> CourseRepositoryAdapter
 *         -> SpringDataCourseRepository
 *            -> CourseJpaEntity
 *
 * 이 Adapter의 핵심 책임:
 * - Domain Model(Course)과 JPA Entity(CourseJpaEntity)를 서로 변환한다.
 * - Application/Domain 계층이 JPA 프록시, Lazy Loading, 영속성 컨텍스트를 몰라도 되게 숨긴다.
 * - DB 저장 구조가 바뀌어도 Course Aggregate의 비즈니스 규칙을 최대한 보호한다.
 *
 * JPA 프록시 관련 주의:
 * - @ManyToOne(fetch = LAZY), @OneToMany 같은 연관관계는 실제 객체 대신 프록시나 지연 로딩 컬렉션일 수 있다.
 * - 트랜잭션이 끝난 뒤 프록시를 건드리면 LazyInitializationException이 발생할 수 있다.
 * - 그래서 Adapter 안의 트랜잭션 범위에서 필요한 연관 데이터를 초기화하고 순수 Domain Model로 변환한다.
 * - Controller나 Application Service로 JPA Entity를 그대로 반환하지 않는다.
 */
@Repository
@Transactional
public class CourseRepositoryAdapter implements CourseRepository {

    private final SpringDataCourseRepository repository;

    public CourseRepositoryAdapter(SpringDataCourseRepository repository) {
        this.repository = repository;
    }

    @Override
    public Course save(Course course) {
        /*
         * 저장 흐름:
         * 1. 새 Course면 CourseJpaEntity를 새로 만든다.
         * 2. 기존 Course면 영속 상태의 CourseJpaEntity를 조회한다.
         * 3. Domain Model의 상태를 JPA Entity에 반영한다.
         * 4. 저장된 JPA Entity를 다시 Domain Model로 변환해 반환한다.
         *
         * Application Service는 이 내부 과정을 모르고 CourseRepository Port만 사용한다.
         */
        CourseJpaEntity entity = course.getId() == null
                ? new CourseJpaEntity(course.getAuthorId(), course.getTitle(), course.getDescription(), course.getStatus().name().toLowerCase())
                : repository.findById(course.getId()).orElseThrow();

        entity.changeTitle(course.getTitle());
        entity.changeDescription(course.getDescription());
        entity.changeStatus(course.getStatus().name().toLowerCase());
        entity.replaceSections(syncSections(entity, course));

        CourseJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Course> findById(Long courseId) {
        return repository.findById(courseId).map(this::toDomain);
    }

    private List<CourseSectionJpaEntity> syncSections(CourseJpaEntity entity, Course course) {
        /*
         * Course Aggregate 내부 컬렉션 변경을 JPA Entity 컬렉션에 반영한다.
         *
         * 이 예제는 수업 이해를 위해 "도메인 상태를 저장 모델에 맞춰 동기화한다"는 흐름을 보여준다.
         * 실무에서 컬렉션 크기가 커지면 변경분만 반영하는 방식도 검토할 수 있다.
         */
        Map<Long, CourseSectionJpaEntity> existingById = entity.getSections().stream()
                .filter(section -> section.getId() != null)
                .collect(Collectors.toMap(CourseSectionJpaEntity::getId, Function.identity()));

        return course.getSections().stream()
                .map(section -> {
                    CourseSectionJpaEntity sectionEntity = section.getId() != null
                            ? existingById.getOrDefault(section.getId(), new CourseSectionJpaEntity(section.getTitle(), section.getSectionOrder()))
                            : new CourseSectionJpaEntity(section.getTitle(), section.getSectionOrder());
                    sectionEntity.changeTitle(section.getTitle());
                    sectionEntity.changeSectionOrder(section.getSectionOrder());
                    sectionEntity.replaceModules(syncModules(sectionEntity, section));
                    return sectionEntity;
                })
                .toList();
    }

    private List<ContentModuleJpaEntity> syncModules(CourseSectionJpaEntity sectionEntity, CourseSection section) {
        Map<Long, ContentModuleJpaEntity> existingById = sectionEntity.getModules().stream()
                .filter(module -> module.getId() != null)
                .collect(Collectors.toMap(ContentModuleJpaEntity::getId, Function.identity()));

        return section.getModules().stream()
                .map(module -> {
                    ContentModuleJpaEntity moduleEntity = module.getId() != null
                            ? existingById.getOrDefault(module.getId(), new ContentModuleJpaEntity(
                            module.getTitle(),
                            module.getContentType().name().toLowerCase(),
                            module.getModuleOrder()))
                            : new ContentModuleJpaEntity(
                            module.getTitle(),
                            module.getContentType().name().toLowerCase(),
                            module.getModuleOrder());
                    moduleEntity.changeTitle(module.getTitle());
                    moduleEntity.changeContentType(module.getContentType().name().toLowerCase());
                    moduleEntity.changeModuleOrder(module.getModuleOrder());
                    return moduleEntity;
                })
                .toList();
    }

    private Course toDomain(CourseJpaEntity entity) {
        /*
         * JPA Entity를 순수 Domain Model로 변환한다.
         *
         * 왜 Hibernate.initialize()를 호출하는가?
         * - entity.getSections()는 JPA가 관리하는 지연 로딩 컬렉션일 수 있다.
         * - section.getModules()도 지연 로딩 컬렉션일 수 있다.
         * - 이 Adapter를 벗어난 뒤에는 트랜잭션/영속성 컨텍스트가 닫힐 수 있다.
         * - 닫힌 뒤 프록시를 접근하면 LazyInitializationException이 발생한다.
         *
         * 그래서 Adapter 내부에서 필요한 연관관계를 초기화한 뒤,
         * Application/Domain 계층에는 JPA 프록시가 아닌 순수 Course 객체만 넘긴다.
         *
         * 수업용 정리:
         * JPA 프록시는 Infrastructure 세부사항이다.
         * Adapter 밖으로 프록시를 내보내지 않는 것이 Clean Architecture 관점에서 안전하다.
         */
        Hibernate.initialize(entity.getSections());
        entity.getSections().forEach(section -> Hibernate.initialize(section.getModules()));

        List<CourseSection> sections = entity.getSections().stream()
                .map(section -> CourseSection.restore(
                        section.getId(),
                        section.getTitle(),
                        section.getSectionOrder(),
                        section.getModules().stream()
                                .map(module -> ContentModule.restore(
                                        module.getId(),
                                        module.getTitle(),
                                        ContentType.valueOf(module.getContentType().toUpperCase()),
                                        module.getModuleOrder()
                                ))
                                .toList()
                ))
                .toList();

        return Course.restore(
                entity.getId(),
                entity.getAuthorId(),
                entity.getTitle(),
                entity.getDescription(),
                CourseStatus.valueOf(entity.getStatus().toUpperCase()),
                sections
        );
    }

}
