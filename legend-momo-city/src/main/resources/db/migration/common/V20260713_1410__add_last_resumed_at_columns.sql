-- =====================================================================
--  열품타 (Study Timer) - 타이머 재개 시각 컬럼 추가
--  -
--  추가 이유:
--  solo_session, group_room_member 모두 "마지막으로 RUNNING/STUDYING이 된 시각"을
--  저장할 컬럼이 없어서, 서버 재시작이나 재조회 시 진행 중인 타이머의 경과시간을
--  정확히 복원할 방법이 없음 -> last_resumed_at 컬럼을 추가해 이 값을 영속화
-- =====================================================================

ALTER TABLE `solo_session`
    ADD COLUMN `last_resumed_at` DATETIME(6) DEFAULT NULL
        COMMENT '마지막으로 RUNNING이 된 시각, PAUSED/ENDED면 NULL'
        AFTER `total_seconds`;

ALTER TABLE `group_room_member`
    ADD COLUMN `last_resumed_at` DATETIME(6) DEFAULT NULL
        COMMENT '마지막으로 STUDYING이 된 시각, RESTING/NULL이면 NULL'
        AFTER `timer_status`;
