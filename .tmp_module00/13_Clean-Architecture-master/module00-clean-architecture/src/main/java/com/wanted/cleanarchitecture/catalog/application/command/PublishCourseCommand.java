package com.wanted.cleanarchitecture.catalog.application.command;

/*
 * PublishCourseCommand는 draft 상태의 Course를 공개 상태로 전이시키는 Command다.
 * 이벤트스토밍에서는 "강의 공개" 요청에 대응하며, application 계층의 쓰기 모델 역할을 한다.
 */
public record PublishCourseCommand(
        Long courseId
) {
}
