package com.wanted.cleanarchitecture.enrollment.application.port;

/*
 * CoursePublicationStatus는 enrollment 컨텍스트가 catalog에서 받아오는 작은 응답 모델이다.
 *
 * Course의 제목, 설명, 섹션, 모듈 구조는 수강 신청 규칙에 필요하지 않다.
 * 이렇게 필요한 값만 담으면 Bounded Context 사이의 결합을 줄일 수 있다.
 */
public record CoursePublicationStatus(
        Long courseId,
        boolean published
) {
}
