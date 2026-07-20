SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
--  1. access_log — 50건
-- =====================================================================
INSERT INTO `access_log` (`id`, `action`, `created_at`, `ip`, `user_id`) VALUES
  (1, 'LOGOUT', NOW() - INTERVAL 150 HOUR, '41.193.40.142', 49),
  (2, 'LOGOUT', NOW() - INTERVAL 35 HOUR, '168.185.98.181', 96),
  (3, 'LOGIN', NOW() - INTERVAL 40 HOUR, '179.116.148.253', 26),
  (4, 'LOGIN', NOW() - INTERVAL 325 HOUR, '35.194.142.117', 122),
  (5, 'LOGIN', NOW() - INTERVAL 136 HOUR, '51.189.181.54', 75),
  (6, 'FORBIDDEN', NOW() - INTERVAL 125 HOUR, '175.36.87.137', 124),
  (7, 'LOGOUT', NOW() - INTERVAL 327 HOUR, '128.194.138.254', 86),
  (8, 'LOGIN', NOW() - INTERVAL 16 HOUR, '66.166.28.59', 60),
  (9, 'LOGOUT', NOW() - INTERVAL 290 HOUR, '112.137.33.55', 50),
  (10, 'LOGIN', NOW() - INTERVAL 329 HOUR, '64.255.202.227', 50),
  (11, 'LOGOUT', NOW() - INTERVAL 287 HOUR, '46.135.71.64', 123),
  (12, 'LOGOUT', NOW() - INTERVAL 112 HOUR, '77.219.204.93', 50),
  (13, 'LOGIN', NOW() - INTERVAL 24 HOUR, '140.252.46.194', 73),
  (14, 'LOGIN', NOW() - INTERVAL 32 HOUR, '49.81.216.153', 59),
  (15, 'LOGOUT', NOW() - INTERVAL 283 HOUR, '107.239.128.249', 86),
  (16, 'LOGOUT', NOW() - INTERVAL 328 HOUR, '184.58.136.197', 6),
  (17, 'LOGIN', NOW() - INTERVAL 232 HOUR, '38.150.222.41', 63),
  (18, 'LOGOUT', NOW() - INTERVAL 54 HOUR, '194.134.91.130', 2),
  (19, 'LOGOUT', NOW() - INTERVAL 82 HOUR, '86.101.78.96', 95),
  (20, 'LOGIN', NOW() - INTERVAL 9 HOUR, '209.0.165.126', 51),
  (21, 'FORBIDDEN', NOW() - INTERVAL 123 HOUR, '102.157.122.15', 60),
  (22, 'LOGIN', NOW() - INTERVAL 35 HOUR, '30.43.248.209', 65),
  (23, 'LOGOUT', NOW() - INTERVAL 243 HOUR, '206.64.65.169', 47),
  (24, 'FORBIDDEN', NOW() - INTERVAL 108 HOUR, '52.135.216.247', 56),
  (25, 'FORBIDDEN', NOW() - INTERVAL 332 HOUR, '203.102.159.103', 51),
  (26, 'LOGOUT', NOW() - INTERVAL 115 HOUR, '122.231.61.64', 80),
  (27, 'LOGOUT', NOW() - INTERVAL 112 HOUR, '96.10.117.151', 35),
  (28, 'LOGOUT', NOW() - INTERVAL 16 HOUR, '28.30.117.18', 4),
  (29, 'LOGOUT', NOW() - INTERVAL 248 HOUR, '28.121.142.172', 58),
  (30, 'FORBIDDEN', NOW() - INTERVAL 242 HOUR, '148.67.242.63', 112),
  (31, 'FORBIDDEN', NOW() - INTERVAL 220 HOUR, '58.48.49.169', 97),
  (32, 'LOGOUT', NOW() - INTERVAL 27 HOUR, '118.210.239.222', 70),
  (33, 'LOGOUT', NOW() - INTERVAL 173 HOUR, '177.50.31.104', 119),
  (34, 'FORBIDDEN', NOW() - INTERVAL 229 HOUR, '73.98.97.138', 58),
  (35, 'LOGIN', NOW() - INTERVAL 127 HOUR, '118.93.142.119', 74),
  (36, 'FORBIDDEN', NOW() - INTERVAL 276 HOUR, '123.50.25.167', 41),
  (37, 'FORBIDDEN', NOW() - INTERVAL 248 HOUR, '33.121.85.105', 8),
  (38, 'LOGIN', NOW() - INTERVAL 194 HOUR, '64.205.30.43', 21),
  (39, 'LOGOUT', NOW() - INTERVAL 216 HOUR, '109.135.232.74', 2),
  (40, 'LOGOUT', NOW() - INTERVAL 151 HOUR, '179.249.79.49', 59),
  (41, 'LOGIN', NOW() - INTERVAL 25 HOUR, '24.31.160.15', 114),
  (42, 'LOGOUT', NOW() - INTERVAL 260 HOUR, '132.80.29.246', 74),
  (43, 'LOGIN', NOW() - INTERVAL 120 HOUR, '57.35.34.173', 44),
  (44, 'LOGIN', NOW() - INTERVAL 41 HOUR, '40.126.20.159', 95),
  (45, 'LOGIN', NOW() - INTERVAL 160 HOUR, '178.161.133.53', 103),
  (46, 'LOGIN', NOW() - INTERVAL 330 HOUR, '77.202.67.172', 125),
  (47, 'LOGIN', NOW() - INTERVAL 234 HOUR, '127.161.37.3', 42),
  (48, 'LOGIN', NOW() - INTERVAL 109 HOUR, '154.51.37.138', 93),
  (49, 'LOGIN', NOW() - INTERVAL 35 HOUR, '77.67.178.226', 34),
  (50, 'FORBIDDEN', NOW() - INTERVAL 278 HOUR, '104.145.80.113', 14)
    ON DUPLICATE KEY UPDATE
                         action = VALUES(action),
                         created_at = VALUES(created_at),
                         ip = VALUES(ip),
                         user_id = VALUES(user_id);

