package com.wanted.cleanarchitecture.catalog.domain.model;

import com.wanted.cleanarchitecture.global.domain.common.exception.DomainRuleViolationException;

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
