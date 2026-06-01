-- =====================================================================
--  MoMo City - 통합 시드 스크립트 (스키마 패치 + 리셋 + 더미 236행)  [LIVE]
--  대상 DB : momo (MySQL, utf8mb4)
-- =====================================================================
--  ★ 팀 DB 셋업은 이 파일 하나면 끝. 아래 3단계가 순서대로 자동 실행된다.
--    STEP 0  스키마 수정사항 적용 (category HEALTH->FITNESS / email NULL / report 잔재컬럼 정리)
--    STEP 1  전체 테이블 리셋(TRUNCATE)  → 멱등: 언제 재실행해도 깨끗하게 다시 채워짐
--    STEP 2  더미데이터 INSERT (26테이블 236행)
-- ---------------------------------------------------------------------
--  [실행 전제] 앱을 1회 bootRun 하여 Hibernate 가 전체 테이블(error_log 포함)을 생성한 상태.
--  [실행 방법] mysql -u root -p momo < seed-dummy.sql   (또는 워크벤치에서 전체 선택 실행)
--  [로그인]   전 계정 비밀번호 password123 (bcrypt 검증완료) / id=12 카카오는 email·password 널.
--  [시간값]   전부 NOW()/CURDATE() 상대값 → 항상 "방금까지 운영된" 데이터. birth(생년월일)만 고정.
--  [참고]     ERD 설계 문서(테이블 구조)는 schema-v3.5.sql 참조. 이 파일은 "데이터 적재" 전용.
-- =====================================================================

-- 대상 DB 선택 (워크벤치에서 스키마 미선택 시 1046 "No database selected" 방지)
USE `momo`;

-- =====================================================================
--  STEP 0. 스키마 수정사항 적용  (구 00-ddl-fix.sql 통합)
--   ① category ENUM 'HEALTH'->'FITNESS' (user/lecture/building)
--   ② user.email NOT NULL -> NULL
--   ③ report 잔재(orphan) 컬럼 정리 : reporter_id / target_nickname / reason_detail
--      (ddl-auto:update 가 안 지운 옛 컬럼. 현재 엔티티는 reporter_user_id / detail 사용)
--   ※ 멱등: 이미 적용된 상태여도 안전. 잔재 컬럼은 "존재할 때만" DROP.
-- =====================================================================
ALTER TABLE `user`     MODIFY `category` ENUM('FITNESS','STUDY','COOK','BEAUTY','ART') NULL;
ALTER TABLE `lecture`  MODIFY `category` ENUM('FITNESS','STUDY','COOK','BEAUTY','ART') NOT NULL;
ALTER TABLE `building` MODIFY `category` ENUM('FITNESS','STUDY','COOK','BEAUTY','ART') NOT NULL;
ALTER TABLE `user`     MODIFY `email`    VARCHAR(255) NULL;

SET @db := DATABASE();
-- reporter_id 에 걸린 옛 FK 먼저 제거(이름 동적 조회) 후 컬럼 DROP
SET @fk := (SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA=@db AND TABLE_NAME='report' AND COLUMN_NAME='reporter_id'
              AND REFERENCED_TABLE_NAME IS NOT NULL LIMIT 1);
SET @sql := IF(@fk IS NOT NULL, CONCAT('ALTER TABLE `report` DROP FOREIGN KEY `',@fk,'`'), 'DO 0');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql := IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='report' AND COLUMN_NAME='reporter_id'),
               'ALTER TABLE `report` DROP COLUMN `reporter_id`', 'DO 0');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql := IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='report' AND COLUMN_NAME='target_nickname'),
               'ALTER TABLE `report` DROP COLUMN `target_nickname`', 'DO 0');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql := IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='report' AND COLUMN_NAME='reason_detail'),
               'ALTER TABLE `report` DROP COLUMN `reason_detail`', 'DO 0');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- =====================================================================
--  STEP 1. 전체 테이블 리셋 (FK 끄고 TRUNCATE → 멱등성 확보)
-- =====================================================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE `access_log`;TRUNCATE `building`;TRUNCATE `calendar`;TRUNCATE `chapter`;TRUNCATE `chat_room`;TRUNCATE `chat_room_member`;TRUNCATE `comment`;TRUNCATE `enrollment`;TRUNCATE `error_log`;TRUNCATE `friend`;TRUNCATE `guestbook`;TRUNCATE `inquiry`;TRUNCATE `learning_history`;TRUNCATE `lecture`;TRUNCATE `message`;TRUNCATE `notification`;TRUNCATE `payment`;TRUNCATE `post`;TRUNCATE `post_image`;TRUNCATE `refresh_token`;TRUNCATE `report`;TRUNCATE `review`;TRUNCATE `streak`;TRUNCATE `user`;TRUNCATE `user_oauth`;TRUNCATE `verification_code`;
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
--  STEP 2. 더미데이터 INSERT (FK 부모→자식 순서)
-- =====================================================================

-- =====================================================================
--  1. user  (13명 - status ENUM 6종 전부 / 가입은 수개월~수일 전으로 분산)
-- =====================================================================
INSERT INTO `user`
  (`id`,`email`,`password`,`name`,`nickname`,`birth`,`profile_image_url`,`role`,`status`,`category`,`proof`,`point`,`is_paid`,`do_not_disturb`,`created_at`,`deleted_at`,`is_tempPWD`)
VALUES
  (1 ,'admin@momo.city'   ,'$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','관리자','momo_admin' ,'1990-01-01','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','ADMIN'  ,'ACTIVE'  ,NULL     ,NULL,5000,1,0,NOW() - INTERVAL 147 DAY,NULL,0),
  (2 ,'student1@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','김민수','minsu'      ,'1998-03-12','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE'  ,NULL     ,NULL,1200,1,0,NOW() - INTERVAL 108 DAY,NULL,0),
  (3 ,'student2@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','이지영','jiyoung'    ,'1999-07-05','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE'  ,NULL     ,NULL, 300,0,0,NOW() - INTERVAL 103 DAY,NULL,0),
  (4 ,'student3@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','박현우','hyunwoo'    ,'2000-11-23','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE'  ,NULL     ,NULL, 850,1,1,NOW() - INTERVAL 89 DAY ,NULL,0),
  (5 ,'student4@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','최서연','seoyeon'    ,'1997-05-30','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE'  ,NULL     ,NULL,  50,0,0,NOW() - INTERVAL 78 DAY ,NULL,0),
  (6 ,'teacher1@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','김강사','coach_kim'  ,'1988-09-09','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','TEACHER','ACTIVE'  ,'FITNESS','https://momo.city/proof/kim.pdf',2000,1,0,NOW() - INTERVAL 129 DAY,NULL,0),
  (7 ,'teacher2@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','박강사','chef_park'  ,'1985-12-01','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','TEACHER','ACTIVE'  ,'COOK'   ,'https://momo.city/proof/park.pdf',1700,1,0,NOW() - INTERVAL 124 DAY,NULL,0),
  (8 ,'pending@momo.city' ,'$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','정대기','wannabe'    ,'1995-04-18','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','TEACHER','PENDING' ,'STUDY'  ,'https://momo.city/proof/wannabe.pdf',0,0,0,NOW() - INTERVAL 9 DAY ,NULL,0),
  (9 ,'rejected@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','한거절','rejected_t' ,'1993-08-08','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','TEACHER','REJECTED','ART'    ,'https://momo.city/proof/rejected.pdf',0,0,0,NOW() - INTERVAL 19 DAY,NULL,0),
  (10,'banned@momo.city'  ,'$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','노정지','banned_user','1996-06-06','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','BANNED'  ,NULL     ,NULL,0,0,0,NOW() - INTERVAL 58 DAY,NULL,0),
  (11,'black@momo.city'   ,'$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','영구정','black_user' ,'1994-02-14','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','BLACK'   ,NULL     ,NULL,0,0,0,NOW() - INTERVAL 68 DAY,NULL,0),
  (12,NULL                ,NULL                                                          ,'카카오','kakao_user' ,'2001-10-10','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE'  ,NULL     ,NULL, 100,0,0,NOW() - INTERVAL 24 DAY,NULL,0),
  (13,'left@momo.city'    ,'$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','떠난이','left_user'  ,'1992-01-01','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','DELETED' ,NULL     ,NULL,0,0,0,NOW() - INTERVAL 118 DAY,NOW() - INTERVAL 14 DAY,0);

-- =====================================================================
--  2. lecture  (10개 - 개설은 수개월 전, WAITING 건만 최근)
-- =====================================================================
INSERT INTO `lecture`
  (`id`,`teacher_id`,`title`,`description`,`thumbnail_url`,`category`,`status`,`completed_user_count`,`created_at`)
VALUES
  (1 ,6,'아침 홈트 30일 챌린지','집에서 따라하는 전신 운동',NULL,'FITNESS','ACTIVE' ,42,NOW() - INTERVAL 117 DAY),
  (2 ,6,'코어 강화 필라테스'   ,'코어 집중 필라테스 클래스',NULL,'FITNESS','ACTIVE' ,18,NOW() - INTERVAL 113 DAY),
  (3 ,7,'집밥 마스터 클래스'   ,'기본부터 배우는 한식 집밥',NULL,'COOK'   ,'ACTIVE' ,30,NOW() - INTERVAL 108 DAY),
  (4 ,7,'베이킹 입문'          ,'홈베이킹 첫걸음'         ,NULL,'COOK'   ,'WAITING', 0,NOW() - INTERVAL 11 DAY),
  (5 ,6,'러닝 클래스'          ,'달리기 자세 교정'        ,NULL,'FITNESS','HOLD'   , 5,NOW() - INTERVAL 89 DAY),
  (6 ,7,'자바 기초 프로그래밍' ,'비전공자를 위한 자바'    ,NULL,'STUDY'  ,'ACTIVE' ,55,NOW() - INTERVAL 98 DAY),
  (7 ,6,'홈케어 메이크업'      ,'데일리 메이크업 노하우'  ,NULL,'BEAUTY' ,'ACTIVE' ,12,NOW() - INTERVAL 80 DAY),
  (8 ,7,'수채화 드로잉'        ,'취미로 시작하는 수채화'  ,NULL,'ART'    ,'ACTIVE' ,21,NOW() - INTERVAL 75 DAY),
  (9 ,6,'생활 영어 회화'       ,'여행에서 쓰는 영어'      ,NULL,'STUDY'  ,'ACTIVE' ,38,NOW() - INTERVAL 58 DAY),
  (10,7,'디저트 클래스'        ,'폐강된 디저트 강의'      ,NULL,'COOK'   ,'DELETED', 8,NOW() - INTERVAL 134 DAY);

-- =====================================================================
--  3. chapter  (10개)
-- =====================================================================
INSERT INTO `chapter`
  (`id`,`lecture_id`,`title`,`order_no`,`video_url`,`video_size_bytes`,`duration_sec`,`video_status`,`original_filename`,`created_at`)
VALUES
  (1 ,1,'1주차 - 준비운동'   ,1,'https://cdn.momo.city/v/1.mp4' ,104857600,600,'READY'    ,'warmup.mp4'  ,NOW() - INTERVAL 117 DAY),
  (2 ,1,'2주차 - 전신운동'   ,2,'https://cdn.momo.city/v/2.mp4' ,209715200,900,'READY'    ,'fullbody.mp4',NOW() - INTERVAL 116 DAY),
  (3 ,2,'필라테스 기초 호흡' ,1,'https://cdn.momo.city/v/3.mp4' ,157286400,720,'READY'    ,'pilates.mp4' ,NOW() - INTERVAL 113 DAY),
  (4 ,3,'기본 칼질 익히기'   ,1,'https://cdn.momo.city/v/4.mp4' ,125829120,540,'READY'    ,'knife.mp4'   ,NOW() - INTERVAL 108 DAY),
  (5 ,3,'국 끓이기'          ,2,'https://cdn.momo.city/v/5.mp4' ,138412032,660,'READY'    ,'soup.mp4'    ,NOW() - INTERVAL 107 DAY),
  (6 ,6,'변수와 타입'        ,1,'https://cdn.momo.city/v/6.mp4' ,178257920,780,'READY'    ,'var.mp4'     ,NOW() - INTERVAL 98 DAY),
  (7 ,6,'조건문과 반복문'    ,2,NULL                            ,NULL     ,NULL,'ENCODING' ,'loop.mp4'    ,NOW() - INTERVAL 2 DAY),
  (8 ,8,'색의 이해'          ,1,'https://cdn.momo.city/v/8.mp4' ,98566144 ,480,'READY'    ,'color.mp4'   ,NOW() - INTERVAL 75 DAY),
  (9 ,9,'인사 표현 익히기'   ,1,'https://cdn.momo.city/v/9.mp4' ,110100480,510,'READY'    ,'hello.mp4'   ,NOW() - INTERVAL 58 DAY),
  (10,1,'3주차 - 마무리 스트레칭',3,NULL                        ,NULL     ,NULL,'UPLOADING','stretch.mp4' ,NOW() - INTERVAL 35 MINUTE);

-- =====================================================================
--  4. post  (10개 - 최근 5일 내 활동, 최신글은 수시간 전)
-- =====================================================================
INSERT INTO `post`
  (`id`,`user_id`,`type`,`title`,`content`,`is_pinned`,`view_count`,`created_at`)
VALUES
  (1 ,1 ,'NOTICE','[공지] 서버 정기 점검 안내'  ,'금주 새벽 점검이 있습니다.',1,540,NOW() - INTERVAL 4 DAY),
  (2 ,2 ,'FREE'  ,'오늘 운동 완료!'            ,'홈트 2주차 클리어했어요',0,120,NOW() - INTERVAL 3 DAY),
  (3 ,3 ,'QNA'   ,'필라테스 호흡법 질문이요'    ,'들숨 날숨 타이밍이 헷갈려요',0,75,NOW() - INTERVAL 3 DAY + INTERVAL 2 HOUR),
  (4 ,4 ,'FREE'  ,'집밥 클래스 후기'           ,'된장국 성공했습니다',0,88,NOW() - INTERVAL 2 DAY),
  (5 ,5 ,'FREE'  ,'자바 스터디원 모집'         ,'주 2회 온라인 스터디 모집',0,60,NOW() - INTERVAL 2 DAY + INTERVAL 5 HOUR),
  (6 ,2 ,'QNA'   ,'영상이 재생이 안돼요'        ,'6강 조건문 영상이 안 나옵니다',0,33,NOW() - INTERVAL 28 HOUR),
  (7 ,6 ,'NOTICE','[강사공지] 홈트 강의 업데이트','3주차 영상 추가했습니다',0,210,NOW() - INTERVAL 25 HOUR),
  (8 ,3 ,'FREE'  ,'베이킹 실패담 ㅠㅠ'         ,'쿠키가 탔어요',0,95,NOW() - INTERVAL 22 HOUR),
  (9 ,4 ,'QNA'   ,'환불 어떻게 하나요'         ,'결제 취소 문의드립니다',0,40,NOW() - INTERVAL 6 HOUR),
  (10,5 ,'FREE'  ,'오운완 인증합니다'          ,'30일 챌린지 완주',0,150,NOW() - INTERVAL 3 HOUR);

