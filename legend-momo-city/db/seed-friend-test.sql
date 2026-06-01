-- =====================================================================
--  MoMo City - friend BC 기능 테스트 전용 더미 (서정림님 요청)
--  대상 DB : momo (MySQL)
-- ---------------------------------------------------------------------
--  [무엇을 하는 파일인가]
--   친구/사용자검색/친구요청/채팅방/메시지 기능을 시나리오별로 테스트하기 위한
--   friend BC 전용 더미. friend / chat_room / chat_room_member / message
--   "4개 테이블만" 비우고 테스트용으로 재구성한다.
--   (나머지 22개 테이블 user/lecture/enrollment 등은 base seed-dummy.sql 그대로 사용)
-- ---------------------------------------------------------------------
--  [실행 전제]
--   1) base seed-dummy.sql 이 먼저 적재돼 있어야 한다.
--      (user 13명 / lecture 10개 / enrollment 10건 등에 FK·강의명 의존)
--   2) 이 파일을 실행하면 friend/chat 4개 테이블이 아래 시나리오 상태로 덮어써진다.
-- ---------------------------------------------------------------------
--  [메인 테스트 로그인 계정] 비번 전부 password123
--   - 학생 관점 : student1@momo.city  (minsu, id=2)
--   - 강사 관점 : teacher1@momo.city  (coach_kim, id=6)
-- ---------------------------------------------------------------------
--  [참고 - 수강 관계 (강의명 표시용, base seed 기준)]
--   - minsu(2)   수강 : lec3,lec6 (둘 다 chef_park=7) / lec1 (coach_kim=6)
--   - jiyoung(3) 수강 : lec1,lec2 (둘 다 coach_kim=6)
--   => minsu 가 chef_park(7) 와 친구면 "집밥 마스터 클래스, 자바 기초 프로그래밍" 2개 표시
--   => jiyoung 가 coach_kim(6) 와 친구면 "아침 홈트 30일 챌린지, 코어 강화 필라테스" 2개 표시
--  [유저 status 참고] banned(10)=BANNED, black(11)=BLACK, rejected(9)=REJECTED, left_user(13)=DELETED
--                     => 위 4명은 ACTIVE 아님 => 출력 시 "닉네임(알 수 없음)" 대상
-- =====================================================================

-- 대상 DB 선택 (워크벤치에서 스키마 미선택 시 1046 "No database selected" 방지)
USE `momo`;

