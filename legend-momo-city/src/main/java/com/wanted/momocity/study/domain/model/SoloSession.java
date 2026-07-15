package com.wanted.momocity.study.domain.model;

import java.time.LocalDateTime;

/*
 * comment.
 *  혼자 공부 세션(열품타 - 솔로) 도메인 역할 -> 순수 비즈니스 데이터만 담당 (JPA 모름)
 *  -
 *  생명주기 : RUNNING -> PAUSED -> RUNNING (반복) -> ENDED
 *  - 상태 전이 검증/예외는 Service(SoloCommandService)가 담당
 *  - 최대 24시간 제한 검증도 Service가 담당 (도메인은 값만 들고 있음)
 *  -
 *  시간 계산
 *  - lastResumedAt : 마지막으로 RUNNING이 된 시각. PAUSED/ENDED 상태에서는 null
 *  - totalSeconds : 지금까지 확정된 누적 공부 시간(초). pause/end 시점에 Service가 계산해서 갱신
 * */

public class SoloSession {

    private Long id;
    private Long userId;
    private SoloSessionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalSeconds;
    private LocalDateTime lastResumedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 신규 생성용 (타이머 최초 시작)
    public static SoloSession create(Long userId, LocalDateTime startTime) {
        SoloSession session = new SoloSession();
        session.userId = userId;
        session.status = SoloSessionStatus.RUNNING;
        session.startTime = startTime;
        session.lastResumedAt = startTime;
        session.totalSeconds = 0;
        return session;
    }

    // DB 복원용
    public static SoloSession reconstitute(
            Long id, Long userId, SoloSessionStatus status,
            LocalDateTime startTime, LocalDateTime endTime, int totalSeconds,
            LocalDateTime lastResumedAt, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        SoloSession session = new SoloSession();
        session.id = id;
        session.userId = userId;
        session.status = status;
        session.startTime = startTime;
        session.endTime = endTime;
        session.totalSeconds = totalSeconds;
        session.lastResumedAt = lastResumedAt;
        session.createdAt = createdAt;
        session.updatedAt = updatedAt;
        return session;
    }

    // 재개 (PAUSED -> RUNNING)
    // 상태 검증은 Service 담당, 여기서는 상태값만 변경
    public void resume(LocalDateTime now) {
        this.status = SoloSessionStatus.RUNNING;
        this.lastResumedAt = now;
    }

    // 일시정지 (RUNNING -> PAUSED)
    // 누적 시간 계산은 Service 담당 (accumulateSeconds 호출 후 이 메서드 호출)
    public void pause() {
        this.status = SoloSessionStatus.PAUSED;
        this.lastResumedAt = null;
    }

    // 종료 (RUNNING/PAUSED -> ENDED)
    public void end(LocalDateTime now) {
        this.status = SoloSessionStatus.ENDED;
        this.endTime = now;
        this.lastResumedAt = null;
    }

    // 누적 시간 더하기 (Service가 lastResumedAt - now 구간을 계산해서 전달)
    public void accumulateSeconds(int seconds) {
        this.totalSeconds += Math.max(seconds, 0);
    }

    public boolean isRunning() {
        return this.status == SoloSessionStatus.RUNNING;
    }

    public boolean isPaused() {
        return this.status == SoloSessionStatus.PAUSED;
    }

    public boolean isEnded() {
        return this.status == SoloSessionStatus.ENDED;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public SoloSessionStatus getStatus() { return status; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public int getTotalSeconds() { return totalSeconds; }
    public LocalDateTime getLastResumedAt() { return lastResumedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public enum SoloSessionStatus {
        RUNNING, PAUSED, ENDED
    }
}