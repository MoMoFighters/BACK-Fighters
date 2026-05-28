package com.wanted.cleanarchitecture.catalog.application.command;

/*
 * CreateCourseCommand는 이벤트스토밍의 Command에 해당한다.
 * 학생 입장에서는 "강의를 생성해 달라"는 사용자 의도를 담는 요청 모델로 보면 된다.
 * Clean Architecture에서는 presentation이 받은 입력을 application으로 넘길 때 사용하는 경계 객체다.
 * domain model을 직접 노출하지 않고, 유스케이스가 필요한 값만 고정된 형태로 전달한다.
 */
public record CreateCourseCommand(
        Long authorId,
        String title,
        String description
) {
}
