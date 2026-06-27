package com.wanted.momocity.community.presentation.api.request;

import com.wanted.momocity.community.domain.model.PostCategory;
import jakarta.validation.constraints.NotBlank;

/*
* comment.
*  게시글 제목 / 카테고리 수정 요청 DTO
* */

public record UpdatePostRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        String title,

        @NotBlank(message = "카테고리를 입력해주세요.")
        PostCategory category
) {
}
