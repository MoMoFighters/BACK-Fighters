-- =====================================================================
--  열품타 (Study Timer) 도메인 마이그레이션
--  담당: 누(DBA)
--  경로: db/migration/common
-- =====================================================================

-- =====================================================================
--  1. solo_session — 혼자 공부 세션
-- =====================================================================
CREATE TABLE `solo_session` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT       NOT NULL,
    `status`           ENUM('RUNNING', 'PAUSED', 'ENDED') NOT NULL DEFAULT 'RUNNING',
    `start_time`       DATETIME(6)  NOT NULL,
    `end_time`         DATETIME(6)  DEFAULT NULL,
    `total_seconds`    INT          NOT NULL DEFAULT 0,
    `created_at`       DATETIME(6)  DEFAULT NULL,
    `updated_at`       DATETIME(6)  DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_solo_session_user`   (`user_id`),
    KEY `idx_solo_session_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  2. group_room — 그룹 공부방
-- =====================================================================
CREATE TABLE `group_room` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `host_user_id`     BIGINT       NOT NULL,
    `invite_code`      VARCHAR(20)  NOT NULL,
    `status`           ENUM('ACTIVE', 'ENDED') NOT NULL DEFAULT 'ACTIVE',
    `max_member`       TINYINT      NOT NULL DEFAULT 4,
    `deleted_at`       DATETIME(6)  DEFAULT NULL,
    `created_at`       DATETIME(6)  DEFAULT NULL,
    `updated_at`       DATETIME(6)  DEFAULT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_group_room_invite_code` (`invite_code`),
    KEY `idx_group_room_host`    (`host_user_id`),
    KEY `idx_group_room_status`  (`status`),
    KEY `idx_group_room_deleted` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  3. group_room_member — 그룹방 멤버 (초대~입퇴장 통합 관리)
--     status: INVITED(초대됨) / JOINED(참가중) / LEFT(나감) /
--             REJECTED(초대받은 사람이 거절) / CANCELED(초대한 사람이 취소)
-- =====================================================================
CREATE TABLE `group_room_member` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `group_room_id`    BIGINT       NOT NULL,
    `user_id`          BIGINT       NOT NULL,
    `status`           ENUM('INVITED', 'JOINED', 'LEFT', 'REJECTED', 'CANCELED') NOT NULL DEFAULT 'INVITED',
    `timer_status`     ENUM('STUDYING', 'RESTING') DEFAULT NULL,
    `invited_at`       DATETIME(6)  DEFAULT NULL,
    `joined_at`        DATETIME(6)  DEFAULT NULL,
    `left_at`          DATETIME(6)  DEFAULT NULL,
    `created_at`       DATETIME(6)  DEFAULT NULL,
    `updated_at`       DATETIME(6)  DEFAULT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_group_room_member` (`group_room_id`, `user_id`),
    KEY `idx_group_room_member_user`   (`user_id`),
    KEY `idx_group_room_member_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  4. daily_study_record — 개인 일별 누적 공부시간 (솔로+그룹 통합, 잔디용)
-- =====================================================================
CREATE TABLE `daily_study_record` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT       NOT NULL,
    `study_date`       DATE         NOT NULL,
    `total_seconds`    INT          NOT NULL DEFAULT 0,
    `created_at`       DATETIME(6)  DEFAULT NULL,
    `updated_at`       DATETIME(6)  DEFAULT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_daily_study_record` (`user_id`, `study_date`),
    KEY `idx_daily_study_record_date` (`study_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  5. monthly_study_record — 개인 월별 누적 공부시간 (이벤트 기반 실시간 갱신)
-- =====================================================================
CREATE TABLE `monthly_study_record` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT       NOT NULL,
    `year_month`       CHAR(7)      NOT NULL COMMENT 'YYYY-MM 형식',
    `total_seconds`    INT          NOT NULL DEFAULT 0,
    `created_at`       DATETIME(6)  DEFAULT NULL,
    `updated_at`       DATETIME(6)  DEFAULT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_monthly_study_record` (`user_id`, `year_month`),
    KEY `idx_monthly_study_record_ym` (`year_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
