package com.wanted.cleanarchitecture.catalog.domain.model;

import com.wanted.cleanarchitecture.global.domain.common.exception.DomainRuleViolationException;

import java.util.ArrayList;
import java.util.List;

public class CourseSection {
    private final Long id;
    private final String title;
    private final int sectionOrder;
    private final List<ContentModule> modules;

    private CourseSection(Long id, String title, int sectionOrder, List<ContentModule> modules) {
        if (title == null || title.isBlank()) {
            throw new DomainRuleViolationException("Section title is required.");
        }
        this.id = id;
        this.title = title;
        this.sectionOrder = sectionOrder;
        this.modules = new ArrayList<>(modules);
    }

    public static CourseSection create(String title, int sectionOrder) {
        return new CourseSection(null, title, sectionOrder, List.of());
    }

    /*
     * restore()는 Repository Adapter가 DB에서 읽은 값을 도메인 객체로 복원할 때 사용한다.
     * 일반 UseCase에서 새 섹션을 만들 때는 create()나 Course.addSection()을 사용한다.
     */
    public static CourseSection restore(Long id, String title, int sectionOrder, List<ContentModule> modules) {
        return new CourseSection(id, title, sectionOrder, modules);
    }

    public void addModule(String title, ContentType contentType, int moduleOrder) {
        boolean duplicatedOrder = modules.stream()
                .anyMatch(module -> module.hasOrder(moduleOrder));
        if (duplicatedOrder) {
            throw new DomainRuleViolationException("Module order must be unique within a section.");
        }
        modules.add(ContentModule.create(title, contentType, moduleOrder));
    }

    public boolean hasSectionOrder(int sectionOrder) {
        return this.sectionOrder == sectionOrder;
    }

    public boolean hasModules() {
        return !modules.isEmpty();
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

    public List<ContentModule> getModules() {
        return List.copyOf(modules);
    }
}