-- =====================================================================
--  5. comment  (12개 - 11,12번은 신고 대상 / 11번은 방금 전 작성)
-- =====================================================================
INSERT INTO `comment`
  (`id`,`post_id`,`user_id`,`parent_id`,`content`,`created_at`)
VALUES
  (1 ,1 ,2 ,NULL,'공지 확인했습니다.',NOW() - INTERVAL 4 DAY + INTERVAL 1 HOUR),
  (2 ,2 ,3 ,NULL,'대단해요 화이팅!',NOW() - INTERVAL 3 DAY + INTERVAL 30 MINUTE),
  (3 ,2 ,4 ,NULL,'저도 오늘 했어요',NOW() - INTERVAL 3 DAY + INTERVAL 1 HOUR),
  (4 ,3 ,6 ,NULL,'코로 마시고 입으로 천천히 내쉬세요',NOW() - INTERVAL 3 DAY + INTERVAL 3 HOUR),
  (5 ,3 ,3 ,4   ,'아 감사합니다 강사님!',NOW() - INTERVAL 3 DAY + INTERVAL 4 HOUR),
  (6 ,5 ,2 ,NULL,'스터디 참여하고 싶어요',NOW() - INTERVAL 2 DAY + INTERVAL 6 HOUR),
  (7 ,6 ,6 ,NULL,'브라우저 캐시 삭제 후 재시도 부탁드려요',NOW() - INTERVAL 27 HOUR),
  (8 ,8 ,5 ,NULL,'ㅋㅋㅋ저도 그랬어요',NOW() - INTERVAL 21 HOUR),
  (9 ,9 ,1 ,NULL,'환불은 마이페이지 > 결제내역에서 가능합니다',NOW() - INTERVAL 5 HOUR),
  (10,10,3 ,NULL,'완주 멋져요!',NOW() - INTERVAL 2 HOUR),
  (11,2 ,10,NULL,'★★대출 문의는 여기로 010-xxxx-xxxx★★',NOW() - INTERVAL 17 MINUTE),
  (12,4 ,11,NULL,'광고 광고 광고 클릭하세요 http://spam.example',NOW() - INTERVAL 26 HOUR);

-- =====================================================================
--  6. post_image  (8개)
-- =====================================================================
INSERT INTO `post_image`
  (`id`,`post_id`,`image_url`,`order_no`,`created_at`)
VALUES
  (1,2 ,'https://cdn.momo.city/img/p2-1.jpg',0,NOW() - INTERVAL 3 DAY),
  (2,2 ,'https://cdn.momo.city/img/p2-2.jpg',1,NOW() - INTERVAL 3 DAY),
  (3,4 ,'https://cdn.momo.city/img/p4-1.jpg',0,NOW() - INTERVAL 2 DAY),
  (4,8 ,'https://cdn.momo.city/img/p8-1.jpg',0,NOW() - INTERVAL 22 HOUR),
  (5,10,'https://cdn.momo.city/img/p10-1.jpg',0,NOW() - INTERVAL 3 HOUR),
  (6,10,'https://cdn.momo.city/img/p10-2.jpg',1,NOW() - INTERVAL 3 HOUR),
  (7,1 ,'https://cdn.momo.city/img/p1-1.jpg',0,NOW() - INTERVAL 4 DAY),
  (8,7 ,'https://cdn.momo.city/img/p7-1.jpg',0,NOW() - INTERVAL 25 HOUR);

-- =====================================================================
--  7. enrollment  (10개 - 수강신청은 수주~수개월 전)
-- =====================================================================
INSERT INTO `enrollment`
  (`id`,`user_id`,`lecture_id`,`total_progress`,`completed_count`,`enrolled_at`)
VALUES
  (1 ,2,1,66,2,NOW() - INTERVAL 105 DAY),
  (2 ,2,3,40,2,NOW() - INTERVAL 104 DAY),
  (3 ,3,1,33,1,NOW() - INTERVAL 100 DAY),
  (4 ,3,2,20,0,NOW() - INTERVAL 99 DAY),
  (5 ,4,3,80,2,NOW() - INTERVAL 88 DAY),
  (6 ,5,6,50,1,NOW() - INTERVAL 76 DAY),
  (7 ,5,1,10,0,NOW() - INTERVAL 75 DAY),
  (8 ,4,8,25,0,NOW() - INTERVAL 73 DAY),
  (9 ,3,9,90,1,NOW() - INTERVAL 57 DAY),
  (10,2,6,15,0,NOW() - INTERVAL 54 DAY);

-- =====================================================================
--  8. learning_history  (10개 - 최근 학습 일부 포함)
-- =====================================================================
-- ※ version : JPA @Version(낙관적 락) 컬럼. 실제 스키마에 NOT NULL 로 존재(DDL엔 없음) -> 0 으로 초기화.
INSERT INTO `learning_history`
  (`id`,`user_id`,`lecture_id`,`chapter_id`,`watched_seconds`,`is_completed`,`last_position_sec`,`progress_rate`,`created_at`,`version`)
VALUES
  (1 ,2,1,1 ,600,1,600,100,NOW() - INTERVAL 104 DAY,0),
  (2 ,2,1,2 ,900,1,900,100,NOW() - INTERVAL 103 DAY,0),
  (3 ,3,1,1 ,600,1,600,100,NOW() - INTERVAL 99 DAY,0),
  (4 ,3,2,3 ,360,0,360, 50,NOW() - INTERVAL 97 DAY,0),
  (5 ,4,3,4 ,540,1,540,100,NOW() - INTERVAL 88 DAY,0),
  (6 ,4,3,5 ,300,0,300, 45,NOW() - INTERVAL 87 DAY,0),
  (7 ,5,6,6 ,780,1,780,100,NOW() - INTERVAL 76 DAY,0),
  (8 ,5,6,7 ,200,0,200, 25,NOW() - INTERVAL 2 DAY,0),
  (9 ,2,1,10,120,0,120, 20,NOW() - INTERVAL 90 MINUTE,0),
  (10,3,9,9 ,510,1,510,100,NOW() - INTERVAL 57 DAY,0);

-- =====================================================================
--  9. review  (8개 - rating 1~5)
-- =====================================================================
INSERT INTO `review`
  (`id`,`user_id`,`lecture_id`,`rating`,`content`,`created_at`)
VALUES
  (1,2,1,5,'운동 초보도 따라하기 좋아요',NOW() - INTERVAL 95 DAY),
  (2,3,1,4,'영상 화질이 좋네요',NOW() - INTERVAL 94 DAY),
  (3,3,2,5,'코어가 단단해졌어요',NOW() - INTERVAL 90 DAY),
  (4,4,3,4,'레시피가 자세해요',NOW() - INTERVAL 80 DAY),
  (5,5,6,3,'중간 난이도가 좀 빨라요',NOW() - INTERVAL 70 DAY),
  (6,2,3,5,'집밥 자신감 생김',NOW() - INTERVAL 68 DAY),
  (7,4,8,4,'취미로 딱 좋아요',NOW() - INTERVAL 50 DAY),
  (8,3,9,2,'내용이 기대보다 적어요',NOW() - INTERVAL 2 DAY);

-- =====================================================================
--  10. streak  (10개 - 최근 9일간의 출석 / streak_date 는 오늘 기준 DATE)
-- =====================================================================
INSERT INTO `streak`
  (`id`,`user_id`,`chapter_id`,`streak_date`,`created_at`)
VALUES
  (1 ,2,1 ,CURDATE() - INTERVAL 9 DAY,NOW() - INTERVAL 9 DAY),
  (2 ,2,2 ,CURDATE() - INTERVAL 8 DAY,NOW() - INTERVAL 8 DAY),
  (3 ,2,10,CURDATE() - INTERVAL 1 DAY,NOW() - INTERVAL 1 DAY),
  (4 ,3,1 ,CURDATE() - INTERVAL 9 DAY,NOW() - INTERVAL 9 DAY),
  (5 ,3,3 ,CURDATE() - INTERVAL 7 DAY,NOW() - INTERVAL 7 DAY),
  (6 ,4,4 ,CURDATE() - INTERVAL 6 DAY,NOW() - INTERVAL 6 DAY),
  (7 ,4,5 ,CURDATE() - INTERVAL 5 DAY,NOW() - INTERVAL 5 DAY),
  (8 ,5,6 ,CURDATE() - INTERVAL 3 DAY,NOW() - INTERVAL 3 DAY),
  (9 ,5,7 ,CURDATE() - INTERVAL 2 DAY,NOW() - INTERVAL 2 DAY),
  (10,2,1 ,CURDATE()                 ,NOW() - INTERVAL 3 HOUR);

-- =====================================================================
--  11. building  (10개 - category FITNESS 등)
-- =====================================================================
INSERT INTO `building`
  (`id`,`user_id`,`category`,`position`,`level`,`created_at`)
VALUES
  (1 ,2,'FITNESS',1,3,NOW() - INTERVAL 105 DAY),
  (2 ,2,'STUDY'  ,2,1,NOW() - INTERVAL 54 DAY),
  (3 ,3,'FITNESS',1,2,NOW() - INTERVAL 100 DAY),
  (4 ,3,'COOK'   ,2,1,NOW() - INTERVAL 98 DAY),
  (5 ,4,'COOK'   ,1,4,NOW() - INTERVAL 88 DAY),
  (6 ,4,'BEAUTY' ,2,1,NOW() - INTERVAL 73 DAY),
  (7 ,5,'STUDY'  ,1,2,NOW() - INTERVAL 76 DAY),
  (8 ,5,'ART'    ,2,1,NOW() - INTERVAL 70 DAY),
  (9 ,6,'FITNESS',1,5,NOW() - INTERVAL 129 DAY),
  (10,7,'COOK'   ,1,3,NOW() - INTERVAL 124 DAY);

-- =====================================================================
--  12. calendar  (10개 - 과거 완료분 + 앞으로 할 일(미래))
-- =====================================================================
INSERT INTO `calendar`
  (`id`,`user_id`,`start`,`title`,`end`,`category`,`is_completed`,`created_at`)
VALUES
  (1 ,2,CURDATE() + INTERVAL 1 DAY,'아침 운동 30분'      ,NULL                    ,'TODO',0,NOW() - INTERVAL 1 DAY),
  (2 ,2,CURDATE() + INTERVAL 3 DAY,'자바 강의 듣기'       ,NULL                    ,'TODO',0,NOW() - INTERVAL 1 DAY),
  (3 ,3,CURDATE() + INTERVAL 2 DAY,'필라테스 복습'        ,NULL                    ,'MEMO',0,NOW() - INTERVAL 20 HOUR),
  (4 ,4,CURDATE() + INTERVAL 4 DAY,'집밥 도전 - 김치찌개' ,NULL                    ,'TODO',0,NOW() - INTERVAL 8 HOUR),
  (5 ,5,CURDATE()                 ,'스터디 모임'          ,CURDATE()               ,'TODO',1,NOW() - INTERVAL 2 DAY),
  (6 ,2,CURDATE() + INTERVAL 5 DAY,'리뷰 작성하기'        ,NULL                    ,'MEMO',0,NOW() - INTERVAL 4 HOUR),
  (7 ,3,CURDATE() + INTERVAL 7 DAY,'친구 만나기'          ,NULL                    ,'MEMO',0,NOW() - INTERVAL 3 HOUR),
  (8 ,4,CURDATE() + INTERVAL 6 DAY,'베이킹 실습'          ,NULL                    ,'TODO',0,NOW() - INTERVAL 2 HOUR),
  (9 ,5,CURDATE() + INTERVAL 8 DAY,'영어 회화 복습'       ,NULL                    ,'TODO',0,NOW() - INTERVAL 90 MINUTE),
  (10,6,CURDATE() + INTERVAL 1 DAY,'강의 영상 업로드'     ,NULL                    ,'TODO',0,NOW() - INTERVAL 25 HOUR);

-- =====================================================================
--  13. chat_room  (4개)
-- =====================================================================
INSERT INTO `chat_room` (`id`,`created_at`) VALUES
  (1,NOW() - INTERVAL 9 DAY),
  (2,NOW() - INTERVAL 8 DAY),
  (3,NOW() - INTERVAL 7 DAY),
  (4,NOW() - INTERVAL 6 DAY);

-- =====================================================================
--  14. chat_room_member  (8개 - uq(room_id,user_id))
-- =====================================================================
INSERT INTO `chat_room_member`
  (`id`,`room_id`,`user_id`,`joined_at`)
VALUES
  (1,1,2,NOW() - INTERVAL 9 DAY),
  (2,1,3,NOW() - INTERVAL 9 DAY + INTERVAL 1 MINUTE),
  (3,2,4,NOW() - INTERVAL 8 DAY),
  (4,2,5,NOW() - INTERVAL 8 DAY + INTERVAL 1 MINUTE),
  (5,3,2,NOW() - INTERVAL 7 DAY),
  (6,3,6,NOW() - INTERVAL 7 DAY + INTERVAL 1 MINUTE),
  (7,4,3,NOW() - INTERVAL 6 DAY),
  (8,4,7,NOW() - INTERVAL 6 DAY + INTERVAL 1 MINUTE);

-- =====================================================================
--  15. message  (10개 - 마지막 대화는 최근 1~2시간 전)
-- =====================================================================
INSERT INTO `message`
  (`id`,`room_id`,`sender_id`,`content`,`is_read`,`created_at`)
