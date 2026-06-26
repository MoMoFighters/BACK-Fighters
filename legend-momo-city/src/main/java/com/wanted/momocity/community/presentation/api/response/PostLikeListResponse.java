package com.wanted.momocity.community.presentation.api.response;

import java.util.List;

/*
* comment.
*  좋아요 누른 사용자 목록 응답 DTO
*  userId 포함 -> 클릭 시 해당 사용자 페이지로 이동
* */

public record PostLikeListResponse(
        int totalCount,
        List<LikeUserItem> users
) {
    public record LikeUserItem(
            Long userId,
            String userName,
            String profileImageUrl,
            String role
    ) {}
}
