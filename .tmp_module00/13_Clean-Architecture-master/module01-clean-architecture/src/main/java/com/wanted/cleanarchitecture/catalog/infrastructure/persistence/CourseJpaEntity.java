package com.wanted.cleanarchitecture.catalog.infrastructure.persistence;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/*
 * CourseJpaEntity는 courses 테이블과 매핑되는 JPA 저장 모델이다.
 *
 * JPA Entity 메서드의 기준:
 * - DB에 저장할 상태를 변경하거나 연관관계를 맞춘다.
 * - JPA의 cascade, orphanRemoval, 양방향 연관관계 같은 저장 기술을 다룬다.
 * - 메서드 이름이 changeStatus(), replaceSections()처럼 "저장 상태 변경"에 가깝다.
 *
 * Domain Model 메서드와의 차이:
 * - Course.publish()는 공개 규칙 검증, 상태 변경, DomainEvent 기록을 포함하는 비즈니스 행위다.
 * - CourseJpaEntity.changeStatus()는 status 컬럼 값을 바꾸는 저장 모델 메서드다.
 * - CourseJpaEntity.replaceSections()는 JPA 컬렉션과 FK 연관관계를 맞추기 위한 기술 메서드다.
 *
 * 수업용 정리:
 * JPA Entity의 메서드는 "DB에 어떻게 저장할지"를 돕고,
 * Domain Model의 메서드는 "업무적으로 무엇을 할지"를 표현한다.
 */
@Entity
@Table(name = "courses")
public class CourseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    private String status;

    /*
     * CourseJpaEntity가 Aggregate Root 저장 모델이므로 하위 section을 cascade로 함께 저장한다.
     *
     * orphanRemoval = true:
     * - Course에서 제거된 section은 DB에서도 제거되도록 한다.
     *
     * 주의:
     * - 이 설정은 저장 기술의 세부사항이다.
     * - Course Domain Model은 cascade나 orphanRemoval을 몰라야 한다.
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sectionOrder asc")
    private List<CourseSectionJpaEntity> sections = new ArrayList<>();

    protected CourseJpaEntity() {
    }

    public CourseJpaEntity(Long authorId, String title, String description, String status) {
        this.authorId = authorId;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    /*
     * changeStatus()는 도메인 행위가 아니다.
     *
     * 이 메서드는 Course.publish()가 이미 결정한 결과를 DB 저장 모델에 반영할 뿐이다.
     * 공개 가능 여부 검증은 CourseJpaEntity가 아니라 Course Domain Model이 담당한다.
     */
    public void changeStatus(String status) {
        this.status = status;
    }

    /*
     * replaceSections()는 Course Aggregate의 섹션 상태를 JPA 컬렉션에 동기화하는 저장용 메서드다.
     *
     * section.assignCourse(this)를 호출하는 이유:
     * - JPA 양방향 연관관계에서 자식 Entity가 부모 FK를 알도록 맞춰야 한다.
     * - 이것은 DB 저장을 위한 기술 작업이지, 도메인 규칙이 아니다.
     */
    public void replaceSections(List<CourseSectionJpaEntity> newSections) {
        this.sections.clear();
        for (CourseSectionJpaEntity section : newSections) {
            section.assignCourse(this);
            this.sections.add(section);
        }
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

    public String getStatus() {
        return status;
    }

    public List<CourseSectionJpaEntity> getSections() {
        return sections;
    }

}
