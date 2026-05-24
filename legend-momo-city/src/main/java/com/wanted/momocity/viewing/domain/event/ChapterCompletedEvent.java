package com.wanted.momocity.viewing.domain.event;

import com.wanted.momocity.global.domain.common.event.DomainEvent;

import java.time.Instant;

/*
* comment.
*  챕터 시청이 완료되었을 때 발행 -> ProgressService 가 받아서 completedCount, totalProgress 업데이트
* */

public record ChapterCompletedEvent (
        Long userId,
        Long lectureId,
        Long chapterId,
        Instant occurredAt
) implements DomainEvent {
}
