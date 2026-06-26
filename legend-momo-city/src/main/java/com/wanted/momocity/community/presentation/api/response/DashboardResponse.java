package com.wanted.momocity.community.presentation.api.response;

/*
* comment.
*  개인 대시보드 응답 DTO
* */

public record DashboardResponse(
        int totalPostCount,
        int totalViewCount,
        int totalLikeCount,
        int totalCommentCount
//        ,MostViewedPost mostViewedPost
) {

//    public record MostViewedPost(
//            Long postId,
//            String title,
//            int viewCount
//    ) {}

}
