package com.wanted.momocity.study.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * comment.
 *  개인 일별 누적 공부시간 도메인 역할 -> 순수 비즈니스 데이터만 담당 (JPA 모름)
 *  -
 *  솔로 + 그룹 통합, 방 구분 없이 유저 단위로 하루치 총 공부시간을 가짐
 *  잔디(연간 조회) 및 방 랭킹(일별)의 데이터 소스
 *  -
 *  StudySessionEndedEvent(및 타이머 종료 이벤트) 수신 시 StudyRecordEventHandler가
 *  이 record를 찾아 accumulate() 하거나, 없으면 createNew()로 새로 만듦
 *  자정을 걸친 세션은 이벤트 발행 단계에서 날짜별로 분할되어 각각 반영됨
 * */

public class DailyStudyRecord {

    private Long id;
    private Long userId;
    private LocalDate studyDate;
    private int totalSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 신규 생성용
    public static DailyStudyRecord create(Long userId, LocalDate studyDate, int seconds) {
        DailyStudyRecord record = new DailyStudyRecord();
        record.userId = userId;
        record.studyDate = studyDate;
        record.totalSeconds = Math.max(seconds, 0);
        return record;
    }

    // DB 복원용
    public static DailyStudyRecord reconstitute(
            Long id, Long userId, LocalDate studyDate, int totalSeconds,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        DailyStudyRecord record = new DailyStudyRecord();
        record.id = id;
        record.userId = userId;
        record.studyDate = studyDate;
        record.totalSeconds = totalSeconds;
        record.createdAt = createdAt;
        record.updatedAt = updatedAt;
        return record;
    }

    // 기존 record에 이번 세션(또는 자정 분할된 일부) 시간을 더함
    public void accumulate(int seconds) {
        this.totalSeconds += Math.max(seconds, 0);
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public LocalDate getStudyDate() { return studyDate; }
    public int getTotalSeconds() { return totalSeconds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}