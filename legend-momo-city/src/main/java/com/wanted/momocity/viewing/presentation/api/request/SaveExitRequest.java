package com.wanted.momocity.viewing.presentation.api.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/*
* comment.
*  강의실 나갈 때 마지막 재생 위치 저장 요청
*  -> 팝업에서 나가기 버튼 클릭 시 호출
*  -
*  playbackSeconds : 마지막으로 보고 있던 재생 위치 (계산용)
*  lastPositionSec : 이어보기 저장 위치 (DB 저장)
* */

public record SaveExitRequest(
        @NotNull(message = "재생 시간 값은 필수 항목입니다.")
        @Min(value = 0, message = "재생 시간은 0 이상이어야 합니다.")
        Integer playbackSeconds,

        @NotNull(message = "마지막 재생 위치는 필수 항목입니다.")
        @Min(value = 0, message = "재생 위치는 0 이상이어야 합니다.")
        Integer lastPositionSec
) {
}
