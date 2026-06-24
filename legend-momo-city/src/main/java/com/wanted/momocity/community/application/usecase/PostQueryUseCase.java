package com.wanted.momocity.community.application.usecase;

import com.wanted.momocity.community.presentation.api.response.PostCommentResponse;
import com.wanted.momocity.community.presentation.api.response.PostDetailResponse;
import com.wanted.momocity.community.presentation.api.response.PostListResponse;

public interface PostQueryUseCase {

    // 게시글 목록 조회 (카테고리 필터링, 페이지네이션)
    PostListResponse getPosts(Long userId, String category, int page, int size);

    // 게시글 단건 조회 (contents 미포함)
    PostDetailResponse getPost(Long userId, Long PostId);

    // 게시글 댓글 목록 조회 (커서 기반 페이지네이션)
    PostCommentResponse getComments(Long userId, Long postId, Long cursor, int size);

}
