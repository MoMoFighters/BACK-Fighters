package com.wanted.cleanarchitecture.catalog.application.command;

/*
 * AddSectionCommand는 Course Aggregate에 섹션 추가를 요청하는 Command다.
 * 이벤트스토밍 기준으로는 "섹션 추가"라는 쓰기 요청을 표현한다.
 * application 계층은 이 객체를 받아 유스케이스를 실행하고, domain은 이 값으로 규칙을 검증한다.
 */
public record AddSectionCommand(
        Long courseId,
        String title,
        int sectionOrder
) {
}
