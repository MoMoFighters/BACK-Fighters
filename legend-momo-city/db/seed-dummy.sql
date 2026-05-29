-- =====================================================================
--  MoMo City - 더미 데이터 시드 스크립트 (개발/시연용)  [LIVE 버전]
--  대상 DB : momo (MySQL, utf8mb4)
-- ---------------------------------------------------------------------
--  [이 버전의 특징 - "방금 전까지 운영되던 서비스"처럼]
--   - 모든 시간 컬럼을 고정 날짜가 아니라 NOW()/CURDATE() 기준 상대값으로 박았다.
--   - 따라서 이 스크립트를 "언제 실행하든" 데이터가 항상 방금까지 살아있던 것처럼 보인다.
--     예) 가장 최근 신고 = 약 4분 전 / 최신 에러로그 = 약 2분 전 / 가입 = 수개월 전
--   - 신규일수록 PENDING(미처리), 오래될수록 RESOLVED/REJECTED(처리완료) 로 운영 흐름 재현.
--   - ※ NOW()/INTERVAL 은 값 계산 표현식일 뿐, ERD/DDL(테이블 구조)에는 영향 없음.
-- ---------------------------------------------------------------------
--  [실행 전제]
--   1) DB 'momo' 에 ver3.4 DDL(수정본)이 이미 적용돼 있어야 한다.
--      - 수정 ① user.email : NOT NULL -> NULL (널 허용)
--      - 수정 ② category ENUM : 'HEALTH' -> 'FITNESS' (user/lecture/building 3곳)
--   2) 앱을 1회 bootRun 하여 Hibernate(ddl-auto:update)가 'error_log' 테이블을
--      먼저 생성한 상태여야 한다.
-- ---------------------------------------------------------------------
--  [로그인 정보] 비밀번호 평문 : password123 (bcrypt, 검증 완료) / id=12 카카오는 널
--  [주의] 멱등 아님. 재실행 시 PK 중복 -> 재시드하려면 TRUNCATE 후 실행.
--         birth(생년월일)만 고정 리터럴(절대값) 유지, 나머지 시간은 전부 상대값.
-- =====================================================================

-- =====================================================================
--  1. user  (13명 - status ENUM 6종 전부 / 가입은 수개월~수일 전으로 분산)
-- =====================================================================
INSERT INTO `user`
  (`id`,`email`,`password`,`name`,`nickname`,`birth`,`profile_image_url`,`role`,`status`,`category`,`proof`,`point`,`is_paid`,`do_not_disturb`,`created_at`,`deleted_at`,`is_tempPWD`)
VALUES
  (1 ,'admin@momo.city'   ,'$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','관리자','momo_admin' ,'1990-01-01',NULL,'ADMIN'  ,'ACTIVE'  ,NULL     ,NULL,5000,1,0,NOW() - INTERVAL 147 DAY,NULL,0),
  (2 ,'student1@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','김민수','minsu'      ,'1998-03-12',NULL,'STUDENT','ACTIVE'  ,NULL     ,NULL,1200,1,0,NOW() - INTERVAL 108 DAY,NULL,0),
  (3 ,'student2@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','이지영','jiyoung'    ,'1999-07-05',NULL,'STUDENT','ACTIVE'  ,NULL     ,NULL, 300,0,0,NOW() - INTERVAL 103 DAY,NULL,0),
  (4 ,'student3@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','박현우','hyunwoo'    ,'2000-11-23',NULL,'STUDENT','ACTIVE'  ,NULL     ,NULL, 850,1,1,NOW() - INTERVAL 89 DAY ,NULL,0),
  (5 ,'student4@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','최서연','seoyeon'    ,'1997-05-30',NULL,'STUDENT','ACTIVE'  ,NULL     ,NULL,  50,0,0,NOW() - INTERVAL 78 DAY ,NULL,0),
  (6 ,'teacher1@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','김강사','coach_kim'  ,'1988-09-09',NULL,'TEACHER','ACTIVE'  ,'FITNESS','https://momo.city/proof/kim.pdf',2000,1,0,NOW() - INTERVAL 129 DAY,NULL,0),
  (7 ,'teacher2@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','박강사','chef_park'  ,'1985-12-01',NULL,'TEACHER','ACTIVE'  ,'COOK'   ,'https://momo.city/proof/park.pdf',1700,1,0,NOW() - INTERVAL 124 DAY,NULL,0),
  (8 ,'pending@momo.city' ,'$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','정대기','wannabe'    ,'1995-04-18',NULL,'STUDENT','PENDING' ,'STUDY'  ,'https://momo.city/proof/wannabe.pdf',0,0,0,NOW() - INTERVAL 9 DAY ,NULL,0),
  (9 ,'rejected@momo.city','$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','한거절','rejected_t' ,'1993-08-08',NULL,'STUDENT','REJECTED','ART'    ,'https://momo.city/proof/rejected.pdf',0,0,0,NOW() - INTERVAL 19 DAY,NULL,0),
  (10,'banned@momo.city'  ,'$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','노정지','banned_user','1996-06-06',NULL,'STUDENT','BANNED'  ,NULL     ,NULL,0,0,0,NOW() - INTERVAL 58 DAY,NULL,0),
  (11,'black@momo.city'   ,'$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','영구정','black_user' ,'1994-02-14',NULL,'STUDENT','BLACK'   ,NULL     ,NULL,0,0,0,NOW() - INTERVAL 68 DAY,NULL,0),
  (12,NULL                ,NULL                                                          ,'카카오','kakao_user' ,'2001-10-10',NULL,'STUDENT','ACTIVE'  ,NULL     ,NULL, 100,0,0,NOW() - INTERVAL 24 DAY,NULL,0),
  (13,'left@momo.city'    ,'$2a$10$UbdCvu2c/oz0u9pwp8qD.e1BqOoQdTL1WHicM5odz5OfUChV6lfSm','떠난이','left_user'  ,'1992-01-01',NULL,'STUDENT','DELETED' ,NULL     ,NULL,0,0,0,NOW() - INTERVAL 118 DAY,NOW() - INTERVAL 14 DAY,0);

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
-- ※ 실제 스키마 반영: reporter_user_id(NOT NULL, reporter_id 와 동일인) / reported_at(NOT NULL, 접수시각)
--   / updated_at(NOT NULL) 추가. 처리완료(REVIEWED/RESOLVED/REJECTED) 건만 handled_at + handler_admin_id(관리자=1) 채움.
--   created_at = reported_at 로 맞춰 최근순 정렬 일관성 확보. PENDING 은 handled_at/handler NULL.
INSERT INTO `report`
  (`id`,`reporter_id`,`reporter_user_id`,`target_type`,`target_id`,`target_nickname`,`reason`,`reason_detail`,`status`,`created_at`,`reported_at`,`updated_at`,`handled_at`,`handler_admin_id`)
