-- =====================================================================
--  열품타 (Study Timer) - group_room invite_code 컬럼 제거
-- =====================================================================

ALTER TABLE `group_room`
    DROP INDEX `uq_group_room_invite_code`;

ALTER TABLE `group_room`
    DROP COLUMN `invite_code`;