-- =====================================================================
--  MoMo City - Database Schema (DDL)   [ver 3.5 - ERDCloud 반영본]
--  Engine : InnoDB / Charset : utf8mb4 / Collation : utf8mb4_unicode_ci
-- ---------------------------------------------------------------------
--  ★★ 용도: ERD 설계 문서(ERDCloud 업로드)용 참조 DDL 입니다. ★★
--      - 로컬 DB 생성/실행은 이 파일이 아니라 "앱 부팅(Hibernate) + seed-dummy.sql" 로 합니다.
--      - 설계 문서라 error_log / @Version 등 코드측 구현 컬럼은 의도적으로 제외.
-- ---------------------------------------------------------------------
--  ver3.4 -> ver3.5 변경 이력
--   [1] category ENUM : 'HEALTH' -> 'FITNESS'   (user / lecture / building)
--   [2] user.email    : NOT NULL -> NULL (널 허용, OAuth 가입 대응)
--   [3] report 테이블 재설계 (엔티티 기준 정렬)
--       - 제거: reporter_id, target_nickname, reason_detail (사용 안 함)
--       - 변경/추가: reporter_user_id, detail, reported_at, handled_at, handler_admin_id, updated_at
--       - FK : reporter_user_id -> user, handler_admin_id -> user
--  ※ error_log / @Version 등 코드측(Hibernate) 구현 세부는 ERD 설계문서에서 제외.
-- ---------------------------------------------------------------------
--  user.status 모델:
--   ACTIVE 정상 / PENDING 강사승인대기 / REJECTED 강사거절 / BANNED 정지 / BLACK 영구정지 / DELETED 탈퇴(soft)
--  DELETE 정책: user=SOFT(deleted_at) / lecture=HARD+하위CASCADE
-- =====================================================================

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

-- =====================================================================
--  1. user
-- =====================================================================
CREATE TABLE `user` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT,
    `email`             VARCHAR(100) NULL,                                              -- [v3.5] NOT NULL -> NULL
    `password`          VARCHAR(255) NULL,
    `name`              VARCHAR(50)  NOT NULL,
    `nickname`          VARCHAR(30)  NOT NULL,
    `birth`             DATE         NULL,
    `profile_image_url` VARCHAR(500) NULL,
    `role`              ENUM('STUDENT','TEACHER','ADMIN')                              NOT NULL DEFAULT 'STUDENT',
    `status`            ENUM('ACTIVE','PENDING','REJECTED','BANNED','BLACK','DELETED') NOT NULL DEFAULT 'ACTIVE',
    `category`          ENUM('FITNESS','STUDY','COOK','BEAUTY','ART')                  NULL,  -- [v3.5] HEALTH -> FITNESS
    `proof`             VARCHAR(500) NULL,
    `point`             INT          NOT NULL DEFAULT 0,
    `is_paid`           BOOLEAN      NOT NULL DEFAULT FALSE,
    `do_not_disturb`    BOOLEAN      NOT NULL DEFAULT FALSE,
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
    `category`             ENUM('FITNESS','STUDY','COOK','BEAUTY','ART')  NOT NULL,  -- [v3.5] HEALTH -> FITNESS
    `status`               ENUM('WAITING','ACTIVE','HOLD','DELETED')      NOT NULL DEFAULT 'WAITING',
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
-- =====================================================================
CREATE TABLE `chapter` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT,
    `lecture_id`        BIGINT       NOT NULL,
    `title`             VARCHAR(200) NOT NULL,
    `order_no`          INT          NOT NULL DEFAULT 0,
    `video_url`         VARCHAR(500) NULL,
    `video_size_bytes`  BIGINT       NULL,
    `duration_sec`      INT          NULL,
    `video_status`      ENUM('UPLOADING','ENCODING','READY','FAILED') NOT NULL DEFAULT 'UPLOADING',
    `original_filename` VARCHAR(255) NULL,
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_chapter_lecture` (`lecture_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  4. post
-- =====================================================================
CREATE TABLE `post` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL,
    `type`       ENUM('NOTICE','FREE','QNA') NOT NULL DEFAULT 'FREE',
    `title`      VARCHAR(200) NOT NULL,
    `content`    TEXT         NULL,
    `is_pinned`  BOOLEAN      NOT NULL DEFAULT FALSE,
    `view_count` INT          NOT NULL DEFAULT 0,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_post_user`       (`user_id`),
    KEY `idx_post_type`       (`type`),
    KEY `idx_post_created_at` (`created_at`),
    FULLTEXT KEY `ft_post` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  5. comment
-- =====================================================================
CREATE TABLE `comment` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `post_id`    BIGINT       NOT NULL,
    `user_id`    BIGINT       NOT NULL,
    `parent_id`  BIGINT       NULL,
    `content`    VARCHAR(500) NOT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_comment_post`   (`post_id`),
    KEY `idx_comment_user`   (`user_id`),
    KEY `idx_comment_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  6. post_image
-- =====================================================================
CREATE TABLE `post_image` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `post_id`    BIGINT       NOT NULL,
    `image_url`  VARCHAR(500) NOT NULL,
    `order_no`   TINYINT      NOT NULL DEFAULT 0,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_post_image_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  7. enrollment
-- =====================================================================
CREATE TABLE `enrollment` (
    `id`              BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT   NOT NULL,
    `lecture_id`      BIGINT   NOT NULL,
    `total_progress`  INT      NOT NULL DEFAULT 0,
    `completed_count` INT      NOT NULL DEFAULT 0,
    `enrolled_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_enrollment_lecture` (`lecture_id`),
    UNIQUE KEY `uq_enrollment_user_lecture` (`user_id`, `lecture_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  8. learning_history
