package com.wanted.cleanarchitecture.enrollment.application.command;

/*
 * EnrollCourseCommand는 "수강 신청" 유스케이스의 입력 모델이다.
 * 이벤트스토밍 기준으로는 수강 신청 Command이며, controller와 application 사이의 경계 객체다.
 */
public record EnrollCourseCommand(
        Long userId,
        Long courseId
) {
}
