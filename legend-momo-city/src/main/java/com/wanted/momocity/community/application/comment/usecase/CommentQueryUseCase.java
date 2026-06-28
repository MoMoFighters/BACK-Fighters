package com.wanted.momocity.community.application.comment.usecase;

import com.wanted.momocity.community.presentation.api.response.PostCommentResponse;
import com.wanted.momocity.community.presentation.api.response.PostReplyResponse;

/*
* comment.
*  댓글 / 대댓글 읽기 작업 UseCase 인터페이스
*  -> 댓글 목록 조회, 대댓글 목록 조회
* */

public interface CommentQueryUseCase {

    // 게시글 댓글 목록 조회 (커서 기반 페이지네이션, 대댓글 포함)
    PostCommentResponse getComments(Long userId, Long postId, Long cursor, int size);

    // 게시글 대댓글 목록 조회 (커서 기반 페이지네이션)
    PostReplyResponse getReplies(Long userId, Long postId, Long commentId, Long cursor, int size);


}
