package com.wanted.momocity.study.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * comment.
 *  그룹방 제목 수정 요청 DTO
 *  - 사용 API : PATCH /api/v3/study/rooms/{roomId}
 * */

public record UpdateRoomTitleRequest(
        @NotBlank(message = "방 제목을 입력해주세요.")
        @Size(max = 50, message = "방 제목은 50자 이하로 입력해주세요.")
        String title
) {
}
