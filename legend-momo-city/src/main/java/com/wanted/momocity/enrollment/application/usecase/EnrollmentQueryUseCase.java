package com.wanted.momocity.enrollment.application.usecase;

import com.wanted.momocity.enrollment.application.query.EnrollmentQuery;

public interface EnrollmentQueryUseCase {
    // 강의 진척도 조회 기능을 수행하는 메서드
    EnrollmentProgressResponse getProgress(
            // Controller에서 넘어온 userId, category 값을 담은 Query 객체
            EnrollmentQuery.GetEnrollmentProgressQuery query
    );
}
