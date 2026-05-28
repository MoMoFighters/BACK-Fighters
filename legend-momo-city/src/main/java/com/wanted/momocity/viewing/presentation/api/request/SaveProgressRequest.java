package com.wanted.momocity.viewing.presentation.api.request;

/*
* comment.
*  진척도 저장 API 의 RequestBody
*  프론트가 5-10 초 주기로 현재 재생 위치를 전송
*  record 라서 불변객체
*  -
*  playbackSeconds : 0 이상 필수값
* */

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SaveProgressRequest(
        @NotNull(message = "재생 시간 값은 필수 항목입니다.")
        @Min(value = 0, message = "재생 시간은 0 이상이어야 합니다.")
        // 현재 재생 위치
        Integer playbackSeconds
) {
}