VALUES
  (1 ,1,2,'안녕하세요 같이 운동해요',1,NOW() - INTERVAL 9 DAY),
  (2 ,1,3,'네 좋아요! 몇시에 하세요?',1,NOW() - INTERVAL 9 DAY + INTERVAL 5 MINUTE),
  (3 ,2,4,'집밥 레시피 공유해요',1,NOW() - INTERVAL 8 DAY),
  (4 ,2,5,'오 감사합니다',0,NOW() - INTERVAL 8 DAY + INTERVAL 3 MINUTE),
  (5 ,3,2,'강사님 질문이 있어요',1,NOW() - INTERVAL 7 DAY),
  (6 ,3,6,'네 편하게 말씀하세요',1,NOW() - INTERVAL 7 DAY + INTERVAL 4 MINUTE),
  (7 ,4,3,'안녕하세요',1,NOW() - INTERVAL 6 DAY),
  (8 ,4,7,'반갑습니다',0,NOW() - INTERVAL 6 DAY + INTERVAL 2 MINUTE),
  (9 ,1,2,'내일 7시에 봐요',0,NOW() - INTERVAL 41 MINUTE),
  (10,2,5,'레시피 잘 받았어요 감사합니다',0,NOW() - INTERVAL 2 HOUR);

-- =====================================================================
--  16. friend  (8개 - 친구는 오래전, 보낸요청(SENT)은 최근)
-- =====================================================================
INSERT INTO `friend`
  (`id`,`from_user_id`,`to_user_id`,`status`,`created_at`)
VALUES
  (1,2,3 ,'FRIEND',NOW() - INTERVAL 89 DAY),
  (2,2,4 ,'FRIEND',NOW() - INTERVAL 88 DAY),
  (3,3,5 ,'SENT'  ,NOW() - INTERVAL 1 DAY),
  (4,4,5 ,'FRIEND',NOW() - INTERVAL 80 DAY),
  (5,5,2 ,'SENT'  ,NOW() - INTERVAL 35 MINUTE),
  (6,3,6 ,'FRIEND',NOW() - INTERVAL 78 DAY),
  (7,2,10,'BLOCK' ,NOW() - INTERVAL 3 HOUR),
  (8,4,7 ,'FRIEND',NOW() - INTERVAL 72 DAY);

-- =====================================================================
--  17. guestbook  (8개)
-- =====================================================================
INSERT INTO `guestbook`
  (`id`,`writer_id`,`owner_id`,`content`,`is_read`,`created_at`)
VALUES
  (1,3,2,'민수님 잘 지내요?',1,NOW() - INTERVAL 4 DAY),
  (2,4,2,'운동 화이팅입니다',0,NOW() - INTERVAL 3 DAY),
  (3,2,3,'지영님 필라테스 멋져요',1,NOW() - INTERVAL 3 DAY),
  (4,5,4,'집밥 후기 잘봤어요',0,NOW() - INTERVAL 2 DAY),
  (5,2,5,'스터디 같이해요',0,NOW() - INTERVAL 2 DAY),
  (6,6,2,'수강 감사합니다!',1,NOW() - INTERVAL 26 HOUR),
  (7,7,3,'안녕하세요 놀러왔어요',0,NOW() - INTERVAL 5 HOUR),
  (8,3,4,'또 놀러왔습니다',0,NOW() - INTERVAL 50 MINUTE);

-- =====================================================================
--  18. notification  (10개 - 신고/친구요청 알림은 최신 이벤트와 시각 동기화)
-- =====================================================================
INSERT INTO `notification`
  (`id`,`user_id`,`type`,`ref_id`,`message`,`created_at`)
VALUES
  (1 ,8,'APPROVAL'      ,8 ,'강사 신청이 접수되어 검토 중입니다.',NOW() - INTERVAL 9 DAY),
  (2 ,6,'ENROLLMENT'    ,1 ,'새 수강생이 강의에 등록했습니다.',NOW() - INTERVAL 75 DAY),
  (3 ,2,'FRIEND_REQUEST',5 ,'서연님이 친구 요청을 보냈습니다.',NOW() - INTERVAL 35 MINUTE),
  (4 ,3,'MESSAGE'       ,7 ,'새 메시지가 도착했습니다.',NOW() - INTERVAL 6 DAY),
  (5 ,2,'GUESTBOOK'     ,1 ,'방명록에 새 글이 작성되었습니다.',NOW() - INTERVAL 4 DAY),
  (6 ,1,'REPORT'        ,1 ,'새로운 신고가 접수되었습니다.',NOW() - INTERVAL 4 MINUTE),
  (7 ,4,'NOTICE'        ,1 ,'서버 정기 점검 안내',NOW() - INTERVAL 4 DAY),
  (8 ,5,'ENROLLMENT'    ,6 ,'자바 기초 강의 등록이 완료되었습니다.',NOW() - INTERVAL 76 DAY),
  (9 ,2,'MESSAGE'       ,9 ,'새 메시지가 도착했습니다.',NOW() - INTERVAL 41 MINUTE),
  (10,9,'APPROVAL'      ,9 ,'강사 신청이 거절되었습니다.',NOW() - INTERVAL 19 DAY);

-- =====================================================================
--  19. user_oauth  (5개 - uq(provider,provider_id)) / 가입 시각과 동기화
-- =====================================================================
INSERT INTO `user_oauth`
  (`id`,`user_id`,`provider`,`provider_id`,`created_at`)
VALUES
  (1,2 ,'LOCAL' ,'local_2'     ,NOW() - INTERVAL 108 DAY),
  (2,3 ,'LOCAL' ,'local_3'     ,NOW() - INTERVAL 103 DAY),
  (3,12,'KAKAO' ,'kakao_8842'  ,NOW() - INTERVAL 24 DAY),
  (4,4 ,'GOOGLE','google_55021',NOW() - INTERVAL 89 DAY),
  (5,5 ,'KAKAO' ,'kakao_3310'  ,NOW() - INTERVAL 78 DAY);

-- =====================================================================
--  20. verification_code  (6개 - 진행중 코드는 만료가 미래(NOW()+))
-- =====================================================================
INSERT INTO `verification_code`
  (`id`,`user_id`,`email`,`code`,`purpose`,`expires_at`,`used`)
VALUES
  (1,2   ,'student1@momo.city','123456','SIGNUP'        ,NOW() - INTERVAL 108 DAY,1),
  (2,NULL,'newuser@test.com'  ,'654321','SIGNUP'        ,NOW() + INTERVAL 10 MINUTE,0),
  (3,3   ,'student2@momo.city','111222','PASSWORD_RESET',NOW() + INTERVAL 8 MINUTE ,0),
  (4,4   ,'student3@momo.city','333444','EMAIL_CHANGE'  ,NOW() + INTERVAL 9 MINUTE ,0),
  (5,NULL,'guest@test.com'    ,'999000','SIGNUP'        ,NOW() - INTERVAL 2 DAY    ,0),
  (6,5   ,'student4@momo.city','555666','PASSWORD_RESET',NOW() + INTERVAL 7 MINUTE ,1);

-- =====================================================================
--  21. access_log  (10개 - 최근 4시간 내 접속 흔적)
-- =====================================================================
INSERT INTO `access_log`
  (`id`,`user_id`,`ip`,`action`,`created_at`)
VALUES
  (1 ,1   ,'127.0.0.1'    ,'LOGIN'         ,NOW() - INTERVAL 12 MINUTE),
  (2 ,2   ,'192.168.0.10' ,'LOGIN'         ,NOW() - INTERVAL 38 MINUTE),
  (3 ,2   ,'192.168.0.10' ,'VIEW_LECTURE'  ,NOW() - INTERVAL 33 MINUTE),
  (4 ,3   ,'10.0.0.5'     ,'LOGIN'         ,NOW() - INTERVAL 55 MINUTE),
  (5 ,NULL,'203.0.113.7'  ,'LOGIN_FAILED'  ,NOW() - INTERVAL 1 HOUR),
  (6 ,4   ,'10.0.0.8'     ,'ENROLL'        ,NOW() - INTERVAL 70 MINUTE),
  (7 ,5   ,'172.16.0.3'   ,'LOGIN'         ,NOW() - INTERVAL 2 HOUR),
  (8 ,1   ,'127.0.0.1'    ,'VIEW_DASHBOARD',NOW() - INTERVAL 6 MINUTE),
  (9 ,2   ,'192.168.0.10' ,'LOGOUT'        ,NOW() - INTERVAL 3 HOUR),
  (10,NULL,'198.51.100.2' ,'LOGIN_FAILED'  ,NOW() - INTERVAL 4 HOUR);

-- =====================================================================
--  22. report  (12개) ★ admin/report BC 핵심 더미
--   status 분포 : PENDING 6 / REVIEWED 2 / RESOLVED 2 / REJECTED 2
--   ★ 신규일수록 PENDING(분 단위 전), 오래될수록 처리완료(수일 전) = 운영 흐름 재현
--   (GET /api/v1/reports?status=PENDING 필터 시 6건, 최근순 정렬 시 r1 이 최상단)
-- =====================================================================
-- ※ 실제 엔티티 스키마 반영 (잔재 컬럼 reporter_id/target_nickname/reason_detail 은 00-ddl-fix.sql 에서 DROP).
--   현재 엔티티 사용 컬럼만: reporter_user_id(신고자) / detail(사유 상세) / reported_at(접수시각) / updated_at.
--   처리완료(REVIEWED/RESOLVED/REJECTED) 건만 handled_at + handler_admin_id(관리자=1). created_at=reported_at 로 정렬 일관성.
--   ※ target 은 (target_type + target_id) 로 식별. (대상 닉네임은 엔티티가 저장 안 함)
INSERT INTO `report`
  (`id`,`reporter_user_id`,`target_type`,`target_id`,`reason`,`detail`,`status`,`created_at`,`reported_at`,`updated_at`,`handled_at`,`handler_admin_id`)
VALUES
  (1 ,2,'USER'   ,10,'SPAM'         ,'DM으로 광고를 계속 보냅니다.'      ,'PENDING' ,NOW() - INTERVAL 4 MINUTE ,NOW() - INTERVAL 4 MINUTE ,NOW() - INTERVAL 4 MINUTE ,NULL                  ,NULL),
  (2 ,3,'COMMENT',11,'ABUSE'        ,'댓글에 욕설과 비방이 있습니다.'     ,'PENDING' ,NOW() - INTERVAL 51 MINUTE,NOW() - INTERVAL 51 MINUTE,NOW() - INTERVAL 51 MINUTE,NULL                  ,NULL),
  (3 ,4,'COMMENT',12,'SPAM'         ,'스팸 광고 댓글을 도배합니다.'      ,'PENDING' ,NOW() - INTERVAL 19 HOUR  ,NOW() - INTERVAL 19 HOUR  ,NOW() - INTERVAL 19 HOUR  ,NULL                  ,NULL),
  (4 ,5,'POST'   ,8 ,'INAPPROPRIATE','게시글에 부적절한 이미지가 있어요.','REVIEWING',NOW() - INTERVAL 2 DAY    ,NOW() - INTERVAL 2 DAY    ,NOW() - INTERVAL 44 HOUR  ,NOW() - INTERVAL 44 HOUR,1),
  (5 ,2,'USER'   ,11,'ABUSE'        ,'지속적으로 비방 메시지를 보냅니다.','CONFIRMED',NOW() - INTERVAL 3 DAY    ,NOW() - INTERVAL 3 DAY    ,NOW() - INTERVAL 2 DAY    ,NOW() - INTERVAL 2 DAY  ,1),
  (6 ,3,'LECTURE',10,'OTHER'          ,'강의 내용이 설명과 다릅니다.'      ,'PENDING' ,NOW() - INTERVAL 5 HOUR   ,NOW() - INTERVAL 5 HOUR   ,NOW() - INTERVAL 5 HOUR   ,NULL                  ,NULL),
  (7 ,4,'POST'   ,2 ,'SPAM'         ,'홍보성 글로 의심됩니다.'          ,'REJECTED',NOW() - INTERVAL 4 DAY - INTERVAL 3 HOUR,NOW() - INTERVAL 4 DAY - INTERVAL 3 HOUR,NOW() - INTERVAL 4 DAY,NOW() - INTERVAL 4 DAY,1),
  (8 ,5,'USER'   ,10,'ABUSE'        ,'욕설이 담긴 DM을 받았습니다.'      ,'REVIEWING',NOW() - INTERVAL 26 HOUR  ,NOW() - INTERVAL 26 HOUR  ,NOW() - INTERVAL 20 HOUR  ,NOW() - INTERVAL 20 HOUR,1),
  (9 ,6,'COMMENT',11,'INAPPROPRIATE','외부 광고 링크를 첨부했습니다.'   ,'PENDING' ,NOW() - INTERVAL 23 MINUTE,NOW() - INTERVAL 23 MINUTE,NOW() - INTERVAL 23 MINUTE,NULL                  ,NULL),
  (10,2,'POST'   ,9 ,'OTHER'          ,'동일 내용을 중복 게시했습니다.'    ,'CONFIRMED',NOW() - INTERVAL 4 DAY    ,NOW() - INTERVAL 4 DAY    ,NOW() - INTERVAL 3 DAY    ,NOW() - INTERVAL 3 DAY  ,1),
  (11,7,'USER'   ,11,'SPAM'         ,'대량으로 친구신청 스팸을 보냅니다.','PENDING' ,NOW() - INTERVAL 2 HOUR   ,NOW() - INTERVAL 2 HOUR   ,NOW() - INTERVAL 2 HOUR   ,NULL                  ,NULL),
  (12,3,'LECTURE',5 ,'OTHER'          ,'강의 일정을 지키지 않았습니다.'    ,'REJECTED',NOW() - INTERVAL 6 DAY    ,NOW() - INTERVAL 6 DAY    ,NOW() - INTERVAL 5 DAY    ,NOW() - INTERVAL 5 DAY  ,1);

-- =====================================================================
--  23. payment  (8개 - 결제는 가입 직후~최근 구독 갱신까지)
-- =====================================================================
INSERT INTO `payment`
  (`id`,`user_id`,`amount`,`method`,`paid_at`)
VALUES
  (1,2,19900,'KAKAO',NOW() - INTERVAL 105 DAY),
  (2,2,29900,'TOSS' ,NOW() - INTERVAL 14 DAY),
  (3,3,19900,'CARD' ,NOW() - INTERVAL 100 DAY),
  (4,4, 9900,'FREE' ,NOW() - INTERVAL 88 DAY),
  (5,5,39900,'KAKAO',NOW() - INTERVAL 76 DAY),
  (6,3,19900,'TOSS' ,NOW() - INTERVAL 10 DAY),
  (7,4,19900,'CARD' ,NOW() - INTERVAL 73 DAY),
  (8,2, 9900,'FREE' ,NOW() - INTERVAL 2 DAY);

-- =====================================================================
--  24. inquiry  (8개 - WAITING 은 최근, ANSWERED/CLOSED 는 과거+답변시각)
-- =====================================================================
INSERT INTO `inquiry`
  (`id`,`user_id`,`title`,`content`,`answer`,`status`,`created_at`,`answered_at`)
