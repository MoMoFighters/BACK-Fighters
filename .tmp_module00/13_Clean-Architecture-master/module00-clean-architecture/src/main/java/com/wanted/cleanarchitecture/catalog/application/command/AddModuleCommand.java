package com.wanted.cleanarchitecture.catalog.application.command;

import com.wanted.cleanarchitecture.catalog.domain.model.ContentType;

/*
 * AddModuleCommand는 특정 Course 내부 섹션에 모듈을 추가하라는 Command다.
 * 여기서 sectionOrder를 사용하는 이유는 domain model이 DB PK보다 도메인 의미가 있는 순서를 기준으로
 * 내부 구성요소를 찾도록 만들기 위해서다.
 */
public record AddModuleCommand(
        Long courseId,
        int sectionOrder,
        String title,
        ContentType contentType,
        int moduleOrder
) {
}
