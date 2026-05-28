package com.wanted.cleanarchitecture.catalog.domain.model;

import com.wanted.cleanarchitecture.global.domain.common.exception.DomainRuleViolationException;

/*
 * ContentModule은 CourseSection 내부에 포함되는 도메인 Entity다.
 *
 * 도메인 클래스의 메서드 기준:
 * - 업무 규칙을 표현하거나 업무 판단에 필요한 질문을 제공한다.
 * - JPA 매핑, FK 연결, 프록시 초기화 같은 저장 기술을 다루지 않는다.
 *
 * 예:
 * - hasOrder(): 같은 섹션 안에서 모듈 순서가 중복되는지 판단할 때 사용한다.
 *
 * 반대로 ContentModuleJpaEntity의 changeTitle(), assignSection() 같은 메서드는
 * DB 저장 상태를 맞추기 위한 기술적 변경 메서드다.
 */
public class ContentModule {

    private final Long id;
    private final String title;
    private final ContentType contentType;
    private final int moduleOrder;

    private ContentModule(Long id, String title, ContentType contentType, int moduleOrder) {
        if (title == null || title.isBlank()) {
            throw new DomainRuleViolationException("Module title is required.");
        }
        this.id = id;
        this.title = title;
        this.contentType = contentType;
        this.moduleOrder = moduleOrder;
    }

    public static ContentModule create(String title, ContentType contentType, int moduleOrder) {
        return new ContentModule(null, title, contentType, moduleOrder);
    }

    /*
     * restore()는 저장된 데이터를 도메인 객체로 되살릴 때 사용하는 복원 메서드다.
     * 새 모듈을 추가하는 유스케이스에서는 create()를 사용한다.
     */
    public static ContentModule restore(Long id, String title, ContentType contentType, int moduleOrder) {
        return new ContentModule(id, title, contentType, moduleOrder);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public int getModuleOrder() {
        return moduleOrder;
    }

    public boolean hasOrder(int moduleOrder) {
        return this.moduleOrder == moduleOrder;
    }
}
