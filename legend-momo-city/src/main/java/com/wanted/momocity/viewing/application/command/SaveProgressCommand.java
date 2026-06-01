package com.wanted.momocity.viewing.application.command;

/*
* comment.
*  진척도 저장에 필요한 값 묶음
*  usrId(토큰) + lectureId(PathVariable) + chapterId(PathVariable) + playbackSeconds(RequestBody)
*  -
*  playbackSeconds : 5-10 초 주기로 받는 현재 재생 위치
*  - watchedSeconds / progressRate 계산에 사용
*  - DB 저장 X
*  -
*  lastPositionSec : 나가기 버튼 클릭 시에만 있는 값
*  - null 이면 저장 안함
*  - null 아니면 DB 저장
* */

public record SaveProgressCommand(
        Long userId,
        Long lectureId,
        Long chapterId,
        int playbackSeconds,
        // nullable
        Integer lastPositionSec
) {
}
