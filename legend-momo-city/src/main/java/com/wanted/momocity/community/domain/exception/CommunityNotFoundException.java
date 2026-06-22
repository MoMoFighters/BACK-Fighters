package com.wanted.momocity.community.domain.exception;

/*
* comment.
*  Community 컨텍스트 전용 404 예외
*  - 게시글, 댓글, 대댓글 없을 때 발생
* */

public class CommunityNotFoundException extends RuntimeException {
    public CommunityNotFoundException(String message) {
        super(message);
    }
}