-- =====================================================================
--  2. error_log — 20건
-- =====================================================================
INSERT INTO `error_log` (`id`, `created_at`, `updated_at`, `level`, `message`, `occurred_at`, `source`) VALUES
  (1, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 'WARNING', 'JWT 토큰 검증 실패', NOW() - INTERVAL 4 DAY, 'AuthService'),
  (2, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 'CRITICAL', '강의 목록 조회 중 NullPointerException 발생', NOW() - INTERVAL 8 DAY, 'LectureService'),
  (3, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY, 'ERROR', '챕터 영상 업로드 실패 - S3 연결 오류', NOW() - INTERVAL 12 DAY, 'ChapterService'),
  (4, NOW() - INTERVAL 16 DAY, NOW() - INTERVAL 16 DAY, 'WARNING', '게시글 이미지 리사이징 처리 지연', NOW() - INTERVAL 16 DAY, 'PostService'),
  (5, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 'CRITICAL', '채팅 메시지 브로드캐스트 실패', NOW() - INTERVAL 20 DAY, 'ChatService'),
  (6, NOW() - INTERVAL 24 DAY, NOW() - INTERVAL 24 DAY, 'ERROR', '알림 발송 큐 적재 지연', NOW() - INTERVAL 24 DAY, 'NotificationService'),
  (7, NOW() - INTERVAL 28 DAY, NOW() - INTERVAL 28 DAY, 'WARNING', '스트릭 갱신 배치 처리 중 DB 락 타임아웃', NOW() - INTERVAL 28 DAY, 'StreakService'),
  (8, NOW() - INTERVAL 32 DAY, NOW() - INTERVAL 32 DAY, 'CRITICAL', '신고 처리 중 중복 키 예외 발생', NOW() - INTERVAL 32 DAY, 'ReportService'),
  (9, NOW() - INTERVAL 36 DAY, NOW() - INTERVAL 36 DAY, 'ERROR', '수강 등록 처리 중 동시성 문제 발생', NOW() - INTERVAL 36 DAY, 'EnrollmentService'),
  (10, NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 40 DAY, 'WARNING', '리뷰 등록 시 rating 값 범위 초과', NOW() - INTERVAL 40 DAY, 'ReviewService'),
  (11, NOW() - INTERVAL 44 DAY, NOW() - INTERVAL 44 DAY, 'CRITICAL', 'JWT 토큰 검증 실패', NOW() - INTERVAL 44 DAY, 'AuthService'),
  (12, NOW() - INTERVAL 48 DAY, NOW() - INTERVAL 48 DAY, 'ERROR', '강의 목록 조회 중 NullPointerException 발생', NOW() - INTERVAL 48 DAY, 'LectureService'),
  (13, NOW() - INTERVAL 52 DAY, NOW() - INTERVAL 52 DAY, 'WARNING', '챕터 영상 업로드 실패 - S3 연결 오류', NOW() - INTERVAL 52 DAY, 'ChapterService'),
  (14, NOW() - INTERVAL 56 DAY, NOW() - INTERVAL 56 DAY, 'CRITICAL', '게시글 이미지 리사이징 처리 지연', NOW() - INTERVAL 56 DAY, 'PostService'),
  (15, NOW() - INTERVAL 60 DAY, NOW() - INTERVAL 60 DAY, 'ERROR', '채팅 메시지 브로드캐스트 실패', NOW() - INTERVAL 60 DAY, 'ChatService'),
  (16, NOW() - INTERVAL 64 DAY, NOW() - INTERVAL 64 DAY, 'WARNING', '알림 발송 큐 적재 지연', NOW() - INTERVAL 64 DAY, 'NotificationService'),
  (17, NOW() - INTERVAL 68 DAY, NOW() - INTERVAL 68 DAY, 'CRITICAL', '스트릭 갱신 배치 처리 중 DB 락 타임아웃', NOW() - INTERVAL 68 DAY, 'StreakService'),
  (18, NOW() - INTERVAL 72 DAY, NOW() - INTERVAL 72 DAY, 'ERROR', '신고 처리 중 중복 키 예외 발생', NOW() - INTERVAL 72 DAY, 'ReportService'),
  (19, NOW() - INTERVAL 76 DAY, NOW() - INTERVAL 76 DAY, 'WARNING', '수강 등록 처리 중 동시성 문제 발생', NOW() - INTERVAL 76 DAY, 'EnrollmentService'),
  (20, NOW() - INTERVAL 80 DAY, NOW() - INTERVAL 80 DAY, 'CRITICAL', '리뷰 등록 시 rating 값 범위 초과', NOW() - INTERVAL 80 DAY, 'ReviewService')
    ON DUPLICATE KEY UPDATE
                         created_at = VALUES(created_at),
                         updated_at = VALUES(updated_at),
                         level = VALUES(level),
                         message = VALUES(message),
                         occurred_at = VALUES(occurred_at),
                         source = VALUES(source);

