-- =====================================================================
--  MoMo City - Database Schema (DDL) [V1 baseline]
--  Engine : InnoDB / Charset : utf8mb4
-- =====================================================================
USE momo;

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

-- =====================================================================
--  DROP (역순 안전 삭제)
-- =====================================================================
DROP TABLE IF EXISTS `order_history`;
DROP TABLE IF EXISTS `store`;
DROP TABLE IF EXISTS `admin_notice`;
DROP TABLE IF EXISTS `report`;
DROP TABLE IF EXISTS `error_log`;
DROP TABLE IF EXISTS `access_log`;
DROP TABLE IF EXISTS `user_oauth`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `guestbook`;
DROP TABLE IF EXISTS `friend`;
DROP TABLE IF EXISTS `message_read`;
DROP TABLE IF EXISTS `message_announce`;
DROP TABLE IF EXISTS `message`;
DROP TABLE IF EXISTS `chat_room_member`;
DROP TABLE IF EXISTS `chat_room`;
DROP TABLE IF EXISTS `calendar`;
DROP TABLE IF EXISTS `building`;
DROP TABLE IF EXISTS `streak`;
DROP TABLE IF EXISTS `review`;
DROP TABLE IF EXISTS `learning_history`;
DROP TABLE IF EXISTS `enrollment`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `post_like`;
DROP TABLE IF EXISTS `post_content`;
DROP TABLE IF EXISTS `post`;
DROP TABLE IF EXISTS `chapter`;
DROP TABLE IF EXISTS `lecture`;
DROP TABLE IF EXISTS `user`;

