package com.wanted.momocity.viewing.domain.model;

import lombok.Getter;

@Getter
public class LearningHistory {

    private Long id;
    private Long userId;
    private Long lectureId;
    private Long chapterId;
    // 실제로 본 최대 위치 저장
    // 뒤로 감기해도 감소안됨, 앞으로 당기기 반영 안함 (10초 초과시 무시)
    private int watchedSeconds;
    private boolean isCompleted;
    // 마지막 재생 위치 (이어보기용)
    private int lastPositionSec;
    private int progressRate;
    // createdAt, updateAt 은 JPA 에서 관리
    private Long version;

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
        // version = null -> 신규 INSERT 시 DB 가 0 으로 세팅
        return history;
    }

    // 진척도 업데이트
    /*
    * comment.
    *  1. playbackSeconds > watchedSeconds → 앞으로 진행
    *    AND playbackSeconds - watchedSeconds <= 10 -> 정상 시청 범위
    *    -> watchedSeconds = playbackSeconds
    *  -
    *  2. playbackSeconds < watchedSeconds -> 뒤로 감기
    *    -> watchedSeconds 업데이트 안 함
    *  -
    *  3. playbackSeconds - watchedSeconds > 10 -> 앞으로 당기기
    *    -> watchedSeconds 업데이트 안 함
    *  -
    *  progressRate = watchedSeconds / durationSec * 100
    * */

    public void updateProgress (
            // 현재 재생 위치
            int playbackSeconds, int durationSec
    ) {
        // watchedSeconds 업데이트
        if (playbackSeconds > this.watchedSeconds
                && playbackSeconds - this.watchedSeconds <= 10) {
            this.watchedSeconds = playbackSeconds;
        }

        // progressRate = watchedSeconds 기준
        this.progressRate = (int) Math.round(
                (double) this.watchedSeconds / durationSec * 100
        );
        if (this.progressRate >= 100) {
            this.progressRate = 100;
        }
    }

    /*
    * comment.
    *  playbackSeconds >= durationSec * 0.9 시 챕터 완료 처리
     * -> isCompleted = true
     * -> progressRate = 100
    * */

    // 챕터 완료처리
    public void complete (int durationSec) {
        if(!this.isCompleted && watchedSeconds >= durationSec * 0.9) {
            this.isCompleted = true;
            this.progressRate = 100;
        }
    }

    /*
     * comment.
     *  나가기 버튼 클릭 시 이어보기 지점 저장
     *  -
     *  progressRate >= 90 (거의 다 본 경우):
     *  -> lastPositionSec 저장
     *  -> 정상적으로 시청한 것으로 판단
     *  -
     *  progressRate < 90:
     *  -> watchedSeconds 저장
     *  -> 앞으로 당겨서 나간 것으로 판단
     *  -> 실제로 본 위치부터 이어보기
     */
    public void saveLastPosition(int lastPositionSec) {
        if (this.isCompleted) {
            // isCompleted = ture -> 나간 위치 그대로 저장
            this.lastPositionSec = lastPositionSec;
        } else {
            // isCompleted = false -> watchedSeconds 로 저장
            // 앞으로 당기거나 뒤로 간 경우 실제 시청 위치로 되돌림
            this.lastPositionSec = this.watchedSeconds;
        }
    }

    // DB 에서 조회한 데이터로 도메인 객체 복원용
    // create() 는 신규 생성, reconstitute() 는 DB 복원
    public static LearningHistory reconstitute(
            Long id, Long userId, Long lectureId, Long chapterId, int watchedSeconds, boolean isCompleted,
            int lastPositionSec, int progressRate, Long version
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
        history.version = version;
        return history;
    }

}
