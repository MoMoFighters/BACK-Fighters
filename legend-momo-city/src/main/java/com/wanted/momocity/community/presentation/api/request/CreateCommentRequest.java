package com.wanted.momocity.community.presentation.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/*
* comment.
*  댓글 / 대댓글 작성 요청 DTO
* */

public record CreateCommentRequest(
        @NotNull(message = "댓글 내용을 입력해주세요.")
        @Size(max = 500, message = "댓글은 500자 이하로 입력해주세요.")
        String content
) {
}