VALUES
  (1,2,'환불 문의'    ,'결제한 강의 환불 가능한가요?'   ,NULL                       ,'WAITING' ,NOW() - INTERVAL 5 HOUR ,NULL),
  (2,3,'강의 영상 오류','6강 영상이 재생되지 않습니다.'  ,'확인 후 재인코딩 완료했습니다.','ANSWERED',NOW() - INTERVAL 26 HOUR,NOW() - INTERVAL 22 HOUR),
  (3,4,'결제 중복'    ,'카드가 두 번 결제됐어요.'      ,NULL                       ,'WAITING' ,NOW() - INTERVAL 3 HOUR ,NULL),
  (4,5,'닉네임 변경'  ,'닉네임 변경 가능한가요?'       ,'마이페이지에서 변경 가능합니다.','ANSWERED',NOW() - INTERVAL 2 DAY  ,NOW() - INTERVAL 47 HOUR),
  (5,2,'제휴 문의'    ,'기업 제휴를 문의드립니다.'     ,NULL                       ,'WAITING' ,NOW() - INTERVAL 90 MINUTE,NULL),
  (6,3,'버그 신고'    ,'대시보드가 열리지 않습니다.'    ,'수정 배포 완료했습니다.'     ,'CLOSED'  ,NOW() - INTERVAL 4 DAY  ,NOW() - INTERVAL 3 DAY),
  (7,4,'강사 신청 방법','강사가 되려면 어떻게 하나요?'   ,'증빙 서류와 함께 신청해 주세요.','ANSWERED',NOW() - INTERVAL 9 DAY  ,NOW() - INTERVAL 9 DAY + INTERVAL 5 HOUR),
  (8,5,'환불 재문의'  ,'환불이 아직 처리되지 않았어요.' ,NULL                       ,'WAITING' ,NOW() - INTERVAL 38 MINUTE,NULL);

-- =====================================================================
--  25. refresh_token  (6개 - uq(user_id) / 만료는 미래, 발급은 최근 로그인)
-- =====================================================================
INSERT INTO `refresh_token`
  (`id`,`user_id`,`token`,`expires_at`,`created_at`)
VALUES
  (1,1,'rt_seed_hash_admin_0001',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 12 MINUTE),
  (2,2,'rt_seed_hash_minsu_0002',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 38 MINUTE),
  (3,3,'rt_seed_hash_jiyoung_03',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 55 MINUTE),
  (4,4,'rt_seed_hash_hyunwoo_04',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 70 MINUTE),
  (5,5,'rt_seed_hash_seoyeon_05',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 2 HOUR),
  (6,6,'rt_seed_hash_coach_0006',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 5 HOUR);

-- =====================================================================
--  error_log  (12개) ★ admin BC 핵심 더미
--   ※ ERD 에 없는 코드측 테이블. 앱 1회 부팅으로 Hibernate 가 생성한 뒤 실행할 것.
--   ※ created_at/updated_at 은 JPA Auditing 컬럼이라 DB 기본값 없음 -> 명시 입력 필수.
--   level 분포 : CRITICAL 3 / ERROR 5 / WARNING 4 / 최신 에러는 약 2분 전
-- =====================================================================
INSERT INTO `error_log`
  (`id`,`level`,`source`,`message`,`occurred_at`,`created_at`,`updated_at`)
VALUES
  (1 ,'ERROR'   ,'API Error','Payment gateway timeout (TOSS)'                        ,NOW() - INTERVAL 2 MINUTE ,NOW() - INTERVAL 2 MINUTE ,NOW() - INTERVAL 2 MINUTE),
  (2 ,'CRITICAL','Database' ,'Connection pool exhausted (HikariCP)'                  ,NOW() - INTERVAL 17 MINUTE,NOW() - INTERVAL 17 MINUTE,NOW() - INTERVAL 17 MINUTE),
  (3 ,'WARNING' ,'Frontend' ,'Deprecated endpoint called: /api/v1/admin/reports'    ,NOW() - INTERVAL 48 MINUTE,NOW() - INTERVAL 48 MINUTE,NOW() - INTERVAL 48 MINUTE),
  (4 ,'ERROR'   ,'Server'   ,'NullPointerException in ReportCommandService'          ,NOW() - INTERVAL 2 HOUR   ,NOW() - INTERVAL 2 HOUR   ,NOW() - INTERVAL 2 HOUR),
  (5 ,'WARNING' ,'API Error','Slow response (>2s) on GET /api/v1/reports'            ,NOW() - INTERVAL 3 HOUR   ,NOW() - INTERVAL 3 HOUR   ,NOW() - INTERVAL 3 HOUR),
  (6 ,'CRITICAL','Server'   ,'OutOfMemoryError: Java heap space'                     ,NOW() - INTERVAL 14 HOUR  ,NOW() - INTERVAL 14 HOUR  ,NOW() - INTERVAL 14 HOUR),
  (7 ,'ERROR'   ,'Database' ,'Deadlock found when trying to get lock'                ,NOW() - INTERVAL 20 HOUR  ,NOW() - INTERVAL 20 HOUR  ,NOW() - INTERVAL 20 HOUR),
  (8 ,'WARNING' ,'Frontend' ,'Image failed to load (404): /img/p2-1.jpg'             ,NOW() - INTERVAL 26 HOUR  ,NOW() - INTERVAL 26 HOUR  ,NOW() - INTERVAL 26 HOUR),
  (9 ,'ERROR'   ,'API Error','JWT signature does not match'                          ,NOW() - INTERVAL 31 HOUR  ,NOW() - INTERVAL 31 HOUR  ,NOW() - INTERVAL 31 HOUR),
  (10,'WARNING' ,'Server'   ,'Disk usage above 80% on /data'                         ,NOW() - INTERVAL 2 DAY    ,NOW() - INTERVAL 2 DAY    ,NOW() - INTERVAL 2 DAY),
  (11,'CRITICAL','Database' ,'Replication lag exceeded threshold (30s)'              ,NOW() - INTERVAL 2 DAY - INTERVAL 4 HOUR,NOW() - INTERVAL 2 DAY - INTERVAL 4 HOUR,NOW() - INTERVAL 2 DAY - INTERVAL 4 HOUR),
  (12,'ERROR'   ,'API Error','Kakao OAuth token exchange failed (400)'               ,NOW() - INTERVAL 3 DAY    ,NOW() - INTERVAL 3 DAY    ,NOW() - INTERVAL 3 DAY);

-- #####################################################################
-- ##  STEP 3. 페이지네이션용 추가 더미 (+20행/테이블)  [FE 요청]
-- ##   - 기존 데이터 뒤에 id 를 이어붙인다 (충돌 없음).
-- ##   - 모든 user profile_image_url = 기본 프사(S3).
-- ##   - created_at 은 NOW() 상대값으로 분산 → 정렬/페이지 넘김 테스트.
-- #####################################################################

-- user (id 14~33) : 강사 2(14,15) + 학생 18(16~33)
INSERT INTO `user`
  (`id`,`email`,`password`,`name`,`nickname`,`birth`,`profile_image_url`,`role`,`status`,`category`,`proof`,`point`,`is_paid`,`do_not_disturb`,`created_at`,`deleted_at`,`is_tempPWD`)
VALUES
  (14,'teacher5@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','이코치','coach_lee','1987-03-03','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','TEACHER','BANNED','FITNESS','https://momo.city/proof/lee.pdf',900,1,0,NOW() - INTERVAL 50 DAY,NULL,0),
  (15,'teacher6@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','최셰프','chef_choi','1986-05-05','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','TEACHER','DELETED','COOK','https://momo.city/proof/choi.pdf',1100,1,0,NOW() - INTERVAL 48 DAY,NOW() - INTERVAL 5 DAY,0),
  (16,'teacher7@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','윤강사','coach_yoon','1989-07-07','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','TEACHER','BLACK','STUDY','https://momo.city/proof/yoon.pdf',0,0,0,NOW() - INTERVAL 46 DAY,NULL,0),
  (17,'pageuser17@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저열일곱','page_u17','1996-02-02','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE',NULL,NULL,80,0,0,NOW() - INTERVAL 44 DAY,NULL,0),
  (18,'pageuser18@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저열여덟','page_u18','1997-03-03','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE',NULL,NULL,300,1,0,NOW() - INTERVAL 42 DAY,NULL,0),
  (19,'pageuser19@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저열아홉','page_u19','1998-04-04','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','BANNED',NULL,NULL,50,0,0,NOW() - INTERVAL 40 DAY,NULL,0),
  (20,'pageuser20@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저스물','page_u20','1999-05-05','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','BLACK',NULL,NULL,200,1,0,NOW() - INTERVAL 38 DAY,NULL,0),
  (21,'pageuser21@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저스물하나','page_u21','1994-06-06','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','DELETED',NULL,NULL,0,0,0,NOW() - INTERVAL 36 DAY,NOW() - INTERVAL 3 DAY,0),
  (22,'pageuser22@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저스물둘','page_u22','1993-07-07','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE',NULL,NULL,90,0,0,NOW() - INTERVAL 34 DAY,NULL,0),
  (23,'pageuser23@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저스물셋','page_u23','1992-08-08','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE',NULL,NULL,150,1,0,NOW() - INTERVAL 32 DAY,NULL,0),
  (24,'pageuser24@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저스물넷','page_u24','1995-09-09','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE',NULL,NULL,70,0,0,NOW() - INTERVAL 30 DAY,NULL,0),
  (25,'pageuser25@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저스물다섯','page_u25','1996-10-10','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE',NULL,NULL,310,1,0,NOW() - INTERVAL 28 DAY,NULL,0),
  (26,'pageuser26@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저스물여섯','page_u26','1997-11-11','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE',NULL,NULL,40,0,0,NOW() - INTERVAL 26 DAY,NULL,0),
  (27,'pageuser27@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저스물일곱','page_u27','1998-12-12','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE',NULL,NULL,220,1,0,NOW() - INTERVAL 24 DAY,NULL,0),
  (28,'pageuser28@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저스물여덟','page_u28','1999-01-13','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE',NULL,NULL,60,0,0,NOW() - INTERVAL 22 DAY,NULL,0),
  (29,'pageuser29@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저스물아홉','page_u29','1994-02-14','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE',NULL,NULL,180,1,0,NOW() - INTERVAL 20 DAY,NULL,0),
  (30,'pageuser30@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저서른','page_u30','1995-03-15','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE',NULL,NULL,110,0,0,NOW() - INTERVAL 18 DAY,NULL,0),
  (31,'pageuser31@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저서른하나','page_u31','1996-04-16','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE',NULL,NULL,260,1,0,NOW() - INTERVAL 16 DAY,NULL,0),
  (32,'pageuser32@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저서른둘','page_u32','1997-05-17','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE',NULL,NULL,30,0,0,NOW() - INTERVAL 14 DAY,NULL,0),
  (33,'pageuser33@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','유저서른셋','page_u33','1998-06-18','https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png','STUDENT','ACTIVE',NULL,NULL,140,1,0,NOW() - INTERVAL 12 DAY,NULL,0);

-- user_oauth (id 6~25) : 새 user 14~33 LOCAL
INSERT INTO `user_oauth` (`id`,`user_id`,`provider`,`provider_id`,`created_at`) VALUES
  (6,14,'LOCAL','local_14',NOW() - INTERVAL 50 DAY),(7,15,'LOCAL','local_15',NOW() - INTERVAL 48 DAY),
  (8,16,'LOCAL','local_16',NOW() - INTERVAL 46 DAY),(9,17,'LOCAL','local_17',NOW() - INTERVAL 44 DAY),
  (10,18,'LOCAL','local_18',NOW() - INTERVAL 42 DAY),(11,19,'LOCAL','local_19',NOW() - INTERVAL 40 DAY),
  (12,20,'LOCAL','local_20',NOW() - INTERVAL 38 DAY),(13,21,'LOCAL','local_21',NOW() - INTERVAL 36 DAY),
  (14,22,'LOCAL','local_22',NOW() - INTERVAL 34 DAY),(15,23,'LOCAL','local_23',NOW() - INTERVAL 32 DAY),
  (16,24,'LOCAL','local_24',NOW() - INTERVAL 30 DAY),(17,25,'LOCAL','local_25',NOW() - INTERVAL 28 DAY),
  (18,26,'LOCAL','local_26',NOW() - INTERVAL 26 DAY),(19,27,'LOCAL','local_27',NOW() - INTERVAL 24 DAY),
  (20,28,'LOCAL','local_28',NOW() - INTERVAL 22 DAY),(21,29,'LOCAL','local_29',NOW() - INTERVAL 20 DAY),
  (22,30,'LOCAL','local_30',NOW() - INTERVAL 18 DAY),(23,31,'LOCAL','local_31',NOW() - INTERVAL 16 DAY),
  (24,32,'LOCAL','local_32',NOW() - INTERVAL 14 DAY),(25,33,'LOCAL','local_33',NOW() - INTERVAL 12 DAY);

-- refresh_token (id 7~26) : 새 user 14~33 (user_id UNIQUE)
INSERT INTO `refresh_token` (`id`,`user_id`,`token`,`expires_at`,`created_at`) VALUES
  (7,14,'rt_page_14',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 1 HOUR),(8,15,'rt_page_15',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 2 HOUR),
  (9,16,'rt_page_16',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 3 HOUR),(10,17,'rt_page_17',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 4 HOUR),
  (11,18,'rt_page_18',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 5 HOUR),(12,19,'rt_page_19',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 6 HOUR),
  (13,20,'rt_page_20',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 7 HOUR),(14,21,'rt_page_21',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 8 HOUR),
  (15,22,'rt_page_22',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 9 HOUR),(16,23,'rt_page_23',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 10 HOUR),
  (17,24,'rt_page_24',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 11 HOUR),(18,25,'rt_page_25',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 12 HOUR),
  (19,26,'rt_page_26',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 13 HOUR),(20,27,'rt_page_27',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 14 HOUR),
  (21,28,'rt_page_28',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 15 HOUR),(22,29,'rt_page_29',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 16 HOUR),
  (23,30,'rt_page_30',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 17 HOUR),(24,31,'rt_page_31',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 18 HOUR),
  (25,32,'rt_page_32',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 19 HOUR),(26,33,'rt_page_33',NOW() + INTERVAL 14 DAY,NOW() - INTERVAL 20 HOUR);

