-- =====================================================================
--  MoMo City - Database Schema (DDL)   [ver 4.0 - Final]
--  Engine : InnoDB / Charset : utf8mb4 / Collation : utf8mb4_unicode_ci
-- ---------------------------------------------------------------------
--  [ver 4.0 변경사항 요약]
--  ① suspension_log       → DROP (이력은 audit_log 흡수, user BC 합의)
--  ② report               → status 제거, is_read 추가
--  ③ inquiry              → status·updated_at 제거, is_answered 추가
--  ④ admin_message        → 신규 (관리자 우편함, sender SET NULL / recipient CASCADE)
--  ⑤ post + post_image    → post_content 테이블로 대체 (텍스트·이미지 순서 통합)
--                            post.content(TEXT) 제거
--  ⑥ chapter              → video_status 컬럼 제거
--  ⑦ streak               → chapter_id 제거, daily_watched_seconds·level 추가
--                            (user_id, streak_date) UNIQUE
--  ⑧ learning_history     → last_watched_at DATE 추가
--  ⑨ comment              → deleted_at 추가 (소프트딜리트)
--  ⑩ post.category ENUM   → HEALTH → FITNESS 로 통일
-- =====================================================================

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

-- =====================================================================
--  DROP (역순 안전 삭제)
-- =====================================================================
DROP TABLE IF EXISTS `admin_message`;
DROP TABLE IF EXISTS `inquiry`;
DROP TABLE IF EXISTS `report`;
DROP TABLE IF EXISTS `error_log`;
DROP TABLE IF EXISTS `access_log`;
DROP TABLE IF EXISTS `audit_log`;
DROP TABLE IF EXISTS `suspension_log`;
DROP TABLE IF EXISTS `user_oauth`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `announce_read`;
DROP TABLE IF EXISTS `order_history`;
DROP TABLE IF EXISTS `store`;
DROP TABLE IF EXISTS `guestbook`;
DROP TABLE IF EXISTS `friend`;
DROP TABLE IF EXISTS `message_announce`;
DROP TABLE IF EXISTS `message_read`;
DROP TABLE IF EXISTS `message`;
DROP TABLE IF EXISTS `chat_room_member`;
DROP TABLE IF EXISTS `chat_room`;
DROP TABLE IF EXISTS `calendar`;
DROP TABLE IF EXISTS `building`;
DROP TABLE IF EXISTS `streak`;
DROP TABLE IF EXISTS `review`;
DROP TABLE IF EXISTS `learning_history`;
DROP TABLE IF EXISTS `enrollment`;
DROP TABLE IF EXISTS `post_content`;
DROP TABLE IF EXISTS `post_like`;
DROP TABLE IF EXISTS `post_image`;   -- 구버전 호환 DROP
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `post`;
DROP TABLE IF EXISTS `chapter`;
DROP TABLE IF EXISTS `lecture`;
DROP TABLE IF EXISTS `user`;

