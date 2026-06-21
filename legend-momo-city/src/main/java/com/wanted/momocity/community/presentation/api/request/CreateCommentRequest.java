package com.wanted.momocity.community.presentation.api.request;

import jakarta.validation.constraints.NotBlank;

/*
* comment.
*  댓글 / 대댓글 작성 요청 DTO
* */

public record CreateCommentRequest(
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        String content
) {
}
