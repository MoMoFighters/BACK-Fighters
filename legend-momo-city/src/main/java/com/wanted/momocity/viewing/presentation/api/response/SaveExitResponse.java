package com.wanted.momocity.viewing.presentation.api.response;

/*
* comment.
*  강의실 나갈 때 마지막 재생 위치 저장 응답
*  -> 저장된 lastPositionSec 반환
* */

public record SaveExitResponse(
        Long chapterId,
        int lastPositionSec
) {
}
