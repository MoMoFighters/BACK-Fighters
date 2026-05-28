package com.wanted.cleanarchitecture.catalog.infrastructure.persistence;

import jakarta.persistence.*;

/*
 * ContentModuleJpaEntity는 content_modules 테이블과 매핑되는 JPA 저장 모델이다.
 *
 * JPA 프록시 관점:
 * - section은 @ManyToOne(fetch = LAZY)이므로 ContentModuleJpaEntity를 읽는 순간
 *   CourseSectionJpaEntity 전체가 즉시 로딩되지 않을 수 있다.
 * - JPA는 section 자리에 프록시 객체를 넣어두고, 실제 접근 시점에 DB 조회를 시도한다.
 *
 * Clean Architecture 관점:
 * - 이 프록시 동작은 Infrastructure 세부사항이다.
 * - Domain Model인 ContentModule은 프록시, 영속성 컨텍스트, Lazy Loading을 몰라야 한다.
 * - 변환 책임은 CourseRepositoryAdapter가 가진다.
 *
 * JPA Entity 메서드의 기준:
 * - changeTitle(), changeContentType(), changeModuleOrder()는 컬럼 값을 저장 상태에 맞춘다.
 * - assignSection()은 section_id FK를 맞추기 위한 기술 메서드다.
 *
 * Domain Model 메서드와의 차이:
 * - ContentModule.hasOrder()는 도메인 규칙 판단에 쓰이는 질문 메서드다.
 * - ContentModuleJpaEntity.assignSection()은 DB 연관관계를 맞추는 저장용 메서드다.
 */
@Entity
@Table(
        name = "content_modules",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_content_modules_section_order",
                columnNames = {"section_id", "module_order"}
        )
)
public class ContentModuleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "module_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSectionJpaEntity section;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "module_order", nullable = false)
    private int moduleOrder;

    @Column(name = "content_body")
    private String contentBody;

    protected ContentModuleJpaEntity() {
    }

    public ContentModuleJpaEntity(String title, String contentType, int moduleOrder) {
        this.title = title;
        this.contentType = contentType;
        this.moduleOrder = moduleOrder;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeContentType(String contentType) {
        this.contentType = contentType;
    }

    public void changeModuleOrder(int moduleOrder) {
        this.moduleOrder = moduleOrder;
    }

    /*
     * assignSection()은 JPA 연관관계와 FK 값을 맞추기 위한 메서드다.
     * "어떤 섹션에 모듈을 추가할 수 있는가"는 Domain Model에서 판단한다.
     */
    void assignSection(CourseSectionJpaEntity section) {
        this.section = section;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContentType() {
        return contentType;
    }

    public int getModuleOrder() {
        return moduleOrder;
    }
}