-- lecture (id 11~30) : teacher_id ∈ {6,7,14,15}
INSERT INTO `lecture` (`id`,`teacher_id`,`title`,`description`,`thumbnail_url`,`category`,`status`,`completed_user_count`,`created_at`) VALUES
  (11,6 ,'페이지 강의 11','전신 순환 운동',NULL,'FITNESS','ACTIVE',12,NOW() - INTERVAL 47 DAY),
  (12,7 ,'페이지 강의 12','반찬 만들기',NULL,'COOK','ACTIVE',8,NOW() - INTERVAL 45 DAY),
  (13,14,'페이지 강의 13','홈트 입문',NULL,'FITNESS','ACTIVE',20,NOW() - INTERVAL 43 DAY),
  (14,15,'페이지 강의 14','베이킹 중급',NULL,'COOK','ACTIVE',5,NOW() - INTERVAL 41 DAY),
  (15,6 ,'페이지 강의 15','스트레칭',NULL,'FITNESS','ACTIVE',14,NOW() - INTERVAL 39 DAY),
  (16,7 ,'페이지 강의 16','국물 요리',NULL,'COOK','ACTIVE',9,NOW() - INTERVAL 37 DAY),
  (17,14,'페이지 강의 17','체형 교정',NULL,'FITNESS','ACTIVE',7,NOW() - INTERVAL 35 DAY),
  (18,15,'페이지 강의 18','디저트 클래스',NULL,'COOK','ACTIVE',11,NOW() - INTERVAL 33 DAY),
  (19,6 ,'페이지 강의 19','코어 운동',NULL,'FITNESS','ACTIVE',16,NOW() - INTERVAL 31 DAY),
  (20,7 ,'페이지 강의 20','한식 기본',NULL,'COOK','ACTIVE',6,NOW() - INTERVAL 29 DAY),
  (21,14,'페이지 강의 21','유산소 루틴',NULL,'FITNESS','WAITING',0,NOW() - INTERVAL 27 DAY),
  (22,15,'페이지 강의 22','브런치 요리',NULL,'COOK','ACTIVE',13,NOW() - INTERVAL 25 DAY),
  (23,6 ,'페이지 강의 23','마음챙김 요가 입문',NULL,'FITNESS','ACTIVE',10,NOW() - INTERVAL 23 DAY),
  (24,7 ,'페이지 강의 24','면 요리',NULL,'COOK','HOLD',3,NOW() - INTERVAL 21 DAY),
  (25,14,'페이지 강의 25','근력 강화',NULL,'FITNESS','ACTIVE',18,NOW() - INTERVAL 19 DAY),
  (26,15,'페이지 강의 26','홈카페',NULL,'COOK','ACTIVE',4,NOW() - INTERVAL 17 DAY),
  (27,6 ,'페이지 강의 27','필라테스 심화',NULL,'FITNESS','ACTIVE',9,NOW() - INTERVAL 15 DAY),
  (28,7 ,'페이지 강의 28','샐러드 만들기',NULL,'COOK','ACTIVE',7,NOW() - INTERVAL 13 DAY),
  (29,14,'페이지 강의 29','맨몸 운동',NULL,'FITNESS','ACTIVE',15,NOW() - INTERVAL 11 DAY),
  (30,15,'페이지 강의 30','파스타 클래스',NULL,'COOK','WAITING',0,NOW() - INTERVAL 9 DAY);

-- chapter (id 11~30) : lecture 11~30 각 1챕터
INSERT INTO `chapter` (`id`,`lecture_id`,`title`,`order_no`,`video_url`,`video_size_bytes`,`duration_sec`,`video_status`,`original_filename`,`created_at`) VALUES
  (11,11,'1강',1,'https://cdn.momo.city/v/p11.mp4',104857600,600,'READY','p11.mp4',NOW() - INTERVAL 47 DAY),
  (12,12,'1강',1,'https://cdn.momo.city/v/p12.mp4',104857600,600,'READY','p12.mp4',NOW() - INTERVAL 45 DAY),
  (13,13,'1강',1,'https://cdn.momo.city/v/p13.mp4',104857600,600,'READY','p13.mp4',NOW() - INTERVAL 43 DAY),
  (14,14,'1강',1,'https://cdn.momo.city/v/p14.mp4',104857600,600,'READY','p14.mp4',NOW() - INTERVAL 41 DAY),
  (15,15,'1강',1,'https://cdn.momo.city/v/p15.mp4',104857600,600,'READY','p15.mp4',NOW() - INTERVAL 39 DAY),
  (16,16,'1강',1,'https://cdn.momo.city/v/p16.mp4',104857600,600,'READY','p16.mp4',NOW() - INTERVAL 37 DAY),
  (17,17,'1강',1,'https://cdn.momo.city/v/p17.mp4',104857600,600,'READY','p17.mp4',NOW() - INTERVAL 35 DAY),
  (18,18,'1강',1,'https://cdn.momo.city/v/p18.mp4',104857600,600,'READY','p18.mp4',NOW() - INTERVAL 33 DAY),
  (19,19,'1강',1,'https://cdn.momo.city/v/p19.mp4',104857600,600,'READY','p19.mp4',NOW() - INTERVAL 31 DAY),
  (20,20,'1강',1,'https://cdn.momo.city/v/p20.mp4',104857600,600,'READY','p20.mp4',NOW() - INTERVAL 29 DAY),
  (21,21,'1강',1,NULL,NULL,NULL,'UPLOADING','p21.mp4',NOW() - INTERVAL 27 DAY),
  (22,22,'1강',1,'https://cdn.momo.city/v/p22.mp4',104857600,600,'READY','p22.mp4',NOW() - INTERVAL 25 DAY),
  (23,23,'1강',1,'https://cdn.momo.city/v/p23.mp4',104857600,600,'READY','p23.mp4',NOW() - INTERVAL 23 DAY),
  (24,24,'1강',1,'https://cdn.momo.city/v/p24.mp4',104857600,600,'READY','p24.mp4',NOW() - INTERVAL 21 DAY),
  (25,25,'1강',1,'https://cdn.momo.city/v/p25.mp4',104857600,600,'READY','p25.mp4',NOW() - INTERVAL 19 DAY),
  (26,26,'1강',1,'https://cdn.momo.city/v/p26.mp4',104857600,600,'READY','p26.mp4',NOW() - INTERVAL 17 DAY),
  (27,27,'1강',1,'https://cdn.momo.city/v/p27.mp4',104857600,600,'READY','p27.mp4',NOW() - INTERVAL 15 DAY),
  (28,28,'1강',1,'https://cdn.momo.city/v/p28.mp4',104857600,600,'READY','p28.mp4',NOW() - INTERVAL 13 DAY),
  (29,29,'1강',1,'https://cdn.momo.city/v/p29.mp4',104857600,600,'READY','p29.mp4',NOW() - INTERVAL 11 DAY),
  (30,30,'1강',1,NULL,NULL,NULL,'ENCODING','p30.mp4',NOW() - INTERVAL 9 DAY);

-- post (id 11~30) : user_id 16~33
INSERT INTO `post` (`id`,`user_id`,`type`,`title`,`content`,`is_pinned`,`view_count`,`created_at`) VALUES
  (11,16,'FREE','페이지 게시글 11','내용 11',0,15,NOW() - INTERVAL 20 DAY),
  (12,17,'FREE','페이지 게시글 12','내용 12',0,22,NOW() - INTERVAL 19 DAY),
  (13,18,'QNA','페이지 게시글 13','질문 13',0,31,NOW() - INTERVAL 18 DAY),
  (14,19,'FREE','페이지 게시글 14','내용 14',0,8,NOW() - INTERVAL 17 DAY),
  (15,20,'FREE','페이지 게시글 15','내용 15',0,44,NOW() - INTERVAL 16 DAY),
  (16,21,'QNA','페이지 게시글 16','질문 16',0,12,NOW() - INTERVAL 15 DAY),
  (17,22,'FREE','페이지 게시글 17','내용 17',0,27,NOW() - INTERVAL 14 DAY),
  (18,23,'FREE','페이지 게시글 18','내용 18',0,19,NOW() - INTERVAL 13 DAY),
  (19,24,'QNA','페이지 게시글 19','질문 19',0,33,NOW() - INTERVAL 12 DAY),
  (20,25,'FREE','페이지 게시글 20','내용 20',0,5,NOW() - INTERVAL 11 DAY),
  (21,26,'FREE','페이지 게시글 21','내용 21',0,48,NOW() - INTERVAL 10 DAY),
  (22,27,'QNA','페이지 게시글 22','질문 22',0,16,NOW() - INTERVAL 9 DAY),
  (23,28,'FREE','페이지 게시글 23','내용 23',0,29,NOW() - INTERVAL 8 DAY),
  (24,29,'FREE','페이지 게시글 24','내용 24',0,21,NOW() - INTERVAL 7 DAY),
  (25,30,'QNA','페이지 게시글 25','질문 25',0,37,NOW() - INTERVAL 6 DAY),
  (26,31,'FREE','페이지 게시글 26','내용 26',0,9,NOW() - INTERVAL 5 DAY),
  (27,32,'FREE','페이지 게시글 27','내용 27',0,52,NOW() - INTERVAL 4 DAY),
  (28,33,'QNA','페이지 게시글 28','질문 28',0,14,NOW() - INTERVAL 3 DAY),
  (29,16,'FREE','페이지 게시글 29','내용 29',0,25,NOW() - INTERVAL 2 DAY),
  (30,17,'FREE','페이지 게시글 30','내용 30',0,40,NOW() - INTERVAL 1 DAY);

-- comment (id 13~32) : post 11~30, user 16~33
INSERT INTO `comment` (`id`,`post_id`,`user_id`,`parent_id`,`content`,`created_at`) VALUES
  (13,11,17,NULL,'댓글 13',NOW() - INTERVAL 19 DAY),(14,12,18,NULL,'댓글 14',NOW() - INTERVAL 18 DAY),
  (15,13,19,NULL,'댓글 15',NOW() - INTERVAL 17 DAY),(16,14,20,NULL,'댓글 16',NOW() - INTERVAL 16 DAY),
  (17,15,21,NULL,'댓글 17',NOW() - INTERVAL 15 DAY),(18,16,22,NULL,'댓글 18',NOW() - INTERVAL 14 DAY),
  (19,17,23,NULL,'댓글 19',NOW() - INTERVAL 13 DAY),(20,18,24,NULL,'댓글 20',NOW() - INTERVAL 12 DAY),
  (21,19,25,NULL,'댓글 21',NOW() - INTERVAL 11 DAY),(22,20,26,NULL,'댓글 22',NOW() - INTERVAL 10 DAY),
  (23,21,27,NULL,'댓글 23',NOW() - INTERVAL 9 DAY),(24,22,28,NULL,'댓글 24',NOW() - INTERVAL 8 DAY),
  (25,23,29,NULL,'댓글 25',NOW() - INTERVAL 7 DAY),(26,24,30,NULL,'댓글 26',NOW() - INTERVAL 6 DAY),
  (27,25,31,NULL,'댓글 27',NOW() - INTERVAL 5 DAY),(28,26,32,NULL,'댓글 28',NOW() - INTERVAL 4 DAY),
  (29,27,33,NULL,'댓글 29',NOW() - INTERVAL 3 DAY),(30,28,16,NULL,'댓글 30',NOW() - INTERVAL 2 DAY),
  (31,29,17,NULL,'댓글 31',NOW() - INTERVAL 1 DAY),(32,30,18,NULL,'댓글 32',NOW() - INTERVAL 12 HOUR);

-- post_image (id 9~28) : post 11~30
INSERT INTO `post_image` (`id`,`post_id`,`image_url`,`order_no`,`created_at`) VALUES
  (9,11,'https://cdn.momo.city/img/p11.jpg',0,NOW() - INTERVAL 20 DAY),(10,12,'https://cdn.momo.city/img/p12.jpg',0,NOW() - INTERVAL 19 DAY),
  (11,13,'https://cdn.momo.city/img/p13.jpg',0,NOW() - INTERVAL 18 DAY),(12,14,'https://cdn.momo.city/img/p14.jpg',0,NOW() - INTERVAL 17 DAY),
  (13,15,'https://cdn.momo.city/img/p15.jpg',0,NOW() - INTERVAL 16 DAY),(14,16,'https://cdn.momo.city/img/p16.jpg',0,NOW() - INTERVAL 15 DAY),
  (15,17,'https://cdn.momo.city/img/p17.jpg',0,NOW() - INTERVAL 14 DAY),(16,18,'https://cdn.momo.city/img/p18.jpg',0,NOW() - INTERVAL 13 DAY),
  (17,19,'https://cdn.momo.city/img/p19.jpg',0,NOW() - INTERVAL 12 DAY),(18,20,'https://cdn.momo.city/img/p20.jpg',0,NOW() - INTERVAL 11 DAY),
  (19,21,'https://cdn.momo.city/img/p21.jpg',0,NOW() - INTERVAL 10 DAY),(20,22,'https://cdn.momo.city/img/p22.jpg',0,NOW() - INTERVAL 9 DAY),
  (21,23,'https://cdn.momo.city/img/p23.jpg',0,NOW() - INTERVAL 8 DAY),(22,24,'https://cdn.momo.city/img/p24.jpg',0,NOW() - INTERVAL 7 DAY),
  (23,25,'https://cdn.momo.city/img/p25.jpg',0,NOW() - INTERVAL 6 DAY),(24,26,'https://cdn.momo.city/img/p26.jpg',0,NOW() - INTERVAL 5 DAY),
  (25,27,'https://cdn.momo.city/img/p27.jpg',0,NOW() - INTERVAL 4 DAY),(26,28,'https://cdn.momo.city/img/p28.jpg',0,NOW() - INTERVAL 3 DAY),
  (27,29,'https://cdn.momo.city/img/p29.jpg',0,NOW() - INTERVAL 2 DAY),(28,30,'https://cdn.momo.city/img/p30.jpg',0,NOW() - INTERVAL 1 DAY);