-- =====================================================================
--  3. report — 30건 (reason/target_type 다양화, 해결률 40%)
-- =====================================================================
INSERT INTO `report` (`id`, `created_at`, `detail`, `is_resolved`, `reason`, `reported_user_id`, `reporter_user_id`, `resolved_at`, `target_id`, `target_path`, `target_type`) VALUES
  (1, NOW() - INTERVAL 5 DAY, '광고성 게시글로 신고합니다', 0, 'SPAM', 20, 15, NULL, 3, NULL, 'POST'),
  (2, NOW() - INTERVAL 40 DAY, NULL, 1, 'SPAM', 45, 22, NOW() - INTERVAL 35 DAY, 17, NULL, 'POST'),
  (3, NOW() - INTERVAL 12 DAY, '욕설이 포함된 게시글입니다', 0, 'ABUSE', 12, 30, NULL, 25, NULL, 'POST'),
  (4, NOW() - INTERVAL 55 DAY, '부적절한 이미지 포함', 1, 'INAPPROPRIATE', 60, 18, NOW() - INTERVAL 50 DAY, 8, NULL, 'POST'),
  (5, NOW() - INTERVAL 20 DAY, '저작권 침해 의심', 0, 'COPYRIGHT', 33, 40, NULL, 52, NULL, 'POST'),
  (6, NOW() - INTERVAL 30 DAY, NULL, 0, 'OTHER', 70, 55, NULL, 64, NULL, 'POST'),
  (7, NOW() - INTERVAL 65 DAY, NULL, 1, 'SPAM', 15, 62, NOW() - INTERVAL 60 DAY, 9, NULL, 'POST'),
  (8, NOW() - INTERVAL 8 DAY, '반복적인 비방 게시글', 0, 'ABUSE', 90, 80, NULL, 71, NULL, 'POST'),
  (9, NOW() - INTERVAL 45 DAY, NULL, 1, 'INAPPROPRIATE', 44, 95, NOW() - INTERVAL 40 DAY, 36, NULL, 'POST'),
  (10, NOW() - INTERVAL 15 DAY, '동일 게시글 반복 등록', 0, 'SPAM', 77, 100, NULL, 88, NULL, 'POST'),
  (11, NOW() - INTERVAL 22 DAY, '댓글 내 욕설', 0, 'ABUSE', 13, 25, NULL, 5, NULL, 'COMMENT'),
  (12, NOW() - INTERVAL 70 DAY, NULL, 1, 'SPAM', 46, 33, NOW() - INTERVAL 65 DAY, 12, NULL, 'COMMENT'),
  (13, NOW() - INTERVAL 18 DAY, NULL, 0, 'INAPPROPRIATE', 21, 47, NULL, 20, NULL, 'COMMENT'),
  (14, NOW() - INTERVAL 33 DAY, '악성 댓글 신고', 0, 'OTHER', 39, 58, NULL, 28, NULL, 'COMMENT'),
  (15, NOW() - INTERVAL 80 DAY, NULL, 1, 'ABUSE', 82, 70, NOW() - INTERVAL 75 DAY, 35, NULL, 'COMMENT'),
  (16, NOW() - INTERVAL 27 DAY, NULL, 0, 'SPAM', 99, 88, NULL, 42, NULL, 'COMMENT'),
  (17, NOW() - INTERVAL 90 DAY, '강의 내용 문제 제기', 1, 'INAPPROPRIATE', 3, 45, NOW() - INTERVAL 85 DAY, 12, NULL, 'LECTURE'),
  (18, NOW() - INTERVAL 14 DAY, '타 강의 콘텐츠 도용 의심', 0, 'COPYRIGHT', 6, 60, NULL, 25, NULL, 'LECTURE'),
  (19, NOW() - INTERVAL 60 DAY, NULL, 0, 'OTHER', 8, 72, NULL, 33, NULL, 'LECTURE'),
  (20, NOW() - INTERVAL 100 DAY, NULL, 1, 'ABUSE', 2, 90, NOW() - INTERVAL 95 DAY, 40, NULL, 'LECTURE'),
  (21, NOW() - INTERVAL 25 DAY, '챕터 영상 음질 및 내용 문제', 0, 'INAPPROPRIATE', 4, 33, NULL, 100, NULL, 'CHAPTER'),
  (22, NOW() - INTERVAL 38 DAY, NULL, 0, 'OTHER', 7, 50, NULL, 150, NULL, 'CHAPTER'),
  (23, NOW() - INTERVAL 110 DAY, NULL, 1, 'COPYRIGHT', 9, 66, NOW() - INTERVAL 105 DAY, 200, NULL, 'CHAPTER'),
  (24, NOW() - INTERVAL 9 DAY, '리뷰에 욕설 포함', 0, 'ABUSE', 55, 20, NULL, 10, NULL, 'REVIEW'),
  (25, NOW() - INTERVAL 120 DAY, NULL, 1, 'SPAM', 63, 41, NOW() - INTERVAL 115 DAY, 22, NULL, 'REVIEW'),
  (26, NOW() - INTERVAL 16 DAY, NULL, 0, 'INAPPROPRIATE', 91, 77, NULL, 35, NULL, 'REVIEW'),
  (27, NOW() - INTERVAL 3 DAY, '채팅 내 욕설/비방', 0, 'ABUSE', 48, 34, NULL, 60, NULL, 'CHAT'),
  (28, NOW() - INTERVAL 130 DAY, NULL, 1, 'OTHER', 67, 52, NOW() - INTERVAL 125 DAY, 85, NULL, 'CHAT'),
  (29, NOW() - INTERVAL 2 DAY, '공지 페이지 오류 신고', 0, 'OTHER', NULL, 19, NULL, NULL, '/community/notice/3', 'PAGE'),
  (30, NOW() - INTERVAL 140 DAY, NULL, 1, 'OTHER', NULL, 105, NOW() - INTERVAL 135 DAY, NULL, '/help/faq', 'PAGE')
    ON DUPLICATE KEY UPDATE
                         created_at = VALUES(created_at),
                         detail = VALUES(detail),
                         is_resolved = VALUES(is_resolved),
                         reason = VALUES(reason),
                         reported_user_id = VALUES(reported_user_id),
                         reporter_user_id = VALUES(reporter_user_id),
                         resolved_at = VALUES(resolved_at),
                         target_id = VALUES(target_id),
                         target_path = VALUES(target_path),
                         target_type = VALUES(target_type);

