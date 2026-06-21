package com.wanted.momocity.community.domain.exception;

/*
* comment.
*  Community 컨텍스트 전용 403 예외
*  - 본인 게시글 / 댓글이 아닐 때 발생
* */

public class CommunityAccessDeniedException extends RuntimeException {
    public CommunityAccessDeniedException(String message) {
        super(message);
    }
}
