-- =====================================================================
-- 오늘의 운세 기능에서 사용하는 테이블을 생성합니다.
-- fortunes: 운세 원본 366개를 보관합니다.
-- user_fortune_logs: 사용자가 날짜별로 뽑은 운세를 기록합니다.
-- =====================================================================


-- =====================================================================
-- 운세 원본 데이터를 보관하는 마스터 테이블입니다.
-- =====================================================================
CREATE TABLE `fortunes` (

    -- 운세를 구분하는 기본키입니다.
    -- 다음 단계에서 1부터 366까지 명시적으로 저장합니다.
                            `id` BIGINT NOT NULL,

    -- 사용자에게 보여줄 운세 문구입니다.
                            `content` VARCHAR(255) NOT NULL,

    -- 운세의 긍정, 중립, 부정 성격을 구분합니다.
    -- API 응답과 통계 및 운세 비율 조절에 사용합니다.
                            `tone` ENUM('GOOD', 'NEUTRAL', 'BAD') NOT NULL,

    -- 운세 데이터가 최초 생성된 시간을 저장합니다.
                            `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    -- 운세 데이터가 마지막으로 수정된 시간을 저장합니다.
                            `updated_at` DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    -- id 컬럼을 fortunes 테이블의 기본키로 지정합니다.
                            PRIMARY KEY (`id`)

-- 프로젝트의 기존 MySQL 테이블 설정과 문자 인코딩을 동일하게 적용합니다.
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


-- =====================================================================
-- 사용자가 날짜별로 뽑은 운세를 저장하는 기록 테이블입니다.
-- =====================================================================
CREATE TABLE `user_fortune_logs` (

    -- 운세 뽑기 기록을 구분하는 기본키입니다.
                                     `id` BIGINT NOT NULL AUTO_INCREMENT,

    -- 운세를 뽑은 사용자의 ID입니다.
                                     `user_id` BIGINT NOT NULL,

    -- 사용자가 뽑은 운세의 ID입니다.
                                     `fortune_id` BIGINT NOT NULL,

    -- 사용자가 운세를 뽑은 KST 기준 날짜입니다.
                                     `drawn_date` DATE NOT NULL,

    -- 운세 기록이 최초 생성된 시간을 저장합니다.
                                     `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    -- id 컬럼을 기록 테이블의 기본키로 지정합니다.
                                     PRIMARY KEY (`id`),

    -- 한 사용자가 같은 날짜에 운세 기록을 두 개 만들지 못하게 합니다.
    -- 같은 날 재요청하면 이 행을 조회하여 동일한 운세를 반환합니다.
                                     UNIQUE KEY `uq_user_fortune_logs_user_date`
                                         (`user_id`, `drawn_date`),

    -- 특정 운세가 몇 번 선택됐는지 조회할 때 사용할 인덱스입니다.
                                     KEY `idx_user_fortune_logs_fortune`
                                         (`fortune_id`),

    -- 특정 날짜의 전체 운세 기록을 조회할 때 사용할 인덱스입니다.
                                     KEY `idx_user_fortune_logs_drawn_date`
                                         (`drawn_date`),

    -- 사용자가 삭제되면 해당 사용자의 운세 기록도 함께 삭제합니다.
                                     CONSTRAINT `fk_user_fortune_logs_user`
                                         FOREIGN KEY (`user_id`)
                                             REFERENCES `user` (`id`)
                                             ON DELETE CASCADE,

    -- 사용 중인 운세가 임의로 삭제되지 않도록 제한합니다.
                                     CONSTRAINT `fk_user_fortune_logs_fortune`
                                         FOREIGN KEY (`fortune_id`)
                                             REFERENCES `fortunes` (`id`)
                                             ON DELETE RESTRICT

-- 프로젝트의 기존 MySQL 테이블 설정과 문자 인코딩을 동일하게 적용합니다.
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;