-- =====================================================================
--  4. admin_notice — 10건 (pinned 1건)
-- =====================================================================
INSERT INTO `admin_notice` (`id`, `content`, `created_at`, `is_pinned`, `title`, `updated_at`) VALUES
  (1, '매주 화요일 새벽 2시~4시 정기 점검이 진행됩니다. 이용에 참고 부탁드립니다.', NOW() - INTERVAL 85 DAY, 0, '[공지] 서버 정기 점검 안내', NOW() - INTERVAL 85 DAY),
  (2, '캘린더에서 반복 일정 등록 기능이 추가되었습니다.', NOW() - INTERVAL 70 DAY, 0, '[업데이트] 캘린더 기능 개선 안내', NOW() - INTERVAL 70 DAY),
  (3, '이번 달 출석 체크 이벤트에 참여하고 포인트를 받아가세요.', NOW() - INTERVAL 55 DAY, 0, '[이벤트] 출석 체크 이벤트 안내', NOW() - INTERVAL 55 DAY),
  (4, '개인정보처리방침이 일부 개정되어 안내드립니다.', NOW() - INTERVAL 40 DAY, 0, '[공지] 개인정보처리방침 개정 안내', NOW() - INTERVAL 40 DAY),
  (5, '학습 도우미 챗봇 기능이 베타로 오픈될 예정입니다.', NOW() - INTERVAL 30 DAY, 0, '[안내] 챗봇 베타 서비스 오픈 예정', NOW() - INTERVAL 30 DAY),
  (6, '아트 카테고리 신규 강좌가 추가되었습니다.', NOW() - INTERVAL 22 DAY, 0, '[공지] 신규 강좌 카테고리 추가 안내', NOW() - INTERVAL 22 DAY),
  (7, '일부 사용자분들의 로그인 오류가 확인되어 조치 완료하였습니다.', NOW() - INTERVAL 15 DAY, 1, '[긴급] 일부 사용자 로그인 오류 안내 및 조치 완료', NOW() - INTERVAL 15 DAY),
  (8, '친구를 초대하고 함께 포인트 혜택을 받아보세요.', NOW() - INTERVAL 10 DAY, 0, '[이벤트] 친구 초대 이벤트 진행 안내', NOW() - INTERVAL 10 DAY),
  (9, '이용약관 개정 사항을 사전 고지드립니다.', NOW() - INTERVAL 5 DAY, 0, '[공지] 이용약관 개정 사전고지', NOW() - INTERVAL 5 DAY),
  (10, '앱스토어 리뷰 이벤트가 종료되었습니다. 참여해주셔서 감사합니다.', NOW() - INTERVAL 1 DAY, 0, '[안내] 앱스토어 리뷰 이벤트 종료 안내', NOW() - INTERVAL 1 DAY)
    ON DUPLICATE KEY UPDATE
                         content = VALUES(content),
                         created_at = VALUES(created_at),
                         is_pinned = VALUES(is_pinned),
                         title = VALUES(title),
                         updated_at = VALUES(updated_at);

SET FOREIGN_KEY_CHECKS = 1;
