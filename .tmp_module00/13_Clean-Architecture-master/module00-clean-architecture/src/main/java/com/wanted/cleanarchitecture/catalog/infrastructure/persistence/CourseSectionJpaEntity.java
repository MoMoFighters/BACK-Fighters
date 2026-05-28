package com.wanted.cleanarchitecture.catalog.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;

/*
 * CourseSectionJpaEntity는 course_sections 테이블과 매핑되는 JPA 저장 모델이다.
 *
 * JPA 프록시 관점:
 * - course는 @ManyToOne(fetch = LAZY)이므로 실제 CourseJpaEntity가 아니라 프록시일 수 있다.
 * - 프록시는 필요한 순간 DB에서 값을 가져오기 위해 Hibernate가 만든 대리 객체다.
 * - 영속성 컨텍스트가 닫힌 뒤 프록시를 접근하면 LazyInitializationException이 발생할 수 있다.
 *
 * 그래서 이 Entity는 Infrastructure 계층 안에서만 사용하고,
 * CourseRepositoryAdapter가 CourseSection Domain Model로 변환한 뒤 바깥 계층에 넘긴다.
 *
 * JPA Entity 메서드의 기준:
 * - changeTitle(), changeSectionOrder()는 DB 컬럼 값을 맞추는 저장용 메서드다.
 * - assignCourse()는 FK 연관관계를 맞추는 기술 메서드다.
 * - replaceModules()는 JPA 자식 컬렉션을 동기화하는 저장용 메서드다.
 *
 * Domain Model 메서드와의 차이:
 * - CourseSection.addModule()은 모듈 순서 중복을 검증하는 도메인 행위다.
 * - CourseSectionJpaEntity.replaceModules()는 이미 결정된 도메인 상태를 DB 저장 모델에 반영하는 작업이다.
 */
@Entity
@Table(
        name = "course_sections",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_course_sections_course_order",
                columnNames = {"course_id", "section_order"}
        )
)
public class CourseSectionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long id;

    /*
     * LAZY를 사용하는 이유:
     * - section을 조회할 때 항상 course 전체가 필요한 것은 아니다.
     * - 필요한 순간에만 로딩해 성능 비용을 줄일 수 있다.
     *
     * 단점:
     * - 프록시가 생기므로 트랜잭션 밖에서 접근하면 문제가 생길 수 있다.
     * - 따라서 Adapter 안에서 필요한 데이터를 도메인 객체로 변환해 내보낸다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseJpaEntity course;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "section_order", nullable = false)
    private int sectionOrder;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("moduleOrder asc")
    private List<ContentModuleJpaEntity> modules = new ArrayList<>();

    protected CourseSectionJpaEntity() {
    }

    public CourseSectionJpaEntity(String title, int sectionOrder) {
        this.title = title;
        this.sectionOrder = sectionOrder;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeSectionOrder(int sectionOrder) {
        this.sectionOrder = sectionOrder;
    }

    /*
     * assignCourse()는 JPA FK를 맞추기 위한 메서드다.
     * "섹션을 강의에 추가한다"는 도메인 행위는 Course.addSection()에서 표현한다.
     */
    void assignCourse(CourseJpaEntity course) {
        this.course = course;
    }

    /*
     * replaceModules()는 JPA 컬렉션 동기화 메서드다.
     * 모듈 추가 가능 여부나 순서 중복 검증은 Domain Model인 CourseSection.addModule()에서 처리한다.
     */
    public void replaceModules(List<ContentModuleJpaEntity> newModules) {
        this.modules.clear();
        for (ContentModuleJpaEntity module : newModules) {
            module.assignSection(this);
            this.modules.add(module);
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getSectionOrder() {
        return sectionOrder;
    }

    public List<ContentModuleJpaEntity> getModules() {
        return modules;
    }
}
