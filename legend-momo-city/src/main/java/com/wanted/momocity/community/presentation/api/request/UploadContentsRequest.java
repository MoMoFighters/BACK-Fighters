package com.wanted.momocity.community.presentation.api.request;

import com.wanted.momocity.community.application.command.PostContentCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/*
* comment.
*  콘텐츠 업로드 / 수정 요청 DTO
*  -> POST /api/v2/posts/{postId}/contents
*  -> PUT /api/v2/posts/{postId}/contents
*  - Controller 에서 PostContentCommand 로 변환 후 UseCase 에 전달
* */

public record UploadContentsRequest(
        @NotEmpty(message = "콘텐츠를 1개 이상 입력해주세요.")
        @Valid
        List<ContentItem> contents
) {

    /*
    * comment.
    *  ContentItem
    *  콘텐츠 단건 요청 DTO
    *  Command 와 분리하여 presentation 계층 전용으로 사용
    *  - ContentItem : presentation 계층 전용 (HTTP 요청 직렬화)
    *  - PostContentCommand : application 계층 전용 (UseCase 입력값)
    *  - Controller 에서 ContentItem -> PostContentCommand 변환 담당
    * */

    public record ContentItem(
            @NotBlank(message = "타입을 입력해주세요.")
            String type,

            String content,
            String imageUrl
    ) {}

}
