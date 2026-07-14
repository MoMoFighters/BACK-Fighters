-- =====================================================================
--  열품타 (Study Timer) - 공부 랩(구간) 기록 테이블 추가
--  담당: 누(DBA)
--  경로: db/migration/common
--  -
--  솔로/그룹 통합 설계:
--  유저는 정책상 솔로/그룹 통틀어 활성 세션이 항상 1개뿐이므로, session_type처럼
--  별도 구분 컬럼 없이 user_id + room_id(nullable) 조합만으로 솔로/그룹을 모두 표현한다.
--  - room_id가 NULL이면 솔로 세션에서 발생한 랩 (session_id는 solo_session.id를 가리킴)
--  - room_id가 특정 방 id면 그 그룹방에서 발생한 랩 (session_id는 group_room_member.id를 가리킴)
--  -
--  session_id가 필요한 이유 :
--  user_id + room_id만으로는 "같은 유저가 같은 조건(솔로 또는 같은 방)에서 서로 다른 시점에
--  진행한 여러 세션"을 구분할 수 없다. 예를 들어 오늘 아침 솔로 세션(A)을 끝내고
--  오후에 새 솔로 세션(B)을 시작하면, room_id만으로 조회 시 A와 B의 랩이 뒤섞여 나온다.
--  session_id를 추가해 "이 랩이 정확히 어느 세션에 속하는지" 특정할 수 있게 한다.
--  -
--  현재는 솔로 세션(SoloCommandService)에서만 이 테이블에 기록하고,
--  그룹방(member.timer)에서의 기록은 이번 스프린트 범위에서 제외한다(추후 확장 예정).
-- =====================================================================

CREATE TABLE `study_lap` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT       NOT NULL,
    `room_id`     BIGINT       DEFAULT NULL COMMENT 'NULL이면 솔로 세션에서 발생한 랩, 값이 있으면 해당 그룹방에서 발생한 랩',
    `session_id`  BIGINT       NOT NULL COMMENT 'room_id가 NULL이면 solo_session.id, 값이 있으면 group_room_member.id',
    `started_at`  DATETIME(6)  NOT NULL COMMENT '이 랩(구간)이 시작된 시각',
    `ended_at`    DATETIME(6)  DEFAULT NULL COMMENT '이 랩이 종료된 시각, 진행 중이면 NULL',
    `seconds`     INT          DEFAULT NULL COMMENT '이 랩의 소요 시간(초), 종료 시에만 확정',
    `created_at`  DATETIME(6)  DEFAULT NULL,
    `updated_at`  DATETIME(6)  DEFAULT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_study_lap_user`    (`user_id`),
    KEY `idx_study_lap_room`    (`room_id`),
    KEY `idx_study_lap_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
