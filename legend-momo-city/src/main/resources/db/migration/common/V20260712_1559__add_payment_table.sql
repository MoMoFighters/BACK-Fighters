-- =====================================================================
--  payment table
-- =====================================================================
CREATE TABLE `payment` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT       NOT NULL,
    `payment_id`   VARCHAR(255) NOT NULL UNIQUE,
    `original_payment_id` VARCHAR(255) NULL ,
    `plan`         ENUM('BASIC','PLUS','PRO')      NOT NULL,
    `price`        BIGINT       NOT NULL,
    `status`       ENUM('PENDING','SUCCESS','FAILED','REFUND','CANCEL_FAILED') NOT NULL DEFAULT 'PENDING',
    `created_at`   DATETIME(6)  NOT NULL,
    `updated_at`   DATETIME(6)  NOT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_payment_payment_id` (`payment_id`),
    KEY `idx_payment_user_id` (`user_id`),
    KEY `idx_payment_status`  (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `payment`
    ADD CONSTRAINT `fk_payment_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
            ON DELETE CASCADE;
           