-- =====================================================================
CREATE TABLE `learning_history` (
    `id`                BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`           BIGINT   NOT NULL,
    `lecture_id`        BIGINT   NOT NULL,
    `chapter_id`        BIGINT   NOT NULL,
    `watched_seconds`   INT      NOT NULL DEFAULT 0,
    `is_completed`      BOOLEAN  NOT NULL DEFAULT FALSE,
    `created_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `last_position_sec` INT      NOT NULL DEFAULT 0,
    `progress_rate`     INT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_lh_user`    (`user_id`),
    KEY `idx_lh_lecture` (`lecture_id`),
    KEY `idx_lh_chapter` (`chapter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  9. review
-- =====================================================================
CREATE TABLE `review` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT   NOT NULL,
    `lecture_id` BIGINT   NOT NULL,
    `rating`     TINYINT  NOT NULL,
    `content`    TEXT     NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_review_lecture` (`lecture_id`),
    KEY `idx_review_user`    (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  10. streak
-- =====================================================================
CREATE TABLE `streak` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT   NOT NULL,
    `chapter_id`  BIGINT   NOT NULL,
    `streak_date` DATE     NOT NULL,
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_streak_user_date` (`user_id`, `streak_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  11. building
-- =====================================================================
CREATE TABLE `building` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT   NOT NULL,
    `category`   ENUM('FITNESS','STUDY','COOK','BEAUTY','ART') NOT NULL,  -- [v3.5] HEALTH -> FITNESS
    `position`   INT      NOT NULL,
    `level`      TINYINT  NOT NULL DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_building_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  12. calendar
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
--  13. chat_room
-- =====================================================================
CREATE TABLE `chat_room` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  14. chat_room_member
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
--  15. message
-- =====================================================================
CREATE TABLE `message` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `room_id`    BIGINT   NOT NULL,
    `sender_id`  BIGINT   NOT NULL,
    `content`    TEXT     NOT NULL,
    `is_read`    BOOLEAN  NOT NULL DEFAULT FALSE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_message_room`   (`room_id`),
    KEY `idx_message_sender` (`sender_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  16. friend
-- =====================================================================
CREATE TABLE `friend` (
    `id`           BIGINT   NOT NULL AUTO_INCREMENT,
    `from_user_id` BIGINT   NOT NULL,
    `to_user_id`   BIGINT   NOT NULL,
    `status`       ENUM('SENT','FRIEND','BLOCK') NOT NULL DEFAULT 'SENT',
    `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_friend_to_user` (`to_user_id`),
    KEY `idx_friend_status`  (`status`),
    UNIQUE KEY `uq_friend_pair` (`from_user_id`, `to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  17. guestbook
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
--  18. notification
-- =====================================================================
CREATE TABLE `notification` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL,
    `type`       ENUM('NOTICE','APPROVAL','REPORT','FRIEND_REQUEST','MESSAGE','GUESTBOOK','ENROLLMENT') NOT NULL,
    `ref_id`     BIGINT       NULL,
    `message`    VARCHAR(500) NULL,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_notification_user` (`user_id`),
    KEY `idx_notification_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  19. user_oauth
-- =====================================================================
CREATE TABLE `user_oauth` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT       NOT NULL,
    `provider`    ENUM('LOCAL','KAKAO','GOOGLE') NOT NULL,
    `provider_id` VARCHAR(100) NULL,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_oauth_user_id` (`user_id`),
    UNIQUE KEY `uq_user_oauth_provider` (`provider`, `provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  20. verification_code
-- =====================================================================
CREATE TABLE `verification_code` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NULL,
    `email`      VARCHAR(100) NOT NULL,
    `code`       VARCHAR(10)  NOT NULL,
    `purpose`    ENUM('SIGNUP','PASSWORD_RESET','EMAIL_CHANGE') NOT NULL,
    `expires_at` DATETIME     NOT NULL,
    `used`       BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`id`),
    KEY `idx_verification_code_email`   (`email`),
    KEY `idx_verification_code_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  21. access_log
-- =====================================================================
CREATE TABLE `access_log` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NULL,
    `ip`         VARCHAR(45)  NOT NULL,
    `action`     VARCHAR(100) NOT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_access_log_user_id`    (`user_id`),
    KEY `idx_access_log_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  22. report   [v3.5 재설계]
--   ※ target 은 (target_type + target_id) 로 식별 (다형 참조라 FK 없음)
--   ※ reported_at = 신고 접수 시각(도메인) / created_at,updated_at = 행 감사 타임스탬프
-- =====================================================================
CREATE TABLE `report` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT,
    `reporter_user_id` BIGINT        NOT NULL,                                        -- [v3.5] reporter_id 대체
    `target_type`      ENUM('POST','COMMENT','USER','LECTURE')          NOT NULL,
    `target_id`        BIGINT        NOT NULL,
    `reason`           ENUM('SPAM','ABUSE','INAPPROPRIATE','ETC')       NOT NULL,
    `detail`           VARCHAR(1000) NULL,                                            -- [v3.5] reason_detail 대체
    `status`           ENUM('PENDING','REVIEWED','RESOLVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    `reported_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,              -- [v3.5] 신규
    `handled_at`       DATETIME      NULL,                                            -- [v3.5] 신규 (처리 시각)
    `handler_admin_id` BIGINT        NULL,                                            -- [v3.5] 신규 (처리 관리자)
    `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_report_reporter`    (`reporter_user_id`),
    KEY `idx_report_target`      (`target_type`, `target_id`),
    KEY `idx_report_status`      (`status`),
    KEY `idx_report_reported_at` (`reported_at`),
    KEY `idx_report_handler`     (`handler_admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  23. payment
-- =====================================================================
CREATE TABLE `payment` (
    `id`      BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT   NOT NULL,
    `amount`  INT      NOT NULL,
    `method`  ENUM('KAKAO','TOSS','CARD','FREE') NOT NULL,
    `paid_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_payment_user`    (`user_id`),
    KEY `idx_payment_paid_at` (`paid_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  24. inquiry
-- =====================================================================
CREATE TABLE `inquiry` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT       NOT NULL,
    `title`       VARCHAR(200) NOT NULL,
    `content`     TEXT         NOT NULL,
    `answer`      TEXT         NULL,
    `status`      ENUM('WAITING','ANSWERED','CLOSED') NOT NULL DEFAULT 'WAITING',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `answered_at` DATETIME     NULL,
    PRIMARY KEY (`id`),
    KEY `idx_inquiry_user`   (`user_id`),
    KEY `idx_inquiry_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  25. refresh_token (JWT 리프레시 토큰)
-- =====================================================================
CREATE TABLE `refresh_token` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL,
    `token`      VARCHAR(255) NOT NULL,
    `expires_at` DATETIME     NOT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_refresh_token_user` (`user_id`),
    KEY `idx_refresh_token_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  FOREIGN KEY CONSTRAINTS (관계선 35개)  [v3.5: report FK 2개로 정리]
-- =====================================================================
-- ※ ERDCloud 호환성: FK 1개당 ALTER 1개로 분리(한 ALTER 에 여러 제약 묶으면 일부 관계선 누락됨).
ALTER TABLE `lecture` ADD CONSTRAINT `fk_lecture_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`);
ALTER TABLE `chapter` ADD CONSTRAINT `fk_chapter_lecture` FOREIGN KEY (`lecture_id`) REFERENCES `lecture` (`id`) ON DELETE CASCADE;
ALTER TABLE `post` ADD CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `comment` ADD CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE;
ALTER TABLE `comment` ADD CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `comment` ADD CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE;
ALTER TABLE `post_image` ADD CONSTRAINT `fk_post_image_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE;
ALTER TABLE `enrollment` ADD CONSTRAINT `fk_enrollment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `enrollment` ADD CONSTRAINT `fk_enrollment_lecture` FOREIGN KEY (`lecture_id`) REFERENCES `lecture` (`id`) ON DELETE CASCADE;
ALTER TABLE `learning_history` ADD CONSTRAINT `fk_lh_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `learning_history` ADD CONSTRAINT `fk_lh_lecture` FOREIGN KEY (`lecture_id`) REFERENCES `lecture` (`id`) ON DELETE CASCADE;
ALTER TABLE `learning_history` ADD CONSTRAINT `fk_lh_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`) ON DELETE CASCADE;
ALTER TABLE `review` ADD CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `review` ADD CONSTRAINT `fk_review_lecture` FOREIGN KEY (`lecture_id`) REFERENCES `lecture` (`id`) ON DELETE CASCADE;
ALTER TABLE `streak` ADD CONSTRAINT `fk_streak_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `streak` ADD CONSTRAINT `fk_streak_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`) ON DELETE CASCADE;
ALTER TABLE `building` ADD CONSTRAINT `fk_building_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `calendar` ADD CONSTRAINT `fk_calendar_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `chat_room_member` ADD CONSTRAINT `fk_crm_room` FOREIGN KEY (`room_id`) REFERENCES `chat_room` (`id`) ON DELETE CASCADE;
ALTER TABLE `chat_room_member` ADD CONSTRAINT `fk_crm_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `message` ADD CONSTRAINT `fk_message_room` FOREIGN KEY (`room_id`) REFERENCES `chat_room` (`id`) ON DELETE CASCADE;
ALTER TABLE `message` ADD CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`);
ALTER TABLE `friend` ADD CONSTRAINT `fk_friend_from` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`);
ALTER TABLE `friend` ADD CONSTRAINT `fk_friend_to` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`);
ALTER TABLE `guestbook` ADD CONSTRAINT `fk_guestbook_writer` FOREIGN KEY (`writer_id`) REFERENCES `user` (`id`);
ALTER TABLE `guestbook` ADD CONSTRAINT `fk_guestbook_owner` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`);
ALTER TABLE `notification` ADD CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `user_oauth` ADD CONSTRAINT `fk_user_oauth_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
ALTER TABLE `verification_code` ADD CONSTRAINT `fk_verification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
ALTER TABLE `access_log` ADD CONSTRAINT `fk_access_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;
ALTER TABLE `report` ADD CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_user_id`) REFERENCES `user` (`id`);
ALTER TABLE `report` ADD CONSTRAINT `fk_report_handler` FOREIGN KEY (`handler_admin_id`) REFERENCES `user` (`id`);
ALTER TABLE `payment` ADD CONSTRAINT `fk_payment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `inquiry` ADD CONSTRAINT `fk_inquiry_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `refresh_token`
    ADD CONSTRAINT `fk_refresh_token_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;
-- =====================================================================
--  END OF SCHEMA  (tables: 25 / foreign keys: 35)
-- =====================================================================
