package com.wanted.cleanarchitecture.catalog.application.usecase;

public interface CourseQueryUseCase {

    // 강의 상세 조회 유스케이스
    CourseView handle(Long courseId);

    // 강의 상세 조회 시 응답 사용 객체
    record CourseView(
            Long courseId,
            Long authorId,
            String title,
            String description,
            String status,
            int sectionCount,
            int moduleCount
    ) {}

}