-- =====================================================================
--  1. user
-- =====================================================================
CREATE TABLE `user` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT,
    `email`             VARCHAR(100) NULL,
    `password`          VARCHAR(255) NULL,
    `name`              VARCHAR(50)  NOT NULL,
    `nickname`          VARCHAR(30)  NOT NULL,
    `profile_image_url` VARCHAR(500) NULL,
    `role`              ENUM('STUDENT','TEACHER','ADMIN')                              NOT NULL DEFAULT 'STUDENT',
    `status`            ENUM('ACTIVE','PENDING','REJECTED','BANNED','BLACK','DELETED') NOT NULL DEFAULT 'ACTIVE',
    `category`          ENUM('FITNESS','STUDY','COOK','BEAUTY','ART')                  NULL,
    `proof`             VARCHAR(500) NULL,
    `point`             INT          NULL,
    `do_not_disturb`    BOOLEAN      NOT NULL DEFAULT FALSE,
    `membership`        ENUM('BASIC','PLUS','PRO')                                     NOT NULL DEFAULT 'BASIC',
    `membership_start`  DATETIME     NULL,
    `suspension_count`  BIGINT       NOT NULL DEFAULT 0,
    `suspended_until`   DATETIME     NULL,
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at`        DATETIME     NULL,
    `is_tempPWD`        BOOLEAN      NOT NULL DEFAULT FALSE,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_user_email`    (`email`),
    UNIQUE KEY `uq_user_nickname` (`nickname`),
    KEY `idx_user_role`       (`role`),
    KEY `idx_user_status`     (`status`),
    KEY `idx_user_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  2. lecture
-- =====================================================================
CREATE TABLE `lecture` (
    `id`                   BIGINT                                        NOT NULL AUTO_INCREMENT,
    `title`                VARCHAR(200)                                  NOT NULL,
    `created_at`           DATETIME(6)                                   NOT NULL,
    `updated_at`           DATETIME(6)                                   NOT NULL,
    `deleted_at`           DATETIME(6)                                   DEFAULT NULL,
    `category`             ENUM('ART','BEAUTY','COOK','FITNESS','STUDY') NOT NULL,
    `completed_user_count` INT                                           NOT NULL,
    `description`          TEXT                                          NOT NULL,
    `status`               ENUM('ACTIVE','DELETED','HOLD','WAITING')     NOT NULL,
    `teacher_id`           BIGINT                                        NOT NULL,
    `thumbnail_url`        VARCHAR(500)                                  DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_lecture_teacher_id`  (`teacher_id`),
    KEY `idx_lecture_status`      (`status`),
    KEY `idx_lecture_category`    (`category`),
    KEY `idx_lecture_deleted_at`  (`deleted_at`),
    FULLTEXT KEY `ft_lecture` (`title`,`description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  3. chapter
-- =====================================================================
CREATE TABLE `chapter` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT,
    `created_at`        DATETIME(6)  NOT NULL,
    `updated_at`        DATETIME(6)  NOT NULL,
    `deleted_at`        DATETIME(6)  DEFAULT NULL,
    `thumbnail_url`     VARCHAR(500) DEFAULT NULL,
    `duration_sec`      INT          DEFAULT NULL,
    `lecture_id`        BIGINT       NOT NULL,
    `order_no`          INT          NOT NULL DEFAULT 0,
    `original_filename` VARCHAR(255) DEFAULT NULL,
    `title`             VARCHAR(200) NOT NULL,
    `video_size_bytes`  BIGINT       DEFAULT NULL,
    `video_url`         VARCHAR(500) DEFAULT NULL,

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  4. post
-- =====================================================================
CREATE TABLE `post` (
    `id`            BIGINT                                               NOT NULL AUTO_INCREMENT,
    `created_at`    DATETIME(6)                                          NOT NULL,
    `updated_at`    DATETIME(6)                                          NOT NULL,
    `category`      ENUM('ART','BEAUTY','COOK','FITNESS','FREE','STUDY') NOT NULL,
    `deleted_at`    DATETIME(6)                                          DEFAULT NULL,
    `post_like`     INT                                                  NOT NULL DEFAULT 0,
    `thumbnail_url` VARCHAR(500)                                         DEFAULT NULL,
    `title`         VARCHAR(200)                                         NOT NULL,
    `user_id`       BIGINT                                               NOT NULL,
    `view_count`    INT                                                  NOT NULL DEFAULT 0,

    PRIMARY KEY (`id`),
    KEY `idx_post_user`       (`user_id`),
    KEY `idx_post_category`   (`category`),
    KEY `idx_post_created_at` (`created_at`),
    FULLTEXT KEY `ft_post` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  5. post_content
-- =====================================================================
CREATE TABLE `post_content` (
    `id`         BIGINT               NOT NULL AUTO_INCREMENT,
    `content`    TEXT                 DEFAULT NULL,
    `created_at` DATETIME(6)          NOT NULL,
    `image_url`  VARCHAR(500)         DEFAULT NULL,
    `order_no`   TINYINT              NOT NULL,
    `post_id`    BIGINT               NOT NULL,
    `type`       ENUM('IMAGE','TEXT') NOT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_post_content_post` (`post_id`,`order_no`),
    KEY `idx_post_content_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  6. post_like
-- =====================================================================
CREATE TABLE `post_like` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `created_at` DATETIME(6) NOT NULL,
    `post_id`    BIGINT      NOT NULL,
    `user_id`    BIGINT      NOT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_post_like_post_id_user_id` (`post_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  7. comment
-- =====================================================================
CREATE TABLE `comment` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `created_at` DATETIME(6)  NOT NULL,
    `updated_at` DATETIME(6)  NOT NULL,
    `content`    VARCHAR(500) NOT NULL,
    `deleted_at` DATETIME(6)  DEFAULT NULL,
    `parent_id`  BIGINT       DEFAULT NULL,
    `post_id`    BIGINT       NOT NULL,
    `user_id`    BIGINT       NOT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_comment_post`   (`post_id`),
    KEY `idx_comment_user`   (`user_id`),
    KEY `idx_comment_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  8. enrollment
-- =====================================================================
CREATE TABLE `enrollment` (
    `id`              BIGINT      NOT NULL AUTO_INCREMENT,
    `completed_count` INT         NOT NULL DEFAULT 0,
    `enrolled_at`     DATETIME(6) NOT NULL,
    `lecture_id`      BIGINT      NOT NULL,
    `total_progress`  INT         NOT NULL DEFAULT 0,
    `user_id`         BIGINT      NOT NULL,
    `is_completed`    BOOLEAN  NOT NULL DEFAULT FALSE,

    PRIMARY KEY (`id`),
    KEY `idx_enrollment_lecture` (`lecture_id`),
    KEY `idx_enrollment_lecture_id` (`lecture_id`),
    KEY `idx_enrollment_user_id`    (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  9. learning_history
-- =====================================================================
CREATE TABLE `learning_history` (
    `id`                BIGINT      NOT NULL AUTO_INCREMENT,
    `created_at`        DATETIME(6) NOT NULL,
    `updated_at`        DATETIME(6) NOT NULL,
    `chapter_id`        BIGINT      NOT NULL,
    `is_completed`      BOOLEAN     NOT NULL DEFAULT FALSE,
    `last_position_sec` INT         NOT NULL DEFAULT 0,
    `last_watched_at`   DATETIME(6) DEFAULT NULL,
    `lecture_id`        BIGINT      NOT NULL,
    `progress_rate`     INT         NOT NULL,
    `user_id`           BIGINT      NOT NULL,
    `version`           BIGINT      NOT NULL DEFAULT 0,
    `watched_seconds`   INT         NOT NULL DEFAULT 0,

    PRIMARY KEY (`id`),
    KEY `idx_lh_user`    (`user_id`),
    KEY `idx_lh_lecture` (`lecture_id`),
    KEY `idx_lh_chapter` (`chapter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  10. review
-- =====================================================================
CREATE TABLE `review` (
    `id`         BIGINT                   NOT NULL AUTO_INCREMENT,
    `content`    TEXT                     NOT NULL,
    `created_at` DATETIME(6)              NOT NULL,
    `deleted_at` DATETIME(6)              DEFAULT NULL,
    `lecture_id` BIGINT                   NOT NULL,
    `rating`     TINYINT                  NOT NULL,
    `status`     ENUM('ACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE',
    `user_id`    BIGINT                   NOT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_review_lecture` (`lecture_id`),
    KEY `idx_review_user`    (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  11. streak
-- =====================================================================
CREATE TABLE `streak` (
    `id`                    BIGINT                                             NOT NULL AUTO_INCREMENT,
    `created_at`            DATETIME(6)                                        NOT NULL,
    `daily_watched_seconds` INT                                                NOT NULL DEFAULT 0,
    `level`                 ENUM('LEVEL0','LEVEL1','LEVEL2','LEVEL3','LEVEL4') NOT NULL DEFAULT 'LEVEL0',
    `streak_date`           DATE                                               NOT NULL,
    `user_id`               BIGINT                                             NOT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_streak_user_date` (`user_id`,`streak_date`),
    UNIQUE KEY `uq_streak_user_id_streak_date` (`user_id`,`streak_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  12. building
-- =====================================================================
CREATE TABLE `building` (
    `id`         BIGINT                                        NOT NULL AUTO_INCREMENT,
    `created_at` DATETIME(6)                                   NOT NULL,
    `updated_at` DATETIME(6)                                   NOT NULL,
    `category`   ENUM('ART','BEAUTY','COOK','FITNESS','STUDY') NOT NULL,
    `level`      INT                                           NOT NULL DEFAULT 1,
    `position`   BIGINT                                        NOT NULL,
    `user_id`    BIGINT                                        NOT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_building_user` (`user_id`),
    UNIQUE KEY `uq_building_user_id_category` (`user_id`,`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  13. calendar
-- =====================================================================
CREATE TABLE `calendar` (
    `id`           BIGINT              NOT NULL AUTO_INCREMENT,
    `category`     ENUM('MEMO','TODO') NOT NULL DEFAULT 'MEMO',
    `end`          DATE                DEFAULT NULL,
    `is_completed` BOOLEAN             NOT NULL DEFAULT FALSE,
    `start`        DATE                NOT NULL,
    `title`        VARCHAR(255)        NOT NULL,
    `user_id`      BIGINT              NOT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_calendar_user`          (`user_id`),
    KEY `idx_calendar_user_category` (`user_id`,`category`),
    KEY `idx_calendar_date`          (`start`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  14. chat_room
-- =====================================================================
CREATE TABLE `chat_room` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `created_at` DATETIME(6)  DEFAULT NULL,
    `title`      VARCHAR(100) DEFAULT NULL,
    `updated_at` DATETIME(6)  DEFAULT NULL,

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  15. chat_room_member
-- =====================================================================
CREATE TABLE `chat_room_member` (
    `id`        BIGINT      NOT NULL AUTO_INCREMENT,
    `joined_at` DATETIME(6) NOT NULL,
    `room_id`   BIGINT      NOT NULL,
    `user_id`   BIGINT      NOT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_chat_room_member_room_id` (`room_id`),
    KEY `idx_chat_room_member_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  16. message
-- =====================================================================
CREATE TABLE `message` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `content`    TEXT         DEFAULT NULL,
    `created_at` DATETIME(6)  NOT NULL,
    `updated_at` DATETIME(6)  DEFAULT NULL,
    `room_id`    BIGINT       NOT NULL,
    `sender_id`  BIGINT       DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_message_room`   (`room_id`),
    KEY `idx_message_sender` (`sender_id`),
    KEY `idx_message_room_id`    (`room_id`),
    KEY `idx_message_sender_id`  (`sender_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  17. message_announce
-- =====================================================================
CREATE TABLE `message_announce` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `content`    TEXT         NOT NULL,
    `created_at` DATETIME(6)  DEFAULT NULL,
    `type`       ENUM('LEAVE','INVITE','RENAME') DEFAULT NULL,
    `room_id`    BIGINT       DEFAULT NULL,
    `target_id`  BIGINT       DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_announce_room_created` (`room_id`,`created_at`),
    KEY `idx_announce_target_user`  (`target_id`),
    KEY `idx_message_announce_room_id`   (`room_id`),
    KEY `idx_message_announce_target_id` (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  18. message_read
-- =====================================================================
CREATE TABLE `message_read` (
    `id`           BIGINT NOT NULL AUTO_INCREMENT,
    `is_deleted`   BOOLEAN NOT NULL DEFAULT FALSE,
    `is_msg_read`  BOOLEAN NOT NULL DEFAULT FALSE,
    `is_noti_read` BOOLEAN NOT NULL DEFAULT FALSE,
    `message_id`   BIGINT DEFAULT NULL,
    `room_id`      BIGINT DEFAULT NULL,
    `user_id`      BIGINT DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_msg_read_room`        (`room_id`),
    KEY `idx_msg_read_message`     (`message_id`),
    KEY `idx_msg_read_target_user` (`user_id`),
    KEY `idx_message_read_message_id` (`message_id`),
    KEY `idx_message_read_room_id`    (`room_id`),
    KEY `idx_message_read_user_id`    (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  19. friend
-- =====================================================================
CREATE TABLE `friend` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `created_at`   DATETIME(6)  DEFAULT NULL,
    `status`       ENUM('SENT', 'FRIEND', 'BLOCK') NOT NULL DEFAULT 'SENT',
    `updated_at`   DATETIME(6)  DEFAULT NULL,
    `from_user_id` BIGINT       NOT NULL,
    `to_user_id`   BIGINT       NOT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_friend_pair` (`from_user_id`, `to_user_id`),
    KEY `idx_friend_to_user` (`to_user_id`),
    KEY `idx_friend_status`  (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  20. guestbook
-- =====================================================================
CREATE TABLE `guestbook` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `is_read`    BOOLEAN      NOT NULL DEFAULT FALSE,
    `content`    VARCHAR(1000) NOT NULL,
    `created_at` DATETIME(6)  DEFAULT NULL,
    `owner_id`   BIGINT       DEFAULT NULL,
    `writer_id`  BIGINT       DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_guestbook_owner`  (`owner_id`),
    KEY `idx_guestbook_writer` (`writer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  21. notification
-- =====================================================================
CREATE TABLE `notification` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `created_at` DATETIME(6)  NOT NULL,
    `is_read`    BOOLEAN      NOT NULL DEFAULT FALSE,
    `message`    VARCHAR(500) DEFAULT NULL,
    `ref_id`     BIGINT       DEFAULT NULL,
    `type`       ENUM('NOTICE', 'REPORT', 'FRIEND_REQUEST', 'MESSAGE', 'GUESTBOOK', 'POST', 'CALENDAR', 'PAYMENT') NOT NULL,
    `user_id`    BIGINT       DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_notification_user` (`user_id`),
    KEY `idx_notification_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  22. user_oauth
-- =====================================================================
CREATE TABLE `user_oauth` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `created_at`  DATETIME(6)  NOT NULL,
    `provider`    ENUM('KAKAO', 'GOOGLE', 'NAVER')      NOT NULL,
    `provider_id` VARCHAR(100) DEFAULT NULL,
    `user_id`     BIGINT       NOT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_user_oauth_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  23. access_log
-- =====================================================================
CREATE TABLE `access_log` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `action`     ENUM('LOGIN', 'LOGOUT', 'FORBIDDEN') DEFAULT NULL,
    `created_at` DATETIME(6) DEFAULT NULL,
    `ip`         VARCHAR(45) DEFAULT NULL,
    `user_id`    BIGINT      DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_access_log_user_id`    (`user_id`),
    KEY `idx_access_log_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  24. error_log
-- =====================================================================
CREATE TABLE `error_log` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `created_at`  DATETIME(6)   NOT NULL,
    `updated_at`  DATETIME(6)   NOT NULL,
    `level`       ENUM('CRITICAL', 'ERROR', 'WARNING') DEFAULT NULL,
    `message`     VARCHAR(1000) NOT NULL,
    `occurred_at` DATETIME(6)   NOT NULL,
    `source`      VARCHAR(50)   NOT NULL,

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  25. report
-- =====================================================================
CREATE TABLE `report` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT,
    `created_at`       DATETIME(6)   NOT NULL,
    `detail`           VARCHAR(1000) DEFAULT NULL,
    `is_resolved`      BIT(1)        NOT NULL,
    `reason`           ENUM('SPAM', 'ABUSE', 'INAPPROPRIATE', 'COPYRIGHT', 'OTHER')   NOT NULL,
    `reported_user_id` BIGINT        DEFAULT NULL,
    `reporter_user_id` BIGINT        DEFAULT NULL,
    `resolved_at`      DATETIME(6)   DEFAULT NULL,
    `target_id`        BIGINT        DEFAULT NULL,
    `target_path`      VARCHAR(500)  DEFAULT NULL,
    `target_type`      ENUM('POST', 'COMMENT', 'LECTURE', 'CHAPTER', 'REVIEW', 'CHAT', 'PAGE')   NOT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_report_reporter`   (`reporter_user_id`),
    KEY `idx_report_reported`   (`reported_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  26. admin_notice
-- =====================================================================
CREATE TABLE `admin_notice` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `content`    TEXT         NOT NULL,
    `created_at` DATETIME(6)  DEFAULT NULL,
    `is_pinned`  BOOLEAN      NOT NULL DEFAULT FALSE,
    `title`      VARCHAR(200) DEFAULT NULL,
    `updated_at` DATETIME(6)  DEFAULT NULL,

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  27. store
-- =====================================================================
CREATE TABLE `store` (
    `id`         BIGINT          NOT NULL AUTO_INCREMENT,
    `created_at` DATETIME(6)     NOT NULL,
    `name`       VARCHAR(500)    NOT NULL,
    `price`      BIGINT          NOT NULL,
    `type`       ENUM('PROFILE') DEFAULT NULL,
    `url`        VARCHAR(500)    DEFAULT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_store_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  28. order_history
-- =====================================================================
CREATE TABLE `order_history` (
    `id`         BIGINT                                                NOT NULL AUTO_INCREMENT,
    `amount`     BIGINT                                                NOT NULL,
    `created_at` DATETIME(6)                                           NOT NULL,
    `item_id`    BIGINT                                                DEFAULT NULL,
    `reason`     ENUM('BUS','COMPLETE','GUESTBOOK','PROFILE','REVIEW') NOT NULL,
    `type`       ENUM('GAINED','USED')                                 NOT NULL,
    `user_id`    BIGINT                                                NOT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_order_history_user` (`user_id`),
    UNIQUE KEY `uq_order_history_user_id_item_id` (`user_id`,`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
--  FOREIGN KEY CONSTRAINTS
-- =====================================================================

-- lecture
ALTER TABLE `lecture`
    ADD CONSTRAINT `fk_lecture_teacher`
        FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE;

-- chapter
ALTER TABLE `chapter`
    ADD CONSTRAINT `fk_chapter_lecture`
        FOREIGN KEY (`lecture_id`) REFERENCES `lecture` (`id`)
        ON DELETE CASCADE;

-- post
ALTER TABLE `post`
    ADD CONSTRAINT `fk_post_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE;

-- post_content
ALTER TABLE `post_content`
    ADD CONSTRAINT `fk_post_content_post`
        FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
        ON DELETE CASCADE;

-- post_like
ALTER TABLE `post_like`
    ADD CONSTRAINT `fk_post_like_post`
        FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_post_like_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE;

-- comment
ALTER TABLE `comment`
    ADD CONSTRAINT `fk_comment_post`
        FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_comment_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_comment_parent`
        FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`)
        ON DELETE CASCADE;

-- enrollment
ALTER TABLE `enrollment`
    ADD CONSTRAINT `fk_enrollment_lecture`
        FOREIGN KEY (`lecture_id`) REFERENCES `lecture` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_enrollment_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE;

-- learning_history
ALTER TABLE `learning_history`
    ADD CONSTRAINT `fk_learning_history_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_learning_history_lecture`
        FOREIGN KEY (`lecture_id`) REFERENCES `lecture` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_learning_history_chapter`
        FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`)
        ON DELETE CASCADE;

-- review
ALTER TABLE `review`
    ADD CONSTRAINT `fk_review_lecture`
        FOREIGN KEY (`lecture_id`) REFERENCES `lecture` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_review_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE;

-- streak
ALTER TABLE `streak`
    ADD CONSTRAINT `fk_streak_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE;

-- building
ALTER TABLE `building`
    ADD CONSTRAINT `fk_building_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE;

-- calendar
ALTER TABLE `calendar`
    ADD CONSTRAINT `fk_calendar_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE;

-- chat_room_member
ALTER TABLE `chat_room_member`
    ADD CONSTRAINT `fk_chat_room_member_room`
        FOREIGN KEY (`room_id`) REFERENCES `chat_room` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_chat_room_member_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE;

-- message
ALTER TABLE `message`
    ADD CONSTRAINT `fk_message_room`
        FOREIGN KEY (`room_id`) REFERENCES `chat_room` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_message_sender`
        FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`)
        ON DELETE SET NULL;

-- message_announce
ALTER TABLE `message_announce`
    ADD CONSTRAINT `fk_message_announce_room`
        FOREIGN KEY (`room_id`) REFERENCES `chat_room` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_message_announce_target`
        FOREIGN KEY (`target_id`) REFERENCES `user` (`id`)
        ON DELETE SET NULL;

-- message_read
ALTER TABLE `message_read`
    ADD CONSTRAINT `fk_message_read_room`
        FOREIGN KEY (`room_id`) REFERENCES `chat_room` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_message_read_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_message_read_message`
        FOREIGN KEY (`message_id`) REFERENCES `message` (`id`)
        ON DELETE CASCADE;

-- friend
ALTER TABLE `friend`
    ADD CONSTRAINT `fk_friend_from_user`
        FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_friend_to_user`
        FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE;

-- guestbook
ALTER TABLE `guestbook`
    ADD CONSTRAINT `fk_guestbook_owner`
        FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_guestbook_writer`
        FOREIGN KEY (`writer_id`) REFERENCES `user` (`id`)
        ON DELETE SET NULL;

-- notification
ALTER TABLE `notification`
    ADD CONSTRAINT `fk_notification_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE;

-- user_oauth
ALTER TABLE `user_oauth`
    ADD CONSTRAINT `fk_user_oauth_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE;

-- access_log
ALTER TABLE `access_log`
    ADD CONSTRAINT `fk_access_log_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE SET NULL;

-- report
ALTER TABLE `report`
    ADD CONSTRAINT `fk_report_reporter`
        FOREIGN KEY (`reporter_user_id`) REFERENCES `user` (`id`)
        ON DELETE SET NULL,
    ADD CONSTRAINT `fk_report_reported`
        FOREIGN KEY (`reported_user_id`) REFERENCES `user` (`id`)
        ON DELETE SET NULL;

-- order_history
ALTER TABLE `order_history`
    ADD CONSTRAINT `fk_order_history_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
--  END OF SCHEMA  (tables: 28)
-- =====================================================================

