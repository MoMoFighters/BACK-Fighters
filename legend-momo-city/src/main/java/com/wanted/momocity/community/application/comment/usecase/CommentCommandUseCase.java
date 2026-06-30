package com.wanted.momocity.community.application.comment.usecase;

/*
* comment.
*  댓글 / 대댓글 쓰기 작업 UseCase 인터페이스
*  -> 댓글 작성 / 삭제, 대댓글 작성 / 삭제
* */

public interface CommentCommandUseCase {

    // 댓글 작성
    void createComment(Long userId, Long postId, String content);

    // 댓글 삭제
    void deleteComment(Long userId, Long postId, Long commentId);

    // 대댓글 작성
    void createReply(Long userId, Long postId, Long commentId, String content);

    // 대댓글 삭제
    void deleteReply(Long userId, Long postId, Long commentId, Long replyId);

}
