package com.wanted.momocity.review.application.query;

// 강의평 목록 조회 클래스
public class ReviewQuery {

    private ReviewQuery() {
    }

    // 수강평 목록 조회 요청 레코드
    public record GetReviewListQuery(
            // 수강평 조회 할 강의 id
            Long lectureId,
            // 조회할 페이지 변호
            int page,
            // 한 페이지에 조회할 수강평 개수
            int  size
    ) {

    }
}
