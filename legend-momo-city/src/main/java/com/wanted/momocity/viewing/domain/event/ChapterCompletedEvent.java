package com.wanted.momocity.viewing.domain.event;

import com.wanted.momocity.global.domain.common.event.DomainEvent;

import java.time.Instant;

/*
* comment.
*  챕터 시청이 완료되었을 때 발행 -> ProgressService 가 받아서 completedCount, totalProgress 업데이트
*  - command 와의 차이
*  -- SaveProgressCommand : 진척도를 저장해달라는 요청
*  -- ChapterCompletedEvent : 챕터가 실제로 완료되었다는 결과
* */

public record ChapterCompletedEvent (
        Long userId,
        Long lectureId,
        Long chapterId,
        Instant occurredAt
) implements DomainEvent {
}
