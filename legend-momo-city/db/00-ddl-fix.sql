-- =====================================================================
--  MoMo City - DDL 수정사항 (스키마 패치)  ★ 시드 적재 "전" 반드시 실행 ★
--  대상 DB : momo (MySQL)
-- ---------------------------------------------------------------------
--  [이 파일이 왜 필요한가]
--   테이블 생성용 DDL에는 아래 2가지 "수정사항"이 빠져 있었다.
--   이게 적용 안 된 DB에 시드(seed-dummy.sql 등)를 넣으면 오류가 난다.
--     수정 ① 카테고리 ENUM : 'HEALTH' -> 'FITNESS'  (user / lecture / building)
--     수정 ② user.email   : NOT NULL -> NULL (널 허용)
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

-- =====================================================================
--  END  (적용 후 seed-dummy.sql 실행)
-- =====================================================================
