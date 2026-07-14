package com.wanted.momocity.study.domain.model;

import java.time.LocalDateTime;

/*
 * comment.
 *  공부 랩(구간) 도메인 역할 -> 순수 비즈니스 데이터만 담당 (JPA 모름)
 *  -
 *  시작/재개 시마다 새 랩이 하나 생기고, 일시정지/종료 시마다 그 랩이 마감된
 *  스톱워치의 랩 기능과 동일한 개념 - "이번 구간에 몇 초 공부했는지"를 개별로 기록
 *  -
 *  솔로/그룹 통합 설계 (테이블은 study_lap 하나) :
 *  - roomId가 null이면 솔로 세션에서 발생한 랩 (sessionId = solo_session.id)
 *  - roomId가 있으면 그룹방에서 발생한 랩 (sessionId = group_room_member.id)
 *  -
 *  endedAt/seconds가 null이면 "아직 진행 중인 랩"을 의미
 * */

public class StudyLap {

    private Long id;
    private Long userId;
    private Long roomId;      // null이면 솔로, 값이 있으면 해당 그룹방
    private Long sessionId;   // roomId가 null이면 solo_session.id, 아니면 group_room_member.id
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer seconds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 신규 생성용 (타이머 시작/재개 시 새 랩 시작)
    public static StudyLap start(Long userId, Long roomId, Long sessionId, LocalDateTime startedAt) {
        StudyLap lap = new StudyLap();
        lap.userId = userId;
        lap.roomId = roomId;
        lap.sessionId = sessionId;
        lap.startedAt = startedAt;
        return lap;
    }

    // DB 복원용
    public static StudyLap reconstitute(
            Long id, Long userId, Long roomId, Long sessionId,
            LocalDateTime startedAt, LocalDateTime endedAt, Integer seconds,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        StudyLap lap = new StudyLap();
        lap.id = id;
        lap.userId = userId;
        lap.roomId = roomId;
        lap.sessionId = sessionId;
        lap.startedAt = startedAt;
        lap.endedAt = endedAt;
        lap.seconds = seconds;
        lap.createdAt = createdAt;
        lap.updatedAt = updatedAt;
        return lap;
    }

    // 랩 마감 (일시정지/종료 시 호출) - seconds는 Service가 startedAt~endedAt 구간을 계산해서 전달
    public void close(LocalDateTime endedAt, int seconds) {
        this.endedAt = endedAt;
        this.seconds = seconds;
    }

    public boolean isOngoing() {
        return this.endedAt == null;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getRoomId() { return roomId; }
    public Long getSessionId() { return sessionId; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public Integer getSeconds() { return seconds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

}
