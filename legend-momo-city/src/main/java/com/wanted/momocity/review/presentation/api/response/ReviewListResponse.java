package com.wanted.momocity.review.presentation.api.response;

import java.time.LocalDateTime;
import java.util.List;

// 수강평 목록 조회 응답
public record ReviewListResponse(
        // 현재 페이지에 해당하는 수강평 목록
        List<ReviewItemResponse> content,
        int page,
        int size,
        // 전체 강의평 개수
        long totalElements,
        // 전체 페이지 수
        int totalPages
) {
    // 수강평 1개에 들어가는 응답
    public record ReviewItemResponse(
            Long userId, // 수강평 작성자 ID
            String nickname, // 수강평 작성자 닉네임
            Long reviewId, // 수강평 ID
            LocalDateTime createdAt, // 수강평 작성 시간
            String content, // 수강평 작성 내용
            int rating, // 수강평 별점
            String profileImageUrl // 수강평 작성자 프로필 이미지 URL
    ) {}
}
