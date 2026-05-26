package com.wanted.momocity.teacher.presentation.api.response;

import java.time.LocalDateTime;
import java.util.List;

/*
 * MS-3: 강사 신청자 목록 응답.
 *
 * 강사 예제의 inner record 패턴(CourseQueryUseCase.CourseView) 따라
 * Item 을 nested record 로 정의한다.
 */
public record TeacherApplicationListResponse(
        List<Item> applications,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record Item(
            Long userId,
            String nickname,
            String name,
            String email,
            String category,
            LocalDateTime appliedAt
    ) {
    }
}
