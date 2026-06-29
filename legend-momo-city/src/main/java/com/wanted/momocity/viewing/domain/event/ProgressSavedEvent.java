package com.wanted.momocity.viewing.domain.event;

import java.time.LocalDate;

/*
* comment.
*  진척도 저장 이벤트
*  - 발행 시점
*  saveProgress() 호출 시마다 발행
*  -> 미완료 챕터 : hasMeaningfulProgress = true
*  -> 완료 챕터 : 재시청 시 playbackSeconds 기준
*  - 잔디 누적용
*  ChapterCompletedEvent 와 상관없이 시청하면 잔디 누적
* */

public record ProgressSavedEvent(
        Long userId,
        Long lectureId,
        Long chapterId,
        int watchedSeconds,
        LocalDate date
) {
}