-- ---------------------------------------------------------------------
--  0. friend BC 4개 테이블 초기화 (FK 끄고 비움 → 깨끗한 상태에서 재구성)
-- ---------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `message`;
TRUNCATE TABLE `chat_room_member`;
TRUNCATE TABLE `chat_room`;
TRUNCATE TABLE `friend`;
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
--  1. friend  (minsu=2 중심 친구 그래프)
--     status : FRIEND(친구) / SENT(요청중) / BLOCK(차단)
-- =====================================================================
INSERT INTO `friend` (`id`,`from_user_id`,`to_user_id`,`status`,`created_at`) VALUES
  -- [친구 목록] 학생-학생 친구. 둘 다 ACTIVE → 닉네임 정상 출력
  (1 ,2 ,3 ,'FRIEND',NOW() - INTERVAL 40 DAY),
  -- [친구 목록] 학생 친구인데 minsu 가 to_user (방향 반대) → 양방향 모두 친구목록에 나와야 함
  (2 ,4 ,2 ,'FRIEND',NOW() - INTERVAL 38 DAY),
  -- [친구 목록/채팅] 채팅방 "재입장" 시나리오용 친구 (Room6 에서 사용)
  (3 ,2 ,5 ,'FRIEND',NOW() - INTERVAL 30 DAY),
  -- [친구 목록 - 강사+강의명] minsu 가 chef_park(7,강사)와 친구 + lec3,lec6 수강중
  --   => "chef_park(박강사) 강사 (집밥 마스터 클래스, 자바 기초 프로그래밍)" 로 출력돼야 함
  (4 ,2 ,7 ,'FRIEND',NOW() - INTERVAL 20 DAY),
  -- [친구 목록 - 알 수 없음] friend 지만 banned(10) 은 BANNED(non-active) → "닉네임(알 수 없음)"
  (5 ,2 ,10,'FRIEND',NOW() - INTERVAL 25 DAY),
  -- [받은 친구 요청] wannabe(8) 가 minsu 에게 보낸 요청(SENT, to=2) → 받은목록/수락/거절 테스트
  (6 ,8 ,2 ,'SENT'  ,NOW() - INTERVAL 2 HOUR),
  -- [보낸 친구 요청] minsu 가 kakao_user(12) 에게 보낸 요청(SENT, from=2) → 보낸목록/철회 테스트
  (7 ,2 ,12,'SENT'  ,NOW() - INTERVAL 35 MINUTE),
  -- [차단 목록 - from 방향] minsu 가 black(11) 차단
  (8 ,2 ,11,'BLOCK' ,NOW() - INTERVAL 3 DAY),
  -- [차단 목록 - to 방향 + 알 수 없음] rejected(9, REJECTED=non-active)가 minsu 차단
  --   => minsu 차단목록에 to 방향으로 나와야 하고, 닉네임은 "(알 수 없음)"
  (9 ,9 ,2 ,'BLOCK' ,NOW() - INTERVAL 5 DAY),
  -- [강사 로그인 관점] coach_kim(6) ↔ jiyoung(3) 친구. jiyoung lec1,lec2 수강(coach_kim 강의)
  --   => coach_kim 로그인 시 친구목록에 jiyoung 보이고, jiyoung 관점에선 강의명 표시
  (10,3 ,6 ,'FRIEND',NOW() - INTERVAL 22 DAY);

-- =====================================================================
--  2. chat_room  (6 + 2 = 8개)
-- =====================================================================
INSERT INTO `chat_room` (`id`,`created_at`) VALUES
  (1,NOW() - INTERVAL 40 DAY),   -- Room1 : minsu 나와의 채팅방
  (2,NOW() - INTERVAL 30 DAY),   -- Room2 : minsu ↔ jiyoung (메시지 22개, 페이지네이션 테스트)
  (3,NOW() - INTERVAL 20 DAY),   -- Room3 : minsu ↔ chef_park(강사) (강의명 표시)
  (4,NOW() - INTERVAL 15 DAY),   -- Room4 : minsu ↔ hyunwoo (hyunwoo 나감 → 알 수 없음)
  (5,NOW() - INTERVAL 25 DAY),   -- Room5 : minsu ↔ banned(non-active) (알 수 없음)
  (6,NOW() - INTERVAL 12 DAY),   -- Room6 : minsu ↔ seoyeon (minsu 재입장 → joined_at 이후 메시지만)
  (7,NOW() - INTERVAL 35 DAY),   -- Room7 : coach_kim 나와의 채팅방 (강사 로그인용)
  (8,NOW() - INTERVAL 22 DAY);   -- Room8 : coach_kim ↔ jiyoung (강사 로그인, 상대=학생)

