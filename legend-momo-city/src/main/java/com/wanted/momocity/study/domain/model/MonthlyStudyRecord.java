package com.wanted.momocity.study.domain.model;

import java.time.LocalDateTime;
import java.time.YearMonth;

/*
 * comment.
 *  개인 월별 누적 공부시간 도메인 역할 -> 순수 비즈니스 데이터만 담당 (JPA 모름)
 *  -
 *  솔로 + 그룹 통합, 방 구분 없이 유저 단위로 한 달치 총 공부시간을 가짐
 *  방 랭킹(월별)의 데이터 소스
 *  -
 *  StudySessionEndedEvent 수신 시 DailyStudyRecord와 동시에
 *  StudyRecordEventHandler가 이 record를 찾아 accumulate() 하거나, 없으면 create()로 새로 만듦
 * */

public class MonthlyStudyRecord {

    private Long id;
    private Long userId;
    private YearMonth yearMonth;
    private int totalSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 신규 생성용
    public static MonthlyStudyRecord create(Long userId, YearMonth yearMonth, int seconds) {
        MonthlyStudyRecord record = new MonthlyStudyRecord();
        record.userId = userId;
        record.yearMonth = yearMonth;
        record.totalSeconds = Math.max(seconds, 0);
        return record;
    }

    // DB 복원용
    public static MonthlyStudyRecord reconstitute(
            Long id, Long userId, YearMonth yearMonth, int totalSeconds,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        MonthlyStudyRecord record = new MonthlyStudyRecord();
        record.id = id;
        record.userId = userId;
        record.yearMonth = yearMonth;
        record.totalSeconds = totalSeconds;
        record.createdAt = createdAt;
        record.updatedAt = updatedAt;
        return record;
    }

    public void accumulate(int seconds) {
        this.totalSeconds += Math.max(seconds, 0);
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public YearMonth getYearMonth() { return yearMonth; }
    public int getTotalSeconds() { return totalSeconds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}