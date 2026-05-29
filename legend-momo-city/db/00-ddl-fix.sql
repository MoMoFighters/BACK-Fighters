-- =====================================================================
--  MoMo City - DDL 수정사항 (스키마 패치)  ★ 시드 적재 "전" 반드시 실행 ★
--  대상 DB : momo (MySQL)
-- ---------------------------------------------------------------------
--  [이 파일이 왜 필요한가]
--   테이블 생성용 DDL에는 아래 2가지 "수정사항"이 빠져 있었다.
--   이게 적용 안 된 DB에 시드(seed-dummy.sql 등)를 넣으면 오류가 난다.
--     수정 ① 카테고리 ENUM : 'HEALTH' -> 'FITNESS'  (user / lecture / building)
--     수정 ② user.email   : NOT NULL -> NULL (널 허용)
--     수정 ③ report 잔재 컬럼 DROP : reporter_id / target_nickname / reason_detail
--            (ddl-auto:update 가 안 지운 옛 컬럼. 현재 엔티티는 reporter_user_id / detail 사용)
--   => 이 파일을 실행하면 어떤 시작 상태든 시드가 기대하는 스키마로 맞춰진다.
-- ---------------------------------------------------------------------
--  [실행 순서]  ※ 이 순서를 반드시 지킬 것
--   1) 테이블 생성   : 앱 1회 bootRun(Hibernate) 또는 제공된 테이블 생성 DDL 실행
--   2) 00-ddl-fix.sql (이 파일)  <-- 여기서 수정사항 적용
--   3) seed-dummy.sql           (기본 더미 236행)
--   4) seed-friend-test.sql     (friend BC 테스트용, 선택)
-- ---------------------------------------------------------------------
--  [주의]
--   - 반드시 "빈 테이블"(시드 넣기 전) 상태에서 실행할 것.
--     이미 category='HEALTH' 데이터가 있으면 ENUM 변경이 실패한다.
--   - 멱등(idempotent): 이미 FITNESS/NULL 상태여도 다시 실행해도 안전(변화 없음).
--   - email 은 VARCHAR(255) 로 맞춘다(기존 100이면 늘어남=안전, 255면 그대로).
-- =====================================================================

-- 수정 ① 카테고리 ENUM : HEALTH -> FITNESS (3개 테이블 전부 통일)
ALTER TABLE `user`     MODIFY `category` ENUM('FITNESS','STUDY','COOK','BEAUTY','ART') NULL;
ALTER TABLE `lecture`  MODIFY `category` ENUM('FITNESS','STUDY','COOK','BEAUTY','ART') NOT NULL;
ALTER TABLE `building` MODIFY `category` ENUM('FITNESS','STUDY','COOK','BEAUTY','ART') NOT NULL;

-- 수정 ② user.email : NOT NULL -> NULL (널 허용, UNIQUE 는 유지됨)
ALTER TABLE `user` MODIFY `email` VARCHAR(255) NULL;

-- ---------------------------------------------------------------------
-- 수정 ③ report 잔재(orphan) 컬럼 정리
--   배경 : ddl-auto:update 는 컬럼 "추가"만 하고 "삭제"는 안 한다.
--          report 엔티티가 reporter_id->reporter_user_id, reason_detail->detail 로
--          리팩토링되면서 옛 컬럼이 NOT NULL 인 채 테이블에 남았다.
--          현재 엔티티(ReportJpaEntity)가 매핑하지 않는 3개를 제거한다.
--   확인 : 코드 전체 검색 결과 reporter_id/target_nickname/reason_detail 사용처 없음(미사용 검증 완료).
--   주의 : Hibernate 로 새로 만든 DB 는 애초에 이 컬럼이 없을 수 있으므로,
--          information_schema 로 "존재할 때만" DROP 한다(멱등·안전).
-- ---------------------------------------------------------------------
SET @db := DATABASE();

-- (reporter_id 에는 옛 FK(fk_report_reporter)가 걸려있으므로 "FK 먼저" 제거해야 컬럼 DROP 가능.
--  FK 이름이 DB마다 다를 수 있어 information_schema 로 동적 조회 후 제거.)
SET @fk := (SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA=@db AND TABLE_NAME='report' AND COLUMN_NAME='reporter_id'
              AND REFERENCED_TABLE_NAME IS NOT NULL LIMIT 1);
SET @sql := IF(@fk IS NOT NULL, CONCAT('ALTER TABLE `report` DROP FOREIGN KEY `',@fk,'`'), 'DO 0');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS
                      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='report' AND COLUMN_NAME='reporter_id'),
               'ALTER TABLE `report` DROP COLUMN `reporter_id`', 'DO 0');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS
                      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='report' AND COLUMN_NAME='target_nickname'),
               'ALTER TABLE `report` DROP COLUMN `target_nickname`', 'DO 0');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS
                      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='report' AND COLUMN_NAME='reason_detail'),
               'ALTER TABLE `report` DROP COLUMN `reason_detail`', 'DO 0');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- =====================================================================
--  END  (적용 후 seed-dummy.sql 실행)
-- =====================================================================