-- =====================================================================
--  3. chat_room_member  ("나가기" = 행 삭제 / "재입장" = joined_at 갱신)
-- =====================================================================
INSERT INTO `chat_room_member` (`id`,`room_id`,`user_id`,`joined_at`) VALUES
  -- Room1 나와의 채팅방 : 멤버는 본인(2) 1명만. → 가장 먼저 "나와의 채팅방(minsu)" 으로 출력, 안읽음 0 고정
  (1 ,1,2 ,NOW() - INTERVAL 40 DAY),
  -- Room2 minsu ↔ jiyoung : 둘 다 오래전 입장 → 22개 메시지 전부 노출 대상
  (2 ,2,2 ,NOW() - INTERVAL 30 DAY),
  (3 ,2,3 ,NOW() - INTERVAL 30 DAY),
  -- Room3 minsu ↔ chef_park(강사)
  (4 ,3,2 ,NOW() - INTERVAL 20 DAY),
  (5 ,3,7 ,NOW() - INTERVAL 20 DAY),
  -- Room4 minsu ↔ hyunwoo : hyunwoo(4) 가 "나감" → 멤버 행 없음(minsu 행만 존재). 메시지는 남아있음
  --   => 목록/내역에서 상대 "닉네임(알 수 없음)" + 상대방 나감 처리
  (6 ,4,2 ,NOW() - INTERVAL 15 DAY),
  -- Room5 minsu ↔ banned(10, non-active)
  (7 ,5,2 ,NOW() - INTERVAL 25 DAY),
  (8 ,5,10,NOW() - INTERVAL 25 DAY),
  -- Room6 재입장 : seoyeon(5)은 오래전 입장 / minsu(2)는 "최근 재입장"(1시간 전)
  --   => minsu 메시지 내역에는 joined_at(1시간 전) "이후" 메시지만 보여야 함
  (9 ,6,5 ,NOW() - INTERVAL 12 DAY),
  (10,6,2 ,NOW() - INTERVAL 1 HOUR),
  -- Room7 coach_kim 나와의 채팅방 : 멤버 본인(6) 1명
  (11,7,6 ,NOW() - INTERVAL 35 DAY),
  -- Room8 coach_kim ↔ jiyoung
  (12,8,6 ,NOW() - INTERVAL 22 DAY),
  (13,8,3 ,NOW() - INTERVAL 22 DAY);

-- =====================================================================
--  4. message
--   is_read : 1=읽음 / 0=안읽음. "상대→나" 안읽음이 곧 "안읽은 메시지 개수"
-- =====================================================================

-- --- Room1 : minsu 나와의 채팅방 (메모용 자기 메시지). 안읽음 카운트는 항상 0 고정 ---
INSERT INTO `message` (`id`,`room_id`,`sender_id`,`content`,`is_read`,`created_at`) VALUES
  (1,1,2,'(나에게) 내일 운동 루틴 메모: 스쿼트 3세트',1,NOW() - INTERVAL 2 DAY),
  (2,1,2,'(나에게) 자바 강의 6강 복습하기',1,NOW() - INTERVAL 5 HOUR);

-- --- Room2 : minsu(2) ↔ jiyoung(3) 메시지 22개 (최초 20개 + 스크롤로 2개 더) ---
--   마지막 3개(20~22)는 jiyoung→minsu 안읽음(is_read=0) → 안읽은 메시지 3개로 카운트
INSERT INTO `message` (`id`,`room_id`,`sender_id`,`content`,`is_read`,`created_at`) VALUES
  (3 ,2,2,'지영아 안녕! 오늘 홈트 했어?',1,NOW() - INTERVAL 26 HOUR),
  (4 ,2,3,'응 방금 끝냈어 ㅎㅎ',1,NOW() - INTERVAL 25 HOUR),
  (5 ,2,2,'오 몇 세트 했어?',1,NOW() - INTERVAL 24 HOUR),
  (6 ,2,3,'3세트! 너무 힘들더라',1,NOW() - INTERVAL 23 HOUR),
  (7 ,2,2,'나도 같이 할걸',1,NOW() - INTERVAL 22 HOUR),
  (8 ,2,3,'내일 같이 하자',1,NOW() - INTERVAL 21 HOUR),
  (9 ,2,2,'좋아 몇 시에?',1,NOW() - INTERVAL 20 HOUR),
  (10,2,3,'아침 7시 어때',1,NOW() - INTERVAL 8 HOUR),
  (11,2,2,'콜 ㅋㅋ',1,NOW() - INTERVAL 7 HOUR),
  (12,2,3,'필라테스도 해볼래?',1,NOW() - INTERVAL 6 HOUR),
  (13,2,2,'그건 좀 어렵던데',1,NOW() - INTERVAL 5 HOUR),
  (14,2,3,'천천히 하면 돼',1,NOW() - INTERVAL 4 HOUR),
  (15,2,2,'알겠어 도전!',1,NOW() - INTERVAL 3 HOUR),
  (16,2,3,'영상 링크 보내줄게',1,NOW() - INTERVAL 160 MINUTE),
  (17,2,2,'땡큐 ㅋㅋ',1,NOW() - INTERVAL 140 MINUTE),
  (18,2,3,'점심 뭐 먹어?',1,NOW() - INTERVAL 120 MINUTE),
  (19,2,2,'집밥 클래스 레시피 해보려고',1,NOW() - INTERVAL 90 MINUTE),
  (20,2,3,'헐 나도 알려줘',0,NOW() - INTERVAL 12 MINUTE),
  (21,2,3,'스크린샷 찍어서 보내줘',0,NOW() - INTERVAL 8 MINUTE),
  (22,2,3,'기다리고 있을게!',0,NOW() - INTERVAL 3 MINUTE),
  (23,2,2,'(이 메시지는 19번 이후 minsu 응답 채우기용) 잠깐만',1,NOW() - INTERVAL 80 MINUTE),
  (24,2,3,'ㅇㅋㅇㅋ',1,NOW() - INTERVAL 70 MINUTE);

