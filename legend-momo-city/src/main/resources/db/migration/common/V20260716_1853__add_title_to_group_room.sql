-- =====================================================================
--  열품타 (Study Timer) - group_room title 컬럼 추가
-- =====================================================================

ALTER TABLE `group_room`
    ADD COLUMN `title` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '그룹방 제목'
        AFTER `host_user_id`;