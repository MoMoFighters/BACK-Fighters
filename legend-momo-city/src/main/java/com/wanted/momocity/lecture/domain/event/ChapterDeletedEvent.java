package com.wanted.momocity.lecture.domain.event;

import com.wanted.momocity.global.domain.common.event.DomainEvent;

import java.time.Instant;

public record ChapterDeletedEvent(
        Long lectureId,
        Long chapterId,
        Instant occurredAt
) implements DomainEvent {
}