-- enrollment (id 11~30) : user 16~33 x lecture 11~30 (lecture_id 유니크라 pair 유니크)
INSERT INTO `enrollment` (`id`,`user_id`,`lecture_id`,`total_progress`,`completed_count`,`enrolled_at`) VALUES
  (11,16,11,30,1,NOW() - INTERVAL 40 DAY),(12,17,12,50,1,NOW() - INTERVAL 39 DAY),
  (13,18,13,70,2,NOW() - INTERVAL 38 DAY),(14,19,14,10,0,NOW() - INTERVAL 37 DAY),
  (15,20,15,90,1,NOW() - INTERVAL 36 DAY),(16,21,16,20,0,NOW() - INTERVAL 35 DAY),
  (17,22,17,60,1,NOW() - INTERVAL 34 DAY),(18,23,18,40,0,NOW() - INTERVAL 33 DAY),
  (19,24,19,80,2,NOW() - INTERVAL 32 DAY),(20,25,20,15,0,NOW() - INTERVAL 31 DAY),
  (21,26,21,55,1,NOW() - INTERVAL 30 DAY),(22,27,22,33,0,NOW() - INTERVAL 29 DAY),
  (23,28,23,100,1,NOW() - INTERVAL 28 DAY),(24,29,24,25,0,NOW() - INTERVAL 27 DAY),
  (25,30,25,65,1,NOW() - INTERVAL 26 DAY),(26,31,26,45,0,NOW() - INTERVAL 25 DAY),
  (27,32,27,75,2,NOW() - INTERVAL 24 DAY),(28,33,28,5,0,NOW() - INTERVAL 23 DAY),
  (29,16,29,95,1,NOW() - INTERVAL 22 DAY),(30,17,30,12,0,NOW() - INTERVAL 21 DAY);

-- learning_history (id 11~30) : version 컬럼 필수
INSERT INTO `learning_history` (`id`,`user_id`,`lecture_id`,`chapter_id`,`watched_seconds`,`is_completed`,`last_position_sec`,`progress_rate`,`created_at`,`version`) VALUES
  (11,16,11,11,600,1,600,100,NOW() - INTERVAL 39 DAY,0),(12,17,12,12,300,0,300,50,NOW() - INTERVAL 38 DAY,0),
  (13,18,13,13,600,1,600,100,NOW() - INTERVAL 37 DAY,0),(14,19,14,14,120,0,120,20,NOW() - INTERVAL 36 DAY,0),
  (15,20,15,15,600,1,600,100,NOW() - INTERVAL 35 DAY,0),(16,21,16,16,240,0,240,40,NOW() - INTERVAL 34 DAY,0),
  (17,22,17,17,600,1,600,100,NOW() - INTERVAL 33 DAY,0),(18,23,18,18,360,0,360,60,NOW() - INTERVAL 32 DAY,0),
  (19,24,19,19,600,1,600,100,NOW() - INTERVAL 31 DAY,0),(20,25,20,20,180,0,180,30,NOW() - INTERVAL 30 DAY,0),
  (21,26,21,21,600,1,600,100,NOW() - INTERVAL 29 DAY,0),(22,27,22,22,420,0,420,70,NOW() - INTERVAL 28 DAY,0),
  (23,28,23,23,600,1,600,100,NOW() - INTERVAL 27 DAY,0),(24,29,24,24,60,0,60,10,NOW() - INTERVAL 26 DAY,0),
  (25,30,25,25,600,1,600,100,NOW() - INTERVAL 25 DAY,0),(26,31,26,26,480,0,480,80,NOW() - INTERVAL 24 DAY,0),
  (27,32,27,27,600,1,600,100,NOW() - INTERVAL 23 DAY,0),(28,33,28,28,540,0,540,90,NOW() - INTERVAL 22 DAY,0),
  (29,16,29,29,600,1,600,100,NOW() - INTERVAL 21 DAY,0),(30,17,30,30,300,0,300,50,NOW() - INTERVAL 20 DAY,0);

-- review (id 9~28)
INSERT INTO `review` (`id`,`user_id`,`lecture_id`,`rating`,`content`,`created_at`) VALUES
  (9,16,11,5,'리뷰 9',NOW() - INTERVAL 30 DAY),(10,17,12,4,'리뷰 10',NOW() - INTERVAL 29 DAY),
  (11,18,13,5,'리뷰 11',NOW() - INTERVAL 28 DAY),(12,19,14,3,'리뷰 12',NOW() - INTERVAL 27 DAY),
  (13,20,15,4,'리뷰 13',NOW() - INTERVAL 26 DAY),(14,21,16,5,'리뷰 14',NOW() - INTERVAL 25 DAY),
  (15,22,17,2,'리뷰 15',NOW() - INTERVAL 24 DAY),(16,23,18,4,'리뷰 16',NOW() - INTERVAL 23 DAY),
  (17,24,19,5,'리뷰 17',NOW() - INTERVAL 22 DAY),(18,25,20,3,'리뷰 18',NOW() - INTERVAL 21 DAY),
  (19,26,21,4,'리뷰 19',NOW() - INTERVAL 20 DAY),(20,27,22,5,'리뷰 20',NOW() - INTERVAL 19 DAY),
  (21,28,23,4,'리뷰 21',NOW() - INTERVAL 18 DAY),(22,29,24,3,'리뷰 22',NOW() - INTERVAL 17 DAY),
  (23,30,25,5,'리뷰 23',NOW() - INTERVAL 16 DAY),(24,31,26,4,'리뷰 24',NOW() - INTERVAL 15 DAY),
  (25,32,27,5,'리뷰 25',NOW() - INTERVAL 14 DAY),(26,33,28,2,'리뷰 26',NOW() - INTERVAL 13 DAY),
  (27,16,29,4,'리뷰 27',NOW() - INTERVAL 12 DAY),(28,17,30,5,'리뷰 28',NOW() - INTERVAL 11 DAY);

-- streak (id 11~30) : chapter 11~30
INSERT INTO `streak` (`id`,`user_id`,`chapter_id`,`streak_date`,`created_at`) VALUES
  (11,16,11,CURDATE() - INTERVAL 19 DAY,NOW() - INTERVAL 19 DAY),(12,17,12,CURDATE() - INTERVAL 18 DAY,NOW() - INTERVAL 18 DAY),
  (13,18,13,CURDATE() - INTERVAL 17 DAY,NOW() - INTERVAL 17 DAY),(14,19,14,CURDATE() - INTERVAL 16 DAY,NOW() - INTERVAL 16 DAY),
  (15,20,15,CURDATE() - INTERVAL 15 DAY,NOW() - INTERVAL 15 DAY),(16,21,16,CURDATE() - INTERVAL 14 DAY,NOW() - INTERVAL 14 DAY),
  (17,22,17,CURDATE() - INTERVAL 13 DAY,NOW() - INTERVAL 13 DAY),(18,23,18,CURDATE() - INTERVAL 12 DAY,NOW() - INTERVAL 12 DAY),
  (19,24,19,CURDATE() - INTERVAL 11 DAY,NOW() - INTERVAL 11 DAY),(20,25,20,CURDATE() - INTERVAL 10 DAY,NOW() - INTERVAL 10 DAY),
  (21,26,21,CURDATE() - INTERVAL 9 DAY,NOW() - INTERVAL 9 DAY),(22,27,22,CURDATE() - INTERVAL 8 DAY,NOW() - INTERVAL 8 DAY),
  (23,28,23,CURDATE() - INTERVAL 7 DAY,NOW() - INTERVAL 7 DAY),(24,29,24,CURDATE() - INTERVAL 6 DAY,NOW() - INTERVAL 6 DAY),
  (25,30,25,CURDATE() - INTERVAL 5 DAY,NOW() - INTERVAL 5 DAY),(26,31,26,CURDATE() - INTERVAL 4 DAY,NOW() - INTERVAL 4 DAY),
  (27,32,27,CURDATE() - INTERVAL 3 DAY,NOW() - INTERVAL 3 DAY),(28,33,28,CURDATE() - INTERVAL 2 DAY,NOW() - INTERVAL 2 DAY),
  (29,16,29,CURDATE() - INTERVAL 1 DAY,NOW() - INTERVAL 1 DAY),(30,17,30,CURDATE(),NOW() - INTERVAL 2 HOUR);

-- building (id 11~30)
INSERT INTO `building` (`id`,`user_id`,`category`,`position`,`level`,`created_at`) VALUES
  (11,16,'FITNESS',1,1,NOW() - INTERVAL 40 DAY),(12,17,'COOK',1,2,NOW() - INTERVAL 39 DAY),
  (13,18,'STUDY',1,1,NOW() - INTERVAL 38 DAY),(14,19,'BEAUTY',1,3,NOW() - INTERVAL 37 DAY),
  (15,20,'ART',1,1,NOW() - INTERVAL 36 DAY),(16,21,'FITNESS',2,2,NOW() - INTERVAL 35 DAY),
  (17,22,'COOK',2,1,NOW() - INTERVAL 34 DAY),(18,23,'STUDY',2,4,NOW() - INTERVAL 33 DAY),
  (19,24,'BEAUTY',2,1,NOW() - INTERVAL 32 DAY),(20,25,'ART',2,2,NOW() - INTERVAL 31 DAY),
  (21,26,'FITNESS',3,1,NOW() - INTERVAL 30 DAY),(22,27,'COOK',3,3,NOW() - INTERVAL 29 DAY),
  (23,28,'STUDY',3,1,NOW() - INTERVAL 28 DAY),(24,29,'BEAUTY',3,2,NOW() - INTERVAL 27 DAY),
  (25,30,'ART',3,1,NOW() - INTERVAL 26 DAY),(26,31,'FITNESS',4,5,NOW() - INTERVAL 25 DAY),
  (27,32,'COOK',4,1,NOW() - INTERVAL 24 DAY),(28,33,'STUDY',4,2,NOW() - INTERVAL 23 DAY),
  (29,16,'BEAUTY',5,1,NOW() - INTERVAL 22 DAY),(30,17,'ART',5,3,NOW() - INTERVAL 21 DAY);

-- calendar (id 11~30)
INSERT INTO `calendar` (`id`,`user_id`,`start`,`title`,`end`,`category`,`is_completed`,`created_at`) VALUES
  (11,16,CURDATE() + INTERVAL 1 DAY,'일정 11',NULL,'TODO',0,NOW() - INTERVAL 10 DAY),(12,17,CURDATE() + INTERVAL 2 DAY,'일정 12',NULL,'TODO',0,NOW() - INTERVAL 9 DAY),
  (13,18,CURDATE() + INTERVAL 3 DAY,'일정 13',NULL,'MEMO',0,NOW() - INTERVAL 9 DAY),(14,19,CURDATE() + INTERVAL 1 DAY,'일정 14',NULL,'TODO',0,NOW() - INTERVAL 8 DAY),
  (15,20,CURDATE() + INTERVAL 4 DAY,'일정 15',NULL,'TODO',0,NOW() - INTERVAL 8 DAY),(16,21,CURDATE() + INTERVAL 2 DAY,'일정 16',NULL,'MEMO',0,NOW() - INTERVAL 7 DAY),
  (17,22,CURDATE() + INTERVAL 5 DAY,'일정 17',NULL,'TODO',0,NOW() - INTERVAL 7 DAY),(18,23,CURDATE() + INTERVAL 1 DAY,'일정 18',NULL,'TODO',0,NOW() - INTERVAL 6 DAY),
  (19,24,CURDATE() + INTERVAL 3 DAY,'일정 19',NULL,'MEMO',0,NOW() - INTERVAL 6 DAY),(20,25,CURDATE() + INTERVAL 6 DAY,'일정 20',NULL,'TODO',0,NOW() - INTERVAL 5 DAY),
  (21,26,CURDATE() + INTERVAL 2 DAY,'일정 21',NULL,'TODO',0,NOW() - INTERVAL 5 DAY),(22,27,CURDATE() + INTERVAL 4 DAY,'일정 22',NULL,'MEMO',0,NOW() - INTERVAL 4 DAY),
  (23,28,CURDATE() + INTERVAL 1 DAY,'일정 23',NULL,'TODO',0,NOW() - INTERVAL 4 DAY),(24,29,CURDATE() + INTERVAL 7 DAY,'일정 24',NULL,'TODO',0,NOW() - INTERVAL 3 DAY),
  (25,30,CURDATE() + INTERVAL 2 DAY,'일정 25',NULL,'MEMO',0,NOW() - INTERVAL 3 DAY),(26,31,CURDATE() + INTERVAL 3 DAY,'일정 26',NULL,'TODO',0,NOW() - INTERVAL 2 DAY),
  (27,32,CURDATE() + INTERVAL 5 DAY,'일정 27',NULL,'TODO',0,NOW() - INTERVAL 2 DAY),(28,33,CURDATE() + INTERVAL 1 DAY,'일정 28',NULL,'MEMO',0,NOW() - INTERVAL 1 DAY),
  (29,16,CURDATE() + INTERVAL 8 DAY,'일정 29',NULL,'TODO',0,NOW() - INTERVAL 1 DAY),(30,17,CURDATE(),'일정 30',CURDATE(),'TODO',1,NOW() - INTERVAL 5 HOUR);

-- chat_room (id 5~24)
INSERT INTO `chat_room` (`id`,`created_at`) VALUES
  (5,NOW() - INTERVAL 20 DAY),(6,NOW() - INTERVAL 19 DAY),(7,NOW() - INTERVAL 18 DAY),(8,NOW() - INTERVAL 17 DAY),
  (9,NOW() - INTERVAL 16 DAY),(10,NOW() - INTERVAL 15 DAY),(11,NOW() - INTERVAL 14 DAY),(12,NOW() - INTERVAL 13 DAY),
  (13,NOW() - INTERVAL 12 DAY),(14,NOW() - INTERVAL 11 DAY),(15,NOW() - INTERVAL 10 DAY),(16,NOW() - INTERVAL 9 DAY),
  (17,NOW() - INTERVAL 8 DAY),(18,NOW() - INTERVAL 7 DAY),(19,NOW() - INTERVAL 6 DAY),(20,NOW() - INTERVAL 5 DAY),
  (21,NOW() - INTERVAL 4 DAY),(22,NOW() - INTERVAL 3 DAY),(23,NOW() - INTERVAL 2 DAY),(24,NOW() - INTERVAL 1 DAY);

-- chat_room_member (id 9~28) : room 5~24 각 멤버 1명 (room,user 유니크)
INSERT INTO `chat_room_member` (`id`,`room_id`,`user_id`,`joined_at`) VALUES
  (9,5,16,NOW() - INTERVAL 20 DAY),(10,6,17,NOW() - INTERVAL 19 DAY),(11,7,18,NOW() - INTERVAL 18 DAY),(12,8,19,NOW() - INTERVAL 17 DAY),
  (13,9,20,NOW() - INTERVAL 16 DAY),(14,10,21,NOW() - INTERVAL 15 DAY),(15,11,22,NOW() - INTERVAL 14 DAY),(16,12,23,NOW() - INTERVAL 13 DAY),
  (17,13,24,NOW() - INTERVAL 12 DAY),(18,14,25,NOW() - INTERVAL 11 DAY),(19,15,26,NOW() - INTERVAL 10 DAY),(20,16,27,NOW() - INTERVAL 9 DAY),
  (21,17,28,NOW() - INTERVAL 8 DAY),(22,18,29,NOW() - INTERVAL 7 DAY),(23,19,30,NOW() - INTERVAL 6 DAY),(24,20,31,NOW() - INTERVAL 5 DAY),
  (25,21,32,NOW() - INTERVAL 4 DAY),(26,22,33,NOW() - INTERVAL 3 DAY),(27,23,16,NOW() - INTERVAL 2 DAY),(28,24,17,NOW() - INTERVAL 1 DAY);