-- =====================================================================
--  1. user
-- =====================================================================
CREATE TABLE `user` (
                        `id`                BIGINT       NOT NULL AUTO_INCREMENT,
                        `email`             VARCHAR(255) NULL,
                        `password`          VARCHAR(255) NULL,
                        `name`              VARCHAR(50)  NOT NULL,
                        `nickname`          VARCHAR(30)  NOT NULL,
                        `profile_image_url` VARCHAR(500) NULL,
                        `role`              ENUM('STUDENT','TEACHER','ADMIN')                              NOT NULL DEFAULT 'STUDENT',
                        `status`            ENUM('ACTIVE','PENDING','REJECTED','BANNED','BLACK','DELETED') NOT NULL DEFAULT 'ACTIVE',
                        `category`          ENUM('FITNESS','STUDY','COOK','BEAUTY','ART')                  NULL,
                        `proof`             VARCHAR(500) NULL,
                        `point`             INT          NOT NULL DEFAULT 0,
                        `do_not_disturb`    BOOLEAN      NOT NULL DEFAULT FALSE,
                        `suspension_count`  INT          NOT NULL DEFAULT 0,
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
                           `id`                   BIGINT       NOT NULL AUTO_INCREMENT,
                           `teacher_id`           BIGINT       NOT NULL,
                           `title`                VARCHAR(200) NOT NULL,
                           `description`          TEXT         NULL,
                           `thumbnail_url`        VARCHAR(500) NULL,
                           `category`             ENUM('FITNESS','STUDY','COOK','BEAUTY','ART') NOT NULL,
                           `status`               ENUM('WAITING','ACTIVE','HOLD','DELETED')     NOT NULL DEFAULT 'WAITING',
                           `completed_user_count` INT          NOT NULL DEFAULT 0,
                           `created_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           `deleted_at`           DATETIME     NULL,
                           PRIMARY KEY (`id`),
                           KEY `idx_lecture_teacher`    (`teacher_id`),
                           KEY `idx_lecture_status`     (`status`),
                           KEY `idx_lecture_category`   (`category`),
                           KEY `idx_lecture_deleted_at` (`deleted_at`),
                           FULLTEXT KEY `ft_lecture` (`title`, `description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  3. chapter
--  ※ video_status 제거
-- =====================================================================
CREATE TABLE `chapter` (
                           `id`                BIGINT       NOT NULL AUTO_INCREMENT,
                           `lecture_id`        BIGINT       NOT NULL,
                           `title`             VARCHAR(200) NOT NULL,
                           `order_no`          INT          NOT NULL DEFAULT 0,
                           `video_url`         VARCHAR(500) NULL,
                           `video_size_bytes`  BIGINT       NULL,
                           `duration_sec`      INT          NULL,
                           `original_filename` VARCHAR(255) NULL,
                           `thumbnail_url`     VARCHAR(500) NULL,
                           `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           `deleted_at`        DATETIME     NULL,
                           PRIMARY KEY (`id`),
                           KEY `idx_chapter_lecture` (`lecture_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  4. post
--  ※ content(TEXT) 제거 → post_content 테이블로 이동
--  ※ post_like → INT DEFAULT 0 유지 (비정규화 캐시 컬럼)
--  ※ category HEALTH → FITNESS
-- =====================================================================
CREATE TABLE `post` (
                        `id`            BIGINT       NOT NULL AUTO_INCREMENT,
                        `user_id`       BIGINT       NOT NULL,
                        `title`         VARCHAR(200) NOT NULL,
                        `post_like`     INT          NOT NULL DEFAULT 0,
                        `view_count`    INT          NOT NULL DEFAULT 0,
                        `category`      ENUM('FITNESS','STUDY','COOK','BEAUTY','ART','FREE') NOT NULL,
                        `thumbnail_url` VARCHAR(500) NULL,
                        `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        `deleted_at`    DATETIME     NULL,
                        PRIMARY KEY (`id`),
                        KEY `idx_post_user`       (`user_id`),
                        KEY `idx_post_category`   (`category`),
                        KEY `idx_post_created_at` (`created_at`),
                        FULLTEXT KEY `ft_post` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  4-1. post_content (post_image 대체 + 텍스트 블록 통합)
--  ※ type: TEXT = 텍스트 블록, IMAGE = S3/CloudFront URL
--  ※ order_no 기준으로 글·사진 순서 관리
-- =====================================================================
CREATE TABLE `post_content` (
                                `id`         BIGINT                   NOT NULL AUTO_INCREMENT,
                                `post_id`    BIGINT                   NOT NULL,
                                `order_no`   TINYINT                  NOT NULL DEFAULT 0,
                                `type`       ENUM('TEXT','IMAGE')      NOT NULL,
                                `image_url`  varchar(500)             NULL,
                                `content`    TEXT                     NULL,
                                `created_at` DATETIME                 NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                KEY `idx_post_content_post` (`post_id`, `order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  4-2. post_like
-- =====================================================================
CREATE TABLE `post_like` (
                             `id`         BIGINT   NOT NULL AUTO_INCREMENT,
                             `post_id`    BIGINT   NOT NULL,
                             `user_id`    BIGINT   NOT NULL,
                             `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uq_post_like` (`post_id`, `user_id`),
                             KEY `idx_post_like_post` (`post_id`),
                             KEY `idx_post_like_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  5. comment
--  ※ deleted_at 추가 (소프트딜리트)
-- =====================================================================
CREATE TABLE `comment` (
                           `id`         BIGINT       NOT NULL AUTO_INCREMENT,
                           `post_id`    BIGINT       NOT NULL,
                           `user_id`    BIGINT       NOT NULL,
                           `parent_id`  BIGINT       NULL,
                           `content`    VARCHAR(500) NOT NULL,
                           `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           `deleted_at` DATETIME     NULL,
                           PRIMARY KEY (`id`),
                           KEY `idx_comment_post`   (`post_id`),
                           KEY `idx_comment_user`   (`user_id`),
                           KEY `idx_comment_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  6. enrollment
-- =====================================================================
CREATE TABLE `enrollment` (
                              `id`              BIGINT   NOT NULL AUTO_INCREMENT,
                              `user_id`         BIGINT   NOT NULL,
                              `lecture_id`      BIGINT   NOT NULL,
                              `total_progress`  INT      NOT NULL DEFAULT 0,
                              `completed_count` INT      NOT NULL DEFAULT 0,
                              `enrolled_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `is_completed`    BOOLEAN  NOT NULL DEFAULT FALSE,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uq_enrollment_user_lecture` (`user_id`, `lecture_id`),
                              KEY `idx_enrollment_lecture` (`lecture_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  7. learning_history
--  ※ last_watched_at DATE 추가
-- =====================================================================
CREATE TABLE `learning_history` (
                                    `id`                BIGINT   NOT NULL AUTO_INCREMENT,
                                    `user_id`           BIGINT   NOT NULL,
                                    `lecture_id`        BIGINT   NOT NULL,
                                    `chapter_id`        BIGINT   NOT NULL,
                                    `watched_seconds`   INT      NOT NULL DEFAULT 0,   -- 최대 시청 위치 (진척도·잔디 기준)
                                    `last_position_sec` INT      NOT NULL DEFAULT 0,   -- resume 전용
                                    `progress_rate`     INT      NOT NULL DEFAULT 0,   -- watched_seconds / duration_sec * 100
                                    `is_completed`      BOOLEAN  NOT NULL DEFAULT FALSE,
                                    `last_watched_at`   DATE     NULL,                 -- 잔디/캘린더 오늘 챕터 조회용
                                    `version`           BIGINT   NOT NULL DEFAULT 0,   -- 낙관적 락
                                    `created_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    `updated_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`),
                                    KEY `idx_lh_user`    (`user_id`),
                                    KEY `idx_lh_lecture` (`lecture_id`),
                                    KEY `idx_lh_chapter` (`chapter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  8. review
-- =====================================================================
CREATE TABLE `review` (
                          `id`         BIGINT   NOT NULL AUTO_INCREMENT,
                          `user_id`    BIGINT   NOT NULL,
                          `lecture_id` BIGINT   NOT NULL,
                          `rating`     TINYINT  NOT NULL,
                          `content`    TEXT     NULL,
                          `status`     ENUM('ACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE',
                          `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `deleted_at` DATETIME NULL,
                          PRIMARY KEY (`id`),
                          KEY `idx_review_lecture` (`lecture_id`),
                          KEY `idx_review_user`    (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  9. streak
--  ※ chapter_id 제거
--  ※ daily_watched_seconds, level 추가
--  ※ (user_id, streak_date) UNIQUE
-- =====================================================================
CREATE TABLE `streak` (
                          `id`                   BIGINT   NOT NULL AUTO_INCREMENT,
                          `user_id`              BIGINT   NOT NULL,
                          `daily_watched_seconds`INT      NOT NULL DEFAULT 0,
                          `streak_date`          DATE     NOT NULL,
                          `level`                ENUM('LEVEL0','LEVEL1','LEVEL2','LEVEL3','LEVEL4') NOT NULL DEFAULT 'LEVEL0',
                          `created_at`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uq_streak_user_date` (`user_id`, `streak_date`),
                          KEY `idx_streak_user_date` (`user_id`, `streak_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  10. building
-- =====================================================================
CREATE TABLE `building` (
                            `id`         BIGINT   NOT NULL AUTO_INCREMENT,
                            `user_id`    BIGINT   NOT NULL,
                            `category`   ENUM('FITNESS','STUDY','COOK','BEAUTY','ART') NOT NULL,
                            `position`   INT      NOT NULL,
                            `level`      ENUM('LEVEL1','LEVEL2','LEVEL3') NOT NULL DEFAULT 'LEVEL1',
                            `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            KEY `idx_building_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  11. calendar
-- =====================================================================
CREATE TABLE `calendar` (
                            `id`           BIGINT       NOT NULL AUTO_INCREMENT,
                            `user_id`      BIGINT       NOT NULL,
                            `start`        DATE         NOT NULL,
                            `title`        VARCHAR(255) NOT NULL,
                            `end`          DATE         NULL,
                            `category`     ENUM('MEMO','TODO') NOT NULL DEFAULT 'MEMO',
                            `is_completed` BOOLEAN      NOT NULL DEFAULT FALSE,
                            `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            KEY `idx_calendar_user`          (`user_id`),
                            KEY `idx_calendar_user_category` (`user_id`, `category`),
                            KEY `idx_calendar_date`          (`start`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  12. chat_room
-- =====================================================================

CREATE TABLE `chat_room` (
                             `id`         BIGINT   NOT NULL AUTO_INCREMENT,
                             `title`      VARCHAR(20) NULL,
                             `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  13. chat_room_member
-- =====================================================================

CREATE TABLE `chat_room_member` (
                                    `id`        BIGINT   NOT NULL AUTO_INCREMENT,
                                    `room_id`   BIGINT   NOT NULL,
                                    `user_id`   BIGINT   NOT NULL,
                                    `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`),
                                    KEY `idx_chat_room_member_user` (`user_id`),
                                    UNIQUE KEY `uq_chat_room_member` (`room_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  14. message
-- =====================================================================
CREATE TABLE `message` (
                           `id`         BIGINT   NOT NULL AUTO_INCREMENT,
                           `room_id`    BIGINT   NOT NULL,
                           `sender_id`  BIGINT   NOT NULL,
                           `content`    TEXT     NOT NULL,
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`),
                           KEY `idx_message_room`   (`room_id`),
                           KEY `idx_message_sender` (`sender_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  14-1. message_read
-- =====================================================================
CREATE TABLE `message_read` (
                                `id`         BIGINT   NOT NULL AUTO_INCREMENT,
                                `room_id`    BIGINT   NOT NULL,
                                `message_id` BIGINT   NOT NULL,
                                `user_id`    BIGINT   NOT NULL, -- 메시지 읽을 사용자
                                `is_msg_read`BOOLEAN  NOT NULL DEFAULT FALSE,
                                `is_noti_read`BOOLEAN  NOT NULL DEFAULT FALSE,
                                `is_deleted`BOOLEAN  NOT NULL DEFAULT FALSE,
                                PRIMARY KEY (`id`),
    -- 중복 데이터 방지 (한 유저가 같은 메시지에 대해 레코드를 중복 생성하는 것 차단)
                                UNIQUE KEY `uq_chat_user_message` (`user_id`, `message_id`),
                                KEY `idx_message_room`   (`room_id`),
                                KEY `idx_message` (`message_id`),
                                KEY `idx_message_target_user` (`user_id`),

                                CONSTRAINT `fk_msg_read_room_id` FOREIGN KEY (`room_id`) REFERENCES `chat_room` (`id`) ON DELETE CASCADE,
                                CONSTRAINT `fk_msg_read_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
                                CONSTRAINT `fk_msg_read_message_id` FOREIGN KEY (`message_id`) REFERENCES `message` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  14-2. message_announce
-- =====================================================================
CREATE TABLE `message_announce` (
                                    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
                                    `room_id`    BIGINT   NOT NULL,
                                    `target_id`  BIGINT   NOT NULL, -- 초대 대상자, 나가기 주체, 수정 주체
                                    `content`    TEXT     NOT NULL,
                                    `type`       ENUM('LEAVE', 'INVITE', 'RENAME') NOT NULL,
                                    `created_at` DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`),

                                    KEY `idx_announce_room_created` (`room_id`, `created_at` DESC),
                                    KEY `idx_announce_target_user`  (`target_id`),

                                    CONSTRAINT `fk_announce_room_id` FOREIGN KEY (`room_id`) REFERENCES `chat_room` (`id`) ON DELETE CASCADE,
                                    CONSTRAINT `fk_announce_target_id` FOREIGN KEY (`target_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  15. friend
-- =====================================================================
CREATE TABLE `friend` (
                          `id`           BIGINT   NOT NULL AUTO_INCREMENT,
                          `from_user_id` BIGINT   NOT NULL,
                          `to_user_id`   BIGINT   NOT NULL,
                          `status`       ENUM('SENT','FRIEND','BLOCK') NOT NULL DEFAULT 'SENT',
                          `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uq_friend_pair` (`from_user_id`, `to_user_id`),
                          KEY `idx_friend_to_user` (`to_user_id`),
                          KEY `idx_friend_status`  (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  16. guestbook
-- =====================================================================
CREATE TABLE `guestbook` (
                             `id`         BIGINT        NOT NULL AUTO_INCREMENT,
                             `writer_id`  BIGINT        NOT NULL,
                             `owner_id`   BIGINT        NOT NULL,
                             `content`    VARCHAR(1000) NOT NULL,
                             `is_read`    BOOLEAN       NOT NULL DEFAULT FALSE,
                             `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`),
                             KEY `idx_guestbook_owner`  (`owner_id`),
                             KEY `idx_guestbook_writer` (`writer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  17. notification
-- =====================================================================
CREATE TABLE `notification` (
                                `id`         BIGINT       NOT NULL AUTO_INCREMENT,
                                `user_id`    BIGINT       NULL,
                                `type`       ENUM('APPROVAL','REPORT','FRIEND_REQUEST','MESSAGE','GUESTBOOK','POST','CALENDAR') NOT NULL,
                                `ref_id`     BIGINT       NULL,
                                `message`    VARCHAR(500) NOT NULL,
                                `is_read`    BOOLEAN      NOT NULL DEFAULT FALSE,
                                `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                KEY `idx_notification_user` (`user_id`),
                                KEY `idx_notification_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  18. user_oauth
-- =====================================================================
CREATE TABLE `user_oauth` (
                              `id`          BIGINT       NOT NULL AUTO_INCREMENT,
                              `user_id`     BIGINT       NOT NULL,
                              `provider`    ENUM('KAKAO','GOOGLE','NAVER') NOT NULL,
                              `provider_id` VARCHAR(100) NULL,
                              `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uq_user_oauth_provider` (`provider`, `provider_id`),
                              KEY `idx_user_oauth_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  19. access_log
-- =====================================================================
CREATE TABLE `access_log` (
                              `id`          BIGINT       NOT NULL AUTO_INCREMENT,
                              `user_id`     BIGINT       NULL,
                              `ip`          VARCHAR(45)  NOT NULL,
                              `action`      ENUM('LOGIN','LOGOUT','FORBIDDEN') NOT NULL,
                              `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              KEY `idx_access_log_user_id`    (`user_id`),
                              KEY `idx_access_log_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  20. report
--  ※ status 제거, is_read 추가
-- =====================================================================
CREATE TABLE `report` (
                          `id`               BIGINT       NOT NULL AUTO_INCREMENT,
                          `reporter_user_id` BIGINT       NOT NULL,
                          `reported_user_id` BIGINT       NOT NULL,
                          `target_type`      ENUM('LECTURE', 'CHAPTER', 'POST', 'REVIEW', 'COMMENT', 'CHAT', 'PAGE') NOT NULL,
                          `target_id`        BIGINT       NOT NULL,
                          `target_path`      VARCHAR(500) NULL,
                          `reason`           ENUM('SPAM','ABUSE','INAPPROPRIATE','COPYRIGHT','OTHER') NOT NULL,
                          `detail`           TEXT         NULL,
                          `is_resolved`      BOOLEAN      NOT NULL DEFAULT FALSE,
                          `resolved_at`      DATETIME     NULL,
                          `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          KEY `idx_report_reporter`        (`reporter_user_id`),
                          KEY `idx_report_target_created`  (`target_type`, `target_id`, `created_at` DESC),
                          KEY `idx_report_is_read_created` (`is_resolved`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  21. inquiry
--  ※ status·updated_at 제거, is_answered 추가
-- =====================================================================
CREATE TABLE `inquiry` (
                           `id`          BIGINT       NOT NULL AUTO_INCREMENT,
                           `user_id`     BIGINT       NOT NULL,
                           `title`       VARCHAR(200) NOT NULL,
                           `content`     TEXT         NOT NULL,
                           `answer`      TEXT         NULL,
                           `is_answered` BOOLEAN      NOT NULL DEFAULT FALSE,
                           `answered_at` DATETIME     NULL,
                           `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`),
                           KEY `idx_inquiry_user`        (`user_id`),
                           KEY `idx_inquiry_is_answered` (`is_answered`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  22. admin_message (신규 - 관리자 우편함)
--  ※ sender   → SET NULL (발신 관리자 탈퇴 시 메시지 보존)
--  ※ recipient → CASCADE  (수신 관리자 탈퇴 시 메시지 삭제)
-- =====================================================================
CREATE TABLE `admin_notice` (
                                `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
                                `title`              VARCHAR(200) NOT NULL,
                                `content`            TEXT         NOT NULL,
                                `is_pinned`          BOOLEAN      NOT NULL DEFAULT FALSE,
                                `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  25. store
-- =====================================================================
CREATE TABLE `store` (
                         `id`         BIGINT       NOT NULL AUTO_INCREMENT,
                         `price`      BIGINT       NOT NULL,
                         `url`        VARCHAR(500) NOT NULL,
                         `name`       VARCHAR(500) NOT NULL,
                         `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`),
                         KEY `idx_store_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  25-1. order_history
-- =====================================================================
CREATE TABLE `order_history` (
                                 `id`         BIGINT   NOT NULL AUTO_INCREMENT,
                                 `user_id`    BIGINT   NOT NULL,
                                 `reason`     ENUM('COMPLETED', 'REVIEW', 'PROFILE', 'BUY', 'GUESTBOOK')  NULL,
                                 `type`       ENUM('GAINED','USED')              NULL,
                                 `amount`     BIGINT   NOT NULL,
                                 `item_id`    BIGINT   NULL,
                                 `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`),
                                 KEY `idx_order_user_created` (`user_id`, `created_at` DESC),
                                 CONSTRAINT `fk_order_history_user`   FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
                                 CONSTRAINT `fk_order_history_store` FOREIGN KEY (`item_id`) REFERENCES `store` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  FOREIGN KEY CONSTRAINTS
-- =====================================================================
ALTER TABLE `lecture`
    ADD CONSTRAINT `fk_lecture_teacher`
        FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`);

ALTER TABLE `chapter`
    ADD CONSTRAINT `fk_chapter_lecture`
        FOREIGN KEY (`lecture_id`) REFERENCES `lecture` (`id`) ON DELETE CASCADE;

ALTER TABLE `post`
    ADD CONSTRAINT `fk_post_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `post_content`
    ADD CONSTRAINT `fk_post_content_post`
        FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE;

ALTER TABLE `post_like`
    ADD CONSTRAINT `fk_post_like_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
    ADD CONSTRAINT `fk_post_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `comment`
    ADD CONSTRAINT `fk_comment_post`   FOREIGN KEY (`post_id`)   REFERENCES `post` (`id`)    ON DELETE CASCADE,
    ADD CONSTRAINT `fk_comment_user`   FOREIGN KEY (`user_id`)   REFERENCES `user` (`id`),
    ADD CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE;

ALTER TABLE `enrollment`
    ADD CONSTRAINT `fk_enrollment_user`    FOREIGN KEY (`user_id`)    REFERENCES `user` (`id`),
    ADD CONSTRAINT `fk_enrollment_lecture` FOREIGN KEY (`lecture_id`) REFERENCES `lecture` (`id`) ON DELETE CASCADE;

ALTER TABLE `learning_history`
    ADD CONSTRAINT `fk_lh_user`    FOREIGN KEY (`user_id`)    REFERENCES `user` (`id`),
    ADD CONSTRAINT `fk_lh_lecture` FOREIGN KEY (`lecture_id`) REFERENCES `lecture` (`id`) ON DELETE CASCADE,
    ADD CONSTRAINT `fk_lh_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`)  ON DELETE CASCADE;

ALTER TABLE `review`
    ADD CONSTRAINT `fk_review_user`    FOREIGN KEY (`user_id`)    REFERENCES `user` (`id`),
    ADD CONSTRAINT `fk_review_lecture` FOREIGN KEY (`lecture_id`) REFERENCES `lecture` (`id`) ON DELETE CASCADE;

ALTER TABLE `streak`
    ADD CONSTRAINT `fk_streak_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `building`
    ADD CONSTRAINT `fk_building_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `calendar`
    ADD CONSTRAINT `fk_calendar_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `chat_room_member`
    ADD CONSTRAINT `fk_crm_room` FOREIGN KEY (`room_id`) REFERENCES `chat_room` (`id`) ON DELETE CASCADE,
    ADD CONSTRAINT `fk_crm_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `message`
    ADD CONSTRAINT `fk_message_room`   FOREIGN KEY (`room_id`)   REFERENCES `chat_room` (`id`) ON DELETE CASCADE,
    ADD CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`);

ALTER TABLE `friend`
    ADD CONSTRAINT `fk_friend_from` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`),
    ADD CONSTRAINT `fk_friend_to`   FOREIGN KEY (`to_user_id`)   REFERENCES `user` (`id`);

ALTER TABLE `guestbook`
    ADD CONSTRAINT `fk_guestbook_writer` FOREIGN KEY (`writer_id`) REFERENCES `user` (`id`),
    ADD CONSTRAINT `fk_guestbook_owner`  FOREIGN KEY (`owner_id`)  REFERENCES `user` (`id`);

ALTER TABLE `notification`
    ADD CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;

ALTER TABLE `user_oauth`
    ADD CONSTRAINT `fk_user_oauth_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

ALTER TABLE `access_log`
    ADD CONSTRAINT `fk_access_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;

ALTER TABLE `report`
    ADD CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_user_id`) REFERENCES `user` (`id`);

ALTER TABLE `inquiry`
    ADD CONSTRAINT `fk_inquiry_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
--  END OF SCHEMA
--  tables : 26 (suspension_log DROP / post_image DROP / post_content ADD / admin_message ADD)
-- =====================================================================