VALUES
  (1 ,2,2,'USER'   ,10,'banned_user','SPAM'         ,'DM으로 광고를 계속 보냅니다.'      ,'PENDING' ,NOW() - INTERVAL 4 MINUTE ,NOW() - INTERVAL 4 MINUTE ,NOW() - INTERVAL 4 MINUTE ,NULL                  ,NULL),
  (2 ,3,3,'COMMENT',11,'banned_user','ABUSE'        ,'댓글에 욕설과 비방이 있습니다.'     ,'PENDING' ,NOW() - INTERVAL 51 MINUTE,NOW() - INTERVAL 51 MINUTE,NOW() - INTERVAL 51 MINUTE,NULL                  ,NULL),
  (3 ,4,4,'COMMENT',12,'black_user' ,'SPAM'         ,'스팸 광고 댓글을 도배합니다.'      ,'PENDING' ,NOW() - INTERVAL 19 HOUR  ,NOW() - INTERVAL 19 HOUR  ,NOW() - INTERVAL 19 HOUR  ,NULL                  ,NULL),
  (4 ,5,5,'POST'   ,8 ,'jiyoung'    ,'INAPPROPRIATE','게시글에 부적절한 이미지가 있어요.','REVIEWED',NOW() - INTERVAL 2 DAY    ,NOW() - INTERVAL 2 DAY    ,NOW() - INTERVAL 44 HOUR  ,NOW() - INTERVAL 44 HOUR,1),
  (5 ,2,2,'USER'   ,11,'black_user' ,'ABUSE'        ,'지속적으로 비방 메시지를 보냅니다.','RESOLVED',NOW() - INTERVAL 3 DAY    ,NOW() - INTERVAL 3 DAY    ,NOW() - INTERVAL 2 DAY    ,NOW() - INTERVAL 2 DAY  ,1),
  (6 ,3,3,'LECTURE',10,'chef_park'  ,'ETC'          ,'강의 내용이 설명과 다릅니다.'      ,'PENDING' ,NOW() - INTERVAL 5 HOUR   ,NOW() - INTERVAL 5 HOUR   ,NOW() - INTERVAL 5 HOUR   ,NULL                  ,NULL),
  (7 ,4,4,'POST'   ,2 ,'minsu'      ,'SPAM'         ,'홍보성 글로 의심됩니다.'          ,'REJECTED',NOW() - INTERVAL 4 DAY - INTERVAL 3 HOUR,NOW() - INTERVAL 4 DAY - INTERVAL 3 HOUR,NOW() - INTERVAL 4 DAY,NOW() - INTERVAL 4 DAY,1),
  (8 ,5,5,'USER'   ,10,'banned_user','ABUSE'        ,'욕설이 담긴 DM을 받았습니다.'      ,'REVIEWED',NOW() - INTERVAL 26 HOUR  ,NOW() - INTERVAL 26 HOUR  ,NOW() - INTERVAL 20 HOUR  ,NOW() - INTERVAL 20 HOUR,1),
  (9 ,6,6,'COMMENT',11,'banned_user','INAPPROPRIATE','외부 광고 링크를 첨부했습니다.'   ,'PENDING' ,NOW() - INTERVAL 23 MINUTE,NOW() - INTERVAL 23 MINUTE,NOW() - INTERVAL 23 MINUTE,NULL                  ,NULL),
  (10,2,2,'POST'   ,9 ,'hyunwoo'    ,'ETC'          ,'동일 내용을 중복 게시했습니다.'    ,'RESOLVED',NOW() - INTERVAL 4 DAY    ,NOW() - INTERVAL 4 DAY    ,NOW() - INTERVAL 3 DAY    ,NOW() - INTERVAL 3 DAY  ,1),
  (11,7,7,'USER'   ,11,'black_user' ,'SPAM'         ,'대량으로 친구신청 스팸을 보냅니다.','PENDING' ,NOW() - INTERVAL 2 HOUR   ,NOW() - INTERVAL 2 HOUR   ,NOW() - INTERVAL 2 HOUR   ,NULL                  ,NULL),
  (12,3,3,'LECTURE',5 ,'coach_kim'  ,'ETC'          ,'강의 일정을 지키지 않았습니다.'    ,'REJECTED',NOW() - INTERVAL 6 DAY    ,NOW() - INTERVAL 6 DAY    ,NOW() - INTERVAL 5 DAY    ,NOW() - INTERVAL 5 DAY  ,1);

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

-- =====================================================================
--  END OF SEED  (25 tables + error_log)
-- =====================================================================