-- message (id 11~30) : room 5~24, sender 16~33
INSERT INTO `message` (`id`,`room_id`,`sender_id`,`content`,`is_read`,`created_at`) VALUES
  (11,5,16,'메시지 11',1,NOW() - INTERVAL 20 DAY),(12,6,17,'메시지 12',1,NOW() - INTERVAL 19 DAY),
  (13,7,18,'메시지 13',1,NOW() - INTERVAL 18 DAY),(14,8,19,'메시지 14',0,NOW() - INTERVAL 17 DAY),
  (15,9,20,'메시지 15',1,NOW() - INTERVAL 16 DAY),(16,10,21,'메시지 16',0,NOW() - INTERVAL 15 DAY),
  (17,11,22,'메시지 17',1,NOW() - INTERVAL 14 DAY),(18,12,23,'메시지 18',1,NOW() - INTERVAL 13 DAY),
  (19,13,24,'메시지 19',0,NOW() - INTERVAL 12 DAY),(20,14,25,'메시지 20',1,NOW() - INTERVAL 11 DAY),
  (21,15,26,'메시지 21',1,NOW() - INTERVAL 10 DAY),(22,16,27,'메시지 22',0,NOW() - INTERVAL 9 DAY),
  (23,17,28,'메시지 23',1,NOW() - INTERVAL 8 DAY),(24,18,29,'메시지 24',1,NOW() - INTERVAL 7 DAY),
  (25,19,30,'메시지 25',0,NOW() - INTERVAL 6 DAY),(26,20,31,'메시지 26',1,NOW() - INTERVAL 5 DAY),
  (27,21,32,'메시지 27',1,NOW() - INTERVAL 4 DAY),(28,22,33,'메시지 28',0,NOW() - INTERVAL 3 DAY),
  (29,23,16,'메시지 29',1,NOW() - INTERVAL 2 DAY),(30,24,17,'메시지 30',0,NOW() - INTERVAL 1 DAY);

-- friend (id 9~28) : (from,to) 유니크. from 14/15, to 16~25
INSERT INTO `friend` (`id`,`from_user_id`,`to_user_id`,`status`,`created_at`) VALUES
  (9,14,16,'FRIEND',NOW() - INTERVAL 40 DAY),(10,14,17,'FRIEND',NOW() - INTERVAL 39 DAY),
  (11,14,18,'FRIEND',NOW() - INTERVAL 38 DAY),(12,14,19,'SENT',NOW() - INTERVAL 2 DAY),
  (13,14,20,'FRIEND',NOW() - INTERVAL 37 DAY),(14,14,21,'FRIEND',NOW() - INTERVAL 36 DAY),
  (15,14,22,'FRIEND',NOW() - INTERVAL 35 DAY),(16,14,23,'SENT',NOW() - INTERVAL 1 DAY),
  (17,14,24,'FRIEND',NOW() - INTERVAL 34 DAY),(18,14,25,'FRIEND',NOW() - INTERVAL 33 DAY),
  (19,15,16,'FRIEND',NOW() - INTERVAL 32 DAY),(20,15,17,'FRIEND',NOW() - INTERVAL 31 DAY),
  (21,15,18,'SENT',NOW() - INTERVAL 3 HOUR),(22,15,19,'FRIEND',NOW() - INTERVAL 30 DAY),
  (23,15,20,'FRIEND',NOW() - INTERVAL 29 DAY),(24,15,21,'FRIEND',NOW() - INTERVAL 28 DAY),
  (25,15,22,'BLOCK',NOW() - INTERVAL 5 DAY),(26,15,23,'FRIEND',NOW() - INTERVAL 27 DAY),
  (27,15,24,'FRIEND',NOW() - INTERVAL 26 DAY),(28,15,25,'FRIEND',NOW() - INTERVAL 25 DAY);

-- guestbook (id 9~28)
INSERT INTO `guestbook` (`id`,`writer_id`,`owner_id`,`content`,`is_read`,`created_at`) VALUES
  (9,16,17,'방명록 9',0,NOW() - INTERVAL 18 DAY),(10,17,18,'방명록 10',1,NOW() - INTERVAL 17 DAY),
  (11,18,19,'방명록 11',0,NOW() - INTERVAL 16 DAY),(12,19,20,'방명록 12',1,NOW() - INTERVAL 15 DAY),
  (13,20,21,'방명록 13',0,NOW() - INTERVAL 14 DAY),(14,21,22,'방명록 14',1,NOW() - INTERVAL 13 DAY),
  (15,22,23,'방명록 15',0,NOW() - INTERVAL 12 DAY),(16,23,24,'방명록 16',1,NOW() - INTERVAL 11 DAY),
  (17,24,25,'방명록 17',0,NOW() - INTERVAL 10 DAY),(18,25,26,'방명록 18',1,NOW() - INTERVAL 9 DAY),
  (19,26,27,'방명록 19',0,NOW() - INTERVAL 8 DAY),(20,27,28,'방명록 20',1,NOW() - INTERVAL 7 DAY),
  (21,28,29,'방명록 21',0,NOW() - INTERVAL 6 DAY),(22,29,30,'방명록 22',1,NOW() - INTERVAL 5 DAY),
  (23,30,31,'방명록 23',0,NOW() - INTERVAL 4 DAY),(24,31,32,'방명록 24',1,NOW() - INTERVAL 3 DAY),
  (25,32,33,'방명록 25',0,NOW() - INTERVAL 2 DAY),(26,33,16,'방명록 26',1,NOW() - INTERVAL 1 DAY),
  (27,16,18,'방명록 27',0,NOW() - INTERVAL 12 HOUR),(28,17,19,'방명록 28',0,NOW() - INTERVAL 6 HOUR);

-- notification (id 11~30)
INSERT INTO `notification` (`id`,`user_id`,`type`,`ref_id`,`message`,`created_at`) VALUES
  (11,16,'ENROLLMENT',11,'알림 11',NOW() - INTERVAL 40 DAY),(12,17,'FRIEND_REQUEST',9,'알림 12',NOW() - INTERVAL 2 DAY),
  (13,18,'NOTICE',1,'알림 13',NOW() - INTERVAL 38 DAY),(14,19,'MESSAGE',14,'알림 14',NOW() - INTERVAL 17 DAY),
  (15,20,'GUESTBOOK',13,'알림 15',NOW() - INTERVAL 14 DAY),(16,21,'ENROLLMENT',16,'알림 16',NOW() - INTERVAL 35 DAY),
  (17,22,'FRIEND_REQUEST',15,'알림 17',NOW() - INTERVAL 1 DAY),(18,23,'NOTICE',1,'알림 18',NOW() - INTERVAL 33 DAY),
  (19,24,'MESSAGE',19,'알림 19',NOW() - INTERVAL 12 DAY),(20,25,'ENROLLMENT',20,'알림 20',NOW() - INTERVAL 31 DAY),
  (21,26,'GUESTBOOK',19,'알림 21',NOW() - INTERVAL 8 DAY),(22,27,'NOTICE',1,'알림 22',NOW() - INTERVAL 29 DAY),
  (23,28,'ENROLLMENT',23,'알림 23',NOW() - INTERVAL 28 DAY),(24,29,'MESSAGE',24,'알림 24',NOW() - INTERVAL 7 DAY),
  (25,30,'FRIEND_REQUEST',23,'알림 25',NOW() - INTERVAL 5 HOUR),(26,31,'ENROLLMENT',26,'알림 26',NOW() - INTERVAL 25 DAY),
  (27,32,'NOTICE',1,'알림 27',NOW() - INTERVAL 24 DAY),(28,33,'GUESTBOOK',25,'알림 28',NOW() - INTERVAL 2 DAY),
  (29,16,'MESSAGE',29,'알림 29',NOW() - INTERVAL 2 DAY),(30,17,'ENROLLMENT',30,'알림 30',NOW() - INTERVAL 1 DAY);

-- verification_code (id 7~26)
INSERT INTO `verification_code` (`id`,`user_id`,`email`,`code`,`purpose`,`expires_at`,`used`) VALUES
  (7,14,'teacher5@momo.city','100014','SIGNUP',NOW() - INTERVAL 50 DAY,1),(8,15,'teacher6@momo.city','100015','SIGNUP',NOW() - INTERVAL 48 DAY,1),
  (9,16,'teacher7@momo.city','100016','SIGNUP',NOW() - INTERVAL 46 DAY,1),(10,17,'pageuser17@momo.city','100017','SIGNUP',NOW() - INTERVAL 44 DAY,1),
  (11,18,'pageuser18@momo.city','100018','SIGNUP',NOW() - INTERVAL 42 DAY,1),(12,19,'pageuser19@momo.city','100019','SIGNUP',NOW() - INTERVAL 40 DAY,1),
  (13,20,'pageuser20@momo.city','100020','SIGNUP',NOW() - INTERVAL 38 DAY,1),(14,21,'pageuser21@momo.city','100021','PASSWORD_RESET',NOW() + INTERVAL 9 MINUTE,0),
  (15,22,'pageuser22@momo.city','100022','SIGNUP',NOW() - INTERVAL 34 DAY,1),(16,23,'pageuser23@momo.city','100023','SIGNUP',NOW() - INTERVAL 32 DAY,1),
  (17,24,'pageuser24@momo.city','100024','EMAIL_CHANGE',NOW() + INTERVAL 8 MINUTE,0),(18,25,'pageuser25@momo.city','100025','SIGNUP',NOW() - INTERVAL 28 DAY,1),
  (19,26,'pageuser26@momo.city','100026','SIGNUP',NOW() - INTERVAL 26 DAY,1),(20,27,'pageuser27@momo.city','100027','SIGNUP',NOW() - INTERVAL 24 DAY,1),
  (21,28,'pageuser28@momo.city','100028','SIGNUP',NOW() - INTERVAL 22 DAY,1),(22,29,'pageuser29@momo.city','100029','PASSWORD_RESET',NOW() + INTERVAL 7 MINUTE,0),
  (23,30,'pageuser30@momo.city','100030','SIGNUP',NOW() - INTERVAL 18 DAY,1),(24,31,'pageuser31@momo.city','100031','SIGNUP',NOW() - INTERVAL 16 DAY,1),
  (25,32,'pageuser32@momo.city','100032','SIGNUP',NOW() - INTERVAL 14 DAY,1),(26,33,'pageuser33@momo.city','100033','SIGNUP',NOW() - INTERVAL 12 DAY,1);

-- access_log (id 11~30)
INSERT INTO `access_log` (`id`,`user_id`,`ip`,`action`,`created_at`) VALUES
  (11,16,'10.0.1.16','LOGIN',NOW() - INTERVAL 50 MINUTE),(12,17,'10.0.1.17','LOGIN',NOW() - INTERVAL 48 MINUTE),
  (13,18,'10.0.1.18','VIEW_LECTURE',NOW() - INTERVAL 46 MINUTE),(14,19,'10.0.1.19','LOGIN',NOW() - INTERVAL 44 MINUTE),
  (15,20,'10.0.1.20','ENROLL',NOW() - INTERVAL 42 MINUTE),(16,21,'10.0.1.21','LOGIN',NOW() - INTERVAL 40 MINUTE),
  (17,22,'10.0.1.22','LOGOUT',NOW() - INTERVAL 38 MINUTE),(18,23,'10.0.1.23','LOGIN',NOW() - INTERVAL 36 MINUTE),
  (19,24,'10.0.1.24','VIEW_LECTURE',NOW() - INTERVAL 34 MINUTE),(20,25,'10.0.1.25','LOGIN',NOW() - INTERVAL 32 MINUTE),
  (21,26,'10.0.1.26','ENROLL',NOW() - INTERVAL 30 MINUTE),(22,27,'10.0.1.27','LOGIN',NOW() - INTERVAL 28 MINUTE),
  (23,28,'10.0.1.28','LOGOUT',NOW() - INTERVAL 26 MINUTE),(24,29,'10.0.1.29','LOGIN',NOW() - INTERVAL 24 MINUTE),
  (25,30,'10.0.1.30','VIEW_LECTURE',NOW() - INTERVAL 22 MINUTE),(26,31,'10.0.1.31','LOGIN',NOW() - INTERVAL 20 MINUTE),
  (27,32,'10.0.1.32','ENROLL',NOW() - INTERVAL 18 MINUTE),(28,33,'10.0.1.33','LOGIN',NOW() - INTERVAL 16 MINUTE),
  (29,16,'10.0.1.16','VIEW_DASHBOARD',NOW() - INTERVAL 14 MINUTE),(30,NULL,'203.0.113.50','LOGIN_FAILED',NOW() - INTERVAL 12 MINUTE);

