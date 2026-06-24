package com.wanted.momocity.enrollment.application.query;

// EnrollmentQuery는 enrollment 조회 요청 record들을 묶어두는 컨테이너 클래스이다.
public final class EnrollmentQuery {

    // 생성자
    private EnrollmentQuery() {
    }

    public record GetEnrollmentProgressQuery(
            // 로그인 한 사용자Id
            Long userId,
            // 조회할 카테고리, 없으면 null
            String category
    ) {}
}
