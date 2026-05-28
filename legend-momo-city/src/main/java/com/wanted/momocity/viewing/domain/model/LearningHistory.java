package com.wanted.momocity.viewing.domain.model;

import lombok.Getter;

@Getter
public class LearningHistory {

    private Long id;
    private Long userId;
    private Long lectureId;
    private Long chapterId;
    // 최대 누적 시청 시간 (감소 안 함)
    private int watchedSeconds;
    private boolean isCompleted;
    // 마지막 재생 위치 (이어보기용)
    private int lastPositionSec;
    private int progressRate;
    // createdAt, updateAt 은 JPA 에서 관리

    // 신규 생성용
    public static LearningHistory create(
            Long userId, Long lectureId, Long chapterId
    ) {
        LearningHistory history = new LearningHistory();
        history.userId = userId;
        history.lectureId = lectureId;
        history.chapterId = chapterId;
        history.watchedSeconds = 0;
        history.isCompleted = false;
        history.lastPositionSec = 0;
        history.progressRate = 0;
        return history;
    }

    // 진척도 업데이트
    public void updateProgress (
            // 현재 재생 위치
            int playbackSeconds, int durationSec
    ) {
        if (playbackSeconds > this.watchedSeconds) {
            this.watchedSeconds = playbackSeconds;
            this.progressRate = (int) Math.round(
                    (double) this.watchedSeconds / durationSec * 100
            );
            if (this.progressRate >= 100) {
                this.progressRate = 100;
            }
        }
    }

    // 챕터 완료처리
    public void complete (int playbackSeconds, int durationSec) {
        if(!this.isCompleted && playbackSeconds >= durationSec * 0.9) {
            this.isCompleted = true;
            this.progressRate = 100;
            this.watchedSeconds = durationSec;
        }
    }

    // 나가기 버튼 클릭 시 이어보기 지점 저장
    public void saveLastPosition(int lastPositionSec) {
        this.lastPositionSec = lastPositionSec;
    }

    // DB 에서 조회한 데이터로 도메인 객체 복원용
    // create() 는 신규 생성, reconstitute() 는 DB 복원
    public static LearningHistory reconstitute(
            Long id, Long userId, Long lectureId, Long chapterId, int watchedSeconds, boolean isCompleted,
            int lastPositionSec, int progressRate
    ) {
        LearningHistory history = new LearningHistory();
        history.id = id;
        history.userId = userId;
        history.lectureId = lectureId;
        history.chapterId = chapterId;
        history.watchedSeconds = watchedSeconds;
        history.isCompleted = isCompleted;
        history.lastPositionSec = lastPositionSec;
        history.progressRate = progressRate;
        return history;
    }

}