-- report (id 13~32) : reporter_user_id 신고자 / 처리완료 건 handler_admin_id=1
INSERT INTO `report`
  (`id`,`reporter_user_id`,`target_type`,`target_id`,`reason`,`detail`,`status`,`created_at`,`reported_at`,`updated_at`,`handled_at`,`handler_admin_id`) VALUES
  (13,16,'POST'   ,11,'SPAM'         ,'신고 13',  'PENDING' ,NOW() - INTERVAL 10 MINUTE,NOW() - INTERVAL 10 MINUTE,NOW() - INTERVAL 10 MINUTE,NULL,NULL),
  (14,17,'COMMENT',13,'ABUSE'        ,'신고 14',  'PENDING' ,NOW() - INTERVAL 30 MINUTE,NOW() - INTERVAL 30 MINUTE,NOW() - INTERVAL 30 MINUTE,NULL,NULL),
  (15,18,'POST'   ,12,'INAPPROPRIATE','신고 15',  'PENDING' ,NOW() - INTERVAL 1 HOUR,NOW() - INTERVAL 1 HOUR,NOW() - INTERVAL 1 HOUR,NULL,NULL),
  (16,19,'USER'   ,22,'OTHER'          ,'신고 16',  'REVIEWING',NOW() - INTERVAL 1 DAY,NOW() - INTERVAL 1 DAY,NOW() - INTERVAL 20 HOUR,NOW() - INTERVAL 20 HOUR,1),
  (17,20,'COMMENT',15,'SPAM'         ,'신고 17',  'PENDING' ,NOW() - INTERVAL 2 HOUR,NOW() - INTERVAL 2 HOUR,NOW() - INTERVAL 2 HOUR,NULL,NULL),
  (18,21,'POST'   ,14,'ABUSE'        ,'신고 18',  'CONFIRMED',NOW() - INTERVAL 2 DAY,NOW() - INTERVAL 2 DAY,NOW() - INTERVAL 1 DAY,NOW() - INTERVAL 1 DAY,1),
  (19,22,'USER'   ,25,'INAPPROPRIATE','신고 19',  'PENDING' ,NOW() - INTERVAL 3 HOUR,NOW() - INTERVAL 3 HOUR,NOW() - INTERVAL 3 HOUR,NULL,NULL),
  (20,23,'LECTURE',13,'OTHER'          ,'신고 20',  'REJECTED',NOW() - INTERVAL 3 DAY,NOW() - INTERVAL 3 DAY,NOW() - INTERVAL 2 DAY,NOW() - INTERVAL 2 DAY,1),
  (21,24,'POST'   ,16,'SPAM'         ,'신고 21',  'PENDING' ,NOW() - INTERVAL 4 HOUR,NOW() - INTERVAL 4 HOUR,NOW() - INTERVAL 4 HOUR,NULL,NULL),
  (22,25,'COMMENT',17,'ABUSE'        ,'신고 22',  'REVIEWING',NOW() - INTERVAL 26 HOUR,NOW() - INTERVAL 26 HOUR,NOW() - INTERVAL 22 HOUR,NOW() - INTERVAL 22 HOUR,1),
  (23,26,'POST'   ,18,'INAPPROPRIATE','신고 23',  'PENDING' ,NOW() - INTERVAL 5 HOUR,NOW() - INTERVAL 5 HOUR,NOW() - INTERVAL 5 HOUR,NULL,NULL),
  (24,27,'USER'   ,16,'OTHER'          ,'신고 24',  'CONFIRMED',NOW() - INTERVAL 4 DAY,NOW() - INTERVAL 4 DAY,NOW() - INTERVAL 3 DAY,NOW() - INTERVAL 3 DAY,1),
  (25,28,'LECTURE',14,'SPAM'         ,'신고 25',  'PENDING' ,NOW() - INTERVAL 6 HOUR,NOW() - INTERVAL 6 HOUR,NOW() - INTERVAL 6 HOUR,NULL,NULL),
  (26,29,'POST'   ,19,'ABUSE'        ,'신고 26',  'REJECTED',NOW() - INTERVAL 5 DAY,NOW() - INTERVAL 5 DAY,NOW() - INTERVAL 4 DAY,NOW() - INTERVAL 4 DAY,1),
  (27,30,'COMMENT',20,'INAPPROPRIATE','신고 27',  'PENDING' ,NOW() - INTERVAL 7 HOUR,NOW() - INTERVAL 7 HOUR,NOW() - INTERVAL 7 HOUR,NULL,NULL),
  (28,31,'USER'   ,33,'OTHER'          ,'신고 28',  'REVIEWING',NOW() - INTERVAL 30 HOUR,NOW() - INTERVAL 30 HOUR,NOW() - INTERVAL 28 HOUR,NOW() - INTERVAL 28 HOUR,1),
  (29,32,'POST'   ,21,'SPAM'         ,'신고 29',  'PENDING' ,NOW() - INTERVAL 8 HOUR,NOW() - INTERVAL 8 HOUR,NOW() - INTERVAL 8 HOUR,NULL,NULL),
  (30,33,'COMMENT',22,'ABUSE'        ,'신고 30',  'CONFIRMED',NOW() - INTERVAL 6 DAY,NOW() - INTERVAL 6 DAY,NOW() - INTERVAL 5 DAY,NOW() - INTERVAL 5 DAY,1),
  (31,16,'POST'   ,23,'INAPPROPRIATE','신고 31',  'PENDING' ,NOW() - INTERVAL 9 HOUR,NOW() - INTERVAL 9 HOUR,NOW() - INTERVAL 9 HOUR,NULL,NULL),
  (32,17,'LECTURE',15,'OTHER'          ,'신고 32',  'PENDING' ,NOW() - INTERVAL 11 HOUR,NOW() - INTERVAL 11 HOUR,NOW() - INTERVAL 11 HOUR,NULL,NULL);

-- payment (id 9~28)
INSERT INTO `payment` (`id`,`user_id`,`amount`,`method`,`paid_at`) VALUES
  (9,16,19900,'KAKAO',NOW() - INTERVAL 40 DAY),(10,17,29900,'TOSS',NOW() - INTERVAL 38 DAY),
  (11,18,19900,'CARD',NOW() - INTERVAL 36 DAY),(12,19,9900,'FREE',NOW() - INTERVAL 34 DAY),
  (13,20,39900,'KAKAO',NOW() - INTERVAL 32 DAY),(14,21,19900,'TOSS',NOW() - INTERVAL 30 DAY),
  (15,22,19900,'CARD',NOW() - INTERVAL 28 DAY),(16,23,9900,'FREE',NOW() - INTERVAL 26 DAY),
  (17,24,29900,'KAKAO',NOW() - INTERVAL 24 DAY),(18,25,19900,'TOSS',NOW() - INTERVAL 22 DAY),
  (19,26,19900,'CARD',NOW() - INTERVAL 20 DAY),(20,27,39900,'KAKAO',NOW() - INTERVAL 18 DAY),
  (21,28,9900,'FREE',NOW() - INTERVAL 16 DAY),(22,29,19900,'TOSS',NOW() - INTERVAL 14 DAY),
  (23,30,19900,'CARD',NOW() - INTERVAL 12 DAY),(24,31,29900,'KAKAO',NOW() - INTERVAL 10 DAY),
  (25,32,19900,'TOSS',NOW() - INTERVAL 8 DAY),(26,33,9900,'FREE',NOW() - INTERVAL 6 DAY),
  (27,16,19900,'CARD',NOW() - INTERVAL 4 DAY),(28,17,39900,'KAKAO',NOW() - INTERVAL 2 DAY);

-- inquiry (id 9~28)
INSERT INTO `inquiry` (`id`,`user_id`,`title`,`content`,`answer`,`status`,`created_at`,`answered_at`) VALUES
  (9,16,'문의 9','내용 9',NULL,'WAITING',NOW() - INTERVAL 5 HOUR,NULL),(10,17,'문의 10','내용 10','답변 10','ANSWERED',NOW() - INTERVAL 2 DAY,NOW() - INTERVAL 1 DAY),
  (11,18,'문의 11','내용 11',NULL,'WAITING',NOW() - INTERVAL 4 HOUR,NULL),(12,19,'문의 12','내용 12','답변 12','ANSWERED',NOW() - INTERVAL 3 DAY,NOW() - INTERVAL 2 DAY),
  (13,20,'문의 13','내용 13',NULL,'WAITING',NOW() - INTERVAL 3 HOUR,NULL),(14,21,'문의 14','내용 14','답변 14','CLOSED',NOW() - INTERVAL 4 DAY,NOW() - INTERVAL 3 DAY),
  (15,22,'문의 15','내용 15',NULL,'WAITING',NOW() - INTERVAL 2 HOUR,NULL),(16,23,'문의 16','내용 16','답변 16','ANSWERED',NOW() - INTERVAL 5 DAY,NOW() - INTERVAL 4 DAY),
  (17,24,'문의 17','내용 17',NULL,'WAITING',NOW() - INTERVAL 90 MINUTE,NULL),(18,25,'문의 18','내용 18','답변 18','ANSWERED',NOW() - INTERVAL 6 DAY,NOW() - INTERVAL 5 DAY),
  (19,26,'문의 19','내용 19',NULL,'WAITING',NOW() - INTERVAL 80 MINUTE,NULL),(20,27,'문의 20','내용 20','답변 20','CLOSED',NOW() - INTERVAL 7 DAY,NOW() - INTERVAL 6 DAY),
  (21,28,'문의 21','내용 21',NULL,'WAITING',NOW() - INTERVAL 70 MINUTE,NULL),(22,29,'문의 22','내용 22','답변 22','ANSWERED',NOW() - INTERVAL 8 DAY,NOW() - INTERVAL 7 DAY),
  (23,30,'문의 23','내용 23',NULL,'WAITING',NOW() - INTERVAL 60 MINUTE,NULL),(24,31,'문의 24','내용 24','답변 24','ANSWERED',NOW() - INTERVAL 9 DAY,NOW() - INTERVAL 8 DAY),
  (25,32,'문의 25','내용 25',NULL,'WAITING',NOW() - INTERVAL 50 MINUTE,NULL),(26,33,'문의 26','내용 26','답변 26','CLOSED',NOW() - INTERVAL 10 DAY,NOW() - INTERVAL 9 DAY),
  (27,16,'문의 27','내용 27',NULL,'WAITING',NOW() - INTERVAL 40 MINUTE,NULL),(28,17,'문의 28','내용 28','답변 28','ANSWERED',NOW() - INTERVAL 11 DAY,NOW() - INTERVAL 10 DAY);

-- error_log (id 13~32)
INSERT INTO `error_log` (`id`,`level`,`source`,`message`,`occurred_at`,`created_at`,`updated_at`) VALUES
  (13,'ERROR','API Error','Page error 13',NOW() - INTERVAL 25 MINUTE,NOW() - INTERVAL 25 MINUTE,NOW() - INTERVAL 25 MINUTE),
  (14,'WARNING','Frontend','Page error 14',NOW() - INTERVAL 35 MINUTE,NOW() - INTERVAL 35 MINUTE,NOW() - INTERVAL 35 MINUTE),
  (15,'ERROR','Server','Page error 15',NOW() - INTERVAL 55 MINUTE,NOW() - INTERVAL 55 MINUTE,NOW() - INTERVAL 55 MINUTE),
  (16,'CRITICAL','Database','Page error 16',NOW() - INTERVAL 90 MINUTE,NOW() - INTERVAL 90 MINUTE,NOW() - INTERVAL 90 MINUTE),
  (17,'WARNING','API Error','Page error 17',NOW() - INTERVAL 4 HOUR,NOW() - INTERVAL 4 HOUR,NOW() - INTERVAL 4 HOUR),
  (18,'ERROR','Server','Page error 18',NOW() - INTERVAL 6 HOUR,NOW() - INTERVAL 6 HOUR,NOW() - INTERVAL 6 HOUR),
  (19,'WARNING','Frontend','Page error 19',NOW() - INTERVAL 8 HOUR,NOW() - INTERVAL 8 HOUR,NOW() - INTERVAL 8 HOUR),
  (20,'CRITICAL','Server','Page error 20',NOW() - INTERVAL 10 HOUR,NOW() - INTERVAL 10 HOUR,NOW() - INTERVAL 10 HOUR),
  (21,'ERROR','Database','Page error 21',NOW() - INTERVAL 12 HOUR,NOW() - INTERVAL 12 HOUR,NOW() - INTERVAL 12 HOUR),
  (22,'WARNING','API Error','Page error 22',NOW() - INTERVAL 16 HOUR,NOW() - INTERVAL 16 HOUR,NOW() - INTERVAL 16 HOUR),
  (23,'ERROR','Server','Page error 23',NOW() - INTERVAL 20 HOUR,NOW() - INTERVAL 20 HOUR,NOW() - INTERVAL 20 HOUR),
  (24,'WARNING','Frontend','Page error 24',NOW() - INTERVAL 24 HOUR,NOW() - INTERVAL 24 HOUR,NOW() - INTERVAL 24 HOUR),
  (25,'CRITICAL','Database','Page error 25',NOW() - INTERVAL 28 HOUR,NOW() - INTERVAL 28 HOUR,NOW() - INTERVAL 28 HOUR),
  (26,'ERROR','API Error','Page error 26',NOW() - INTERVAL 2 DAY,NOW() - INTERVAL 2 DAY,NOW() - INTERVAL 2 DAY),
  (27,'WARNING','Server','Page error 27',NOW() - INTERVAL 2 DAY - INTERVAL 6 HOUR,NOW() - INTERVAL 2 DAY - INTERVAL 6 HOUR,NOW() - INTERVAL 2 DAY - INTERVAL 6 HOUR),
  (28,'ERROR','Database','Page error 28',NOW() - INTERVAL 3 DAY,NOW() - INTERVAL 3 DAY,NOW() - INTERVAL 3 DAY),
  (29,'WARNING','Frontend','Page error 29',NOW() - INTERVAL 3 DAY - INTERVAL 8 HOUR,NOW() - INTERVAL 3 DAY - INTERVAL 8 HOUR,NOW() - INTERVAL 3 DAY - INTERVAL 8 HOUR),
  (30,'CRITICAL','Server','Page error 30',NOW() - INTERVAL 4 DAY,NOW() - INTERVAL 4 DAY,NOW() - INTERVAL 4 DAY),
  (31,'ERROR','API Error','Page error 31',NOW() - INTERVAL 5 DAY,NOW() - INTERVAL 5 DAY,NOW() - INTERVAL 5 DAY),
  (32,'WARNING','Database','Page error 32',NOW() - INTERVAL 6 DAY,NOW() - INTERVAL 6 DAY,NOW() - INTERVAL 6 DAY);

-- #####################################################################
-- ##  STEP 4. lecture 썸네일 채우기  [FE 요청 - 더미 썸네일에 아무 이미지]
-- ##   - picsum.photos seed 방식: 강의 id 별로 다른 실제 사진이 뜬다.
-- ##   - seed 고정이라 새로고침해도 같은 이미지(데모 일관성). 400x250 비율.
-- #####################################################################
UPDATE `lecture`
SET `thumbnail_url` = CONCAT('https://picsum.photos/seed/momolecture', `id`, '/400/250')
WHERE `id` > 0 AND `thumbnail_url` IS NULL;   -- id(PK) 조건: Workbench Safe Update Mode(Error 1175) 통과용

-- =====================================================================
--  END OF SEED  (25 tables + error_log / 기존 236행 + 페이지네이션 +520행)
--  + user status 규칙(학생 REJECTED/PENDING 제외, 강사 다양화, DELETED 포함)
--  + lecture 썸네일 picsum 적용
-- =====================================================================
