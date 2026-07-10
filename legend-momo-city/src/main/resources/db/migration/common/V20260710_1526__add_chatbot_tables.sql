-- =====================================================================
--  MoMo City - Migration
--  V20260710_1526__add_chatbot_tables.sql
--  챗봇 관련 테이블 2개 추가
--  - chatbot_daily_usage  : 유저 하루 5회 호출 제한 체크용
--  - chatbot_question_log : 동일/유사 질문 3회 반복 판별용
-- =====================================================================
USE momo;

SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
--  29. chatbot_daily_usage — 유저 하루 5회 호출 제한 체크용
-- =====================================================================
CREATE TABLE `chatbot_daily_usage` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT      NOT NULL,
    `usage_date` DATE        NOT NULL,
    `call_count` INT         NOT NULL DEFAULT 0,
    `token_used` INT         DEFAULT NULL,
    `created_at` DATETIME(6) DEFAULT NULL,
    `updated_at` DATETIME(6) DEFAULT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_chatbot_daily_usage_user_date` (`user_id`, `usage_date`),
    KEY `idx_chatbot_daily_usage_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  30. chatbot_question_log — 동일/유사 질문 3회 반복 판별용
-- =====================================================================
CREATE TABLE `chatbot_question_log` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`        BIGINT       NOT NULL,
    `lecture_id`     BIGINT       DEFAULT NULL,
    `question`       VARCHAR(100) DEFAULT NULL,
    `is_faq_matched` BOOLEAN      DEFAULT NULL,
    `created_at`     DATETIME(6)  DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_chatbot_question_log_user`    (`user_id`),
    KEY `idx_chatbot_question_log_lecture` (`lecture_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
--  END  (추가 tables: 2 → 총 30개)
-- =====================================================================