-- --- Room3 : minsu(2) ↔ chef_park(7, 강사). 학생 관점 "닉네임(이름) 강사 (강의명1, 강의명2)" ---
INSERT INTO `message` (`id`,`room_id`,`sender_id`,`content`,`is_read`,`created_at`) VALUES
  (25,3,2,'강사님 집밥 클래스 질문 있어요',1,NOW() - INTERVAL 3 DAY),
  (26,3,7,'네 말씀하세요',1,NOW() - INTERVAL 3 DAY + INTERVAL 10 MINUTE),
  (27,3,2,'된장국 간을 못 맞추겠어요',1,NOW() - INTERVAL 2 DAY),
  (28,3,7,'4강 영상 다시 보시면 도움 될 거예요',0,NOW() - INTERVAL 50 MINUTE);

-- --- Room4 : minsu(2) ↔ hyunwoo(4). hyunwoo 나감(멤버행 없음) → 상대 "알 수 없음" ---
--   메시지는 나가기 전 기록이 남아있음(양쪽 발신 모두)
INSERT INTO `message` (`id`,`room_id`,`sender_id`,`content`,`is_read`,`created_at`) VALUES
  (29,4,2,'현우야 스터디 자료 공유해줘',1,NOW() - INTERVAL 15 DAY),
  (30,4,4,'옙 이메일로 보냈어',1,NOW() - INTERVAL 15 DAY + INTERVAL 20 MINUTE),
  (31,4,2,'고마워!',1,NOW() - INTERVAL 14 DAY),
  (32,4,4,'그럼 나 이만 나갈게 ㅎㅎ',1,NOW() - INTERVAL 13 DAY);

-- --- Room5 : minsu(2) ↔ banned(10, BANNED=non-active) → 상대 "닉네임(알 수 없음)" ---
INSERT INTO `message` (`id`,`room_id`,`sender_id`,`content`,`is_read`,`created_at`) VALUES
  (33,5,2 ,'안녕하세요',1,NOW() - INTERVAL 25 DAY),
  (34,5,10,'네 안녕하세요',1,NOW() - INTERVAL 24 DAY),
  (35,5,2 ,'...',1,NOW() - INTERVAL 23 DAY);

