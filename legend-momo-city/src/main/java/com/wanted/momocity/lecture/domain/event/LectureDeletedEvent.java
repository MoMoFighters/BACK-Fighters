package com.wanted.momocity.lecture.domain.event;

import com.wanted.momocity.global.domain.common.event.DomainEvent;

import java.time.Instant;
import java.util.List;

// 강의 트렌젝션이 완료 후 후속 작업을 요청하는 이벤트
public record LectureDeletedEvent(
        Long lectureId,
        List<Long> chapterIds,
        Instant occurredAt
) implements DomainEvent {
    // 이벤트 생성 시 챕터 ID 목록을 안전한 불변 리스트로 보관
    public LectureDeletedEvent {
        // null이 전달되면 빈 리스트를 사용하고, 값이 있으면 외부에서 수정할 수 없도록 복사
        chapterIds = chapterIds == null ? List.of() : List.copyOf(chapterIds);
    }
}
