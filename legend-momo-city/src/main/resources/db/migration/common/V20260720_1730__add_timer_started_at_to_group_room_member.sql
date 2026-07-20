-- timer_started_at_to_group_room_member.sql
-- 그룹 타이머 24시간 자동 만료 기능을 위한 컬럼 추가

ALTER TABLE group_room_member
    ADD COLUMN timer_started_at DATETIME NULL AFTER total_seconds;