-- --- Room6 : minsu(2) ↔ seoyeon(5). minsu 재입장(joined_at=1시간 전) ---
--   minsu 메시지 내역에는 (36,37)=재입장 전 → 안 보임 / (38~41)=재입장 후 → 보여야 함
INSERT INTO `message` (`id`,`room_id`,`sender_id`,`content`,`is_read`,`created_at`) VALUES
  (36,6,5,'(재입장 전) 예전에 나눴던 대화1',1,NOW() - INTERVAL 2 DAY),
  (37,6,2,'(재입장 전) 예전에 나눴던 대화2',1,NOW() - INTERVAL 47 HOUR),
  (38,6,5,'(재입장 후) 다시 왔네 반가워',1,NOW() - INTERVAL 40 MINUTE),
  (39,6,2,'(재입장 후) 응 다시 들어왔어',1,NOW() - INTERVAL 30 MINUTE),
  (40,6,5,'(재입장 후) 안읽음 테스트용 메시지',0,NOW() - INTERVAL 20 MINUTE),
  (41,6,5,'(재입장 후) 이것도 안읽음',0,NOW() - INTERVAL 10 MINUTE);

-- --- Room7 : coach_kim 나와의 채팅방 (강사 로그인용) ---
INSERT INTO `message` (`id`,`room_id`,`sender_id`,`content`,`is_read`,`created_at`) VALUES
  (42,7,6,'(나에게) 다음 주 강의 업로드 잊지 말기',1,NOW() - INTERVAL 1 DAY);

-- --- Room8 : coach_kim(6) ↔ jiyoung(3). 강사 로그인 관점 "닉네임(강의명1, 강의명2)" ---
INSERT INTO `message` (`id`,`room_id`,`sender_id`,`content`,`is_read`,`created_at`) VALUES
  (43,8,3,'강사님 코어 강화 강의 잘 듣고 있어요',1,NOW() - INTERVAL 5 DAY),
  (44,8,6,'감사합니다 꾸준히 하세요!',1,NOW() - INTERVAL 5 DAY + INTERVAL 30 MINUTE),
  (45,8,3,'네! 다음 강의도 기대할게요',0,NOW() - INTERVAL 2 HOUR);

-- =====================================================================
--  ★ 테스트 가이드 (로그인 계정별 / 기능별)
-- =====================================================================
--  [minsu(student1@momo.city) 로 로그인하면]
--   - 친구 목록      : jiyoung(친구), hyunwoo(친구), seoyeon(친구), chef_park(강사+강의명2개),
--                      banned(친구지만 "알 수 없음")  ← BLOCK/SENT 는 친구목록에 안 나옴
--   - 사용자 검색    : 닉네임 일부로 검색. black(11)은 BLOCK이라 제외. 강사는 "닉네임(이름) 강사"
--                      존재X 닉네임 검색 시 예외문구 확인
--   - 받은 친구요청  : wannabe(8) 1건 (수락/거절 테스트)
--   - 보낸 친구요청  : kakao_user(12) 1건 (철회 테스트)
--   - 차단 목록      : black(11, from방향), rejected(9, to방향+"알 수 없음")
--   - 친구요청 예외  : coach_kim(6)에게 요청→"강사에게 직접 요청 불가" / 본인(2)에게→"자기자신 불가"
--                      jiyoung(이미친구)→"이미 친구" / kakao(이미SENT)→"이미 요청 보냄" / black→"차단됨"
--   - 채팅 목록      : Room1(나와의 채팅방·맨 위·안읽음0), Room2(jiyoung·안읽음3),
--                      Room3(chef_park 강사·강의명), Room4(hyunwoo "알 수 없음"·나감),
--                      Room5(banned "알 수 없음"), Room6(seoyeon·재입장)
--   - 메시지 내역    : Room2 진입→최신20개, 스크롤→나머지2개(총22) / Room6→재입장(1시간전) 이후만
--   - 채팅 개설 예외 : 비친구와 개설 시도→불가 / 본인과→불가 / 존재X 유저→불가
--
--  [coach_kim(teacher1@momo.city) 로 로그인하면 - 강사 관점]
--   - 친구 목록/채팅 : jiyoung(학생). 메시지 내역에서 "jiyoung(강의명1,강의명2)" 형태 확인
--   - Room7 나와의 채팅방 / Room8 jiyoung 과의 채팅
--   - 강사 제약       : 강사는 차단 불가 / 강사 속한 방 나가기 불가 (코드 검증 대상)
-- =====================================================================
--  END
-- =====================================================================
