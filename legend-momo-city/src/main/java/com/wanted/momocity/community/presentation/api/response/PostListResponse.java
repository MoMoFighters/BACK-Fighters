package com.wanted.momocity.community.presentation.api.response;

import java.time.LocalDateTime;
import java.util.List;

/*
* comment.
*  게시글 목록 조회 응답 DTO
*  - posts : 게시글 목록
*  - totalElements : 전체 게시글 수
*  - totalPages : 전체 페이지 수
*  - currentPage : 현재 페이지 수
* */

public record PostListResponse (
        List<PostItem> posts,
        long totalElements,
        int totalPages,
        int currentPage
) {

    /*
    * comment.
    *  PostItem : 게시글 목록 단건 응답 DTO
    *  - 목록에서는 contents 미포함
    * */

    public record PostItem(
            Long postId,
            String title,
            String category,
            int viewCount,
            int likeCount,
            int commentCount,
            String thumbnailUrl,
            Long authorId,
            String authorName,
            String authorProfileImageUrl,
            String authorRole,
            LocalDateTime createdAt
    ) {}

}
