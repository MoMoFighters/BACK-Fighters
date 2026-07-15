SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
--  1. chat_room
-- =====================================================================
INSERT INTO `chat_room` (`id`, `created_at`, `title`, `updated_at`) VALUES
  (1, NOW() - INTERVAL 40 DAY, NULL, NOW() - INTERVAL 1 DAY),
  (2, NOW() - INTERVAL 35 DAY, NULL, NOW() - INTERVAL 2 DAY),
  (3, NOW() - INTERVAL 30 DAY, NULL, NOW() - INTERVAL 3 DAY);
-- =====================================================================
--  2. chat_room_member
-- =====================================================================
INSERT INTO `chat_room_member` (`id`, `joined_at`, `room_id`, `user_id`) VALUES
  (1, NOW() - INTERVAL 40 DAY, 1, 12),
  (2, NOW() - INTERVAL 40 DAY, 1, 13),
  (3, NOW() - INTERVAL 35 DAY, 2, 12),
  (4, NOW() - INTERVAL 35 DAY, 2, 14),
  (5, NOW() - INTERVAL 30 DAY, 3, 13),
  (6, NOW() - INTERVAL 30 DAY, 3, 15);
-- =====================================================================
--  3. message
-- =====================================================================
INSERT INTO `message` (`id`, `content`, `created_at`, `updated_at`, `room_id`, `sender_id`) VALUES
  (1,  '안녕하세요!',  NOW() - INTERVAL 3 DAY,  NOW() - INTERVAL 3 DAY,  1,  12),
  (2,  '반가워요 :)',  NOW() - INTERVAL 3 DAY + INTERVAL 1 HOUR,  NOW() - INTERVAL 3 DAY + INTERVAL 1 HOUR,  1,  13),
  (3,  '오늘 운동하셨나요?',  NOW() - INTERVAL 2 DAY,  NOW() - INTERVAL 2 DAY,  2,  12),
  (4,  '네 오늘 완료했어요!',  NOW() - INTERVAL 2 DAY + INTERVAL 30 MINUTE,  NOW() - INTERVAL 2 DAY + INTERVAL 30 MINUTE,  2,  14),
  (5,  '같이 스터디할래요?',  NOW() - INTERVAL 1 DAY,  NOW() - INTERVAL 1 DAY,  3,  13),
  (6, '좋아요!', NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 12 HOUR, 3, 15);
-- =====================================================================
--  4. message_read
-- =====================================================================
INSERT INTO `message_read` (`id`, `is_deleted`, `is_msg_read`, `is_noti_read`, `message_id`, `room_id`, `user_id`) VALUES
  (1, 0, 1, 0, 1, 1, 12),
  (2, 0, 1, 0, 1, 1, 13),
  (3, 0, 1, 0, 2, 1, 12),
  (4, 0, 0, 0, 2, 1, 13),
  (5, 0, 1, 0, 3, 2, 12),
  (6, 0, 1, 0, 4, 2, 14);
-- =====================================================================
--  5. guestbook
-- =====================================================================
INSERT INTO `guestbook` (`id`, `content`, `created_at`, `owner_id`, `writer_id`) VALUES
  (1,  '잘 부탁드려요!', NOW() - INTERVAL 30 DAY, 12, 13),
  (2, '응원합니다!', NOW() - INTERVAL 25 DAY, 13, 12),
  (3,  '같이 열심히 해봐요 :)', NOW() - INTERVAL 20 DAY, 14, 12),
  (4,  '오늘도 화이팅!', NOW() - INTERVAL 10 DAY, 12, 15),
  (5,  '잘 지내시죠?', NOW() - INTERVAL 5 DAY, 15, 14);
-- =====================================================================
--  6. notification
-- =====================================================================
INSERT INTO `notification` (`id`, `created_at`, `is_read`, `message`, `ref_id`, `type`, `user_id`)
VALUES
    -- 12번 민수 (118일 전 가입/친구 등록)
    (1, NOW() - INTERVAL 118 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 12),
    (2, NOW() - INTERVAL 118 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 12),
    (3, NOW() - INTERVAL 118 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 12),
    -- 13번 지영 (117일 전)
    (4, NOW() - INTERVAL 117 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 13),
    (5, NOW() - INTERVAL 117 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 13),
    (6, NOW() - INTERVAL 117 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 13),
    -- 14번 현우 (116일 전)
    (7, NOW() - INTERVAL 116 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 14),
    (8, NOW() - INTERVAL 116 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 14),
    (9, NOW() - INTERVAL 116 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 14),
    -- 15번 서연 (115일 전)
    (10, NOW() - INTERVAL 115 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 15),
    (11, NOW() - INTERVAL 115 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 15),
    (12, NOW() - INTERVAL 115 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 15),
    -- 16번 도현 (114일 전)
    (13, NOW() - INTERVAL 114 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 16),
    (14, NOW() - INTERVAL 114 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 16),
    (15, NOW() - INTERVAL 114 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 16),
    -- 17번 하늘 (113일 전)
    (16, NOW() - INTERVAL 113 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 17),
    (17, NOW() - INTERVAL 113 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 17),
    (18, NOW() - INTERVAL 113 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 17),
    -- 18번 채원 (112일 전)
    (19, NOW() - INTERVAL 112 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 18),
    (20, NOW() - INTERVAL 112 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 18),
    (21, NOW() - INTERVAL 112 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 18),
    -- 19번 승준 (111일 전)
    (22, NOW() - INTERVAL 111 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 19),
    (23, NOW() - INTERVAL 111 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 19),
    (24, NOW() - INTERVAL 111 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 19),
    -- 20번 유진 (110일 전)
    (25, NOW() - INTERVAL 110 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 20),
    (26, NOW() - INTERVAL 110 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 20),
    (27, NOW() - INTERVAL 110 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 20),
    -- 21번 지훈 (109일 전)
    (28, NOW() - INTERVAL 109 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 21),
    (29, NOW() - INTERVAL 109 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 21),
    (30, NOW() - INTERVAL 109 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 21),
    -- 22번 소희 (108일 전)
    (31, NOW() - INTERVAL 108 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 22),
    (32, NOW() - INTERVAL 108 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 22),
    (33, NOW() - INTERVAL 108 DAY, 0, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 22),
    -- 23번 태양 (107일 전)
    (34, NOW() - INTERVAL 107 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 23),
    (35, NOW() - INTERVAL 107 DAY, 0, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 23),
    (36, NOW() - INTERVAL 107 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 23),
    -- 24번 한거절 (106일 전)
    (37, NOW() - INTERVAL 106 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 24),
    (38, NOW() - INTERVAL 106 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 24),
    (39, NOW() - INTERVAL 106 DAY, 0, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 24),
    -- 25번 조반려 (105일 전)
    (40, NOW() - INTERVAL 105 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 25),
    (41, NOW() - INTERVAL 105 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 25),
    (42, NOW() - INTERVAL 105 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 25),
    -- 26번 노정지 (104일 전)
    (43, NOW() - INTERVAL 104 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 26),
    (44, NOW() - INTERVAL 104 DAY, 0, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 26),
    (45, NOW() - INTERVAL 104 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 26),
    -- 27번 영구정 (103일 전)
    (46, NOW() - INTERVAL 103 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 27),
    (47, NOW() - INTERVAL 103 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 27),
    (48, NOW() - INTERVAL 103 DAY, 0, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 27),
    -- 28번 떠난이 (102일 전)
    (49, NOW() - INTERVAL 102 DAY, 1, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 28),
    (50, NOW() - INTERVAL 102 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 28),
    (51, NOW() - INTERVAL 102 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 28),
    -- 29번 카카오유저 (101일 전)
    (52, NOW() - INTERVAL 101 DAY, 0, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 29),
    (53, NOW() - INTERVAL 101 DAY, 1, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 29),
    (54, NOW() - INTERVAL 101 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 29),
    -- 30번 구글유저 (100일 전)
    (55, NOW() - INTERVAL 100 DAY, 0, '호랑이쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 2, 'FRIEND_REQUEST', 30),
    (56, NOW() - INTERVAL 100 DAY, 0, '요리왕강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 4, 'FRIEND_REQUEST', 30),
    (57, NOW() - INTERVAL 100 DAY, 1, '코딩쌤강사님과 자동으로 친구가 되었습니다. 질문을 시작해보세요!', 6, 'FRIEND_REQUEST', 30),
    -- 학생-학생
    (58, NOW() - INTERVAL 41 DAY, 1, '민수님이 친구 요청을 보냈습니다.', 12, 'FRIEND_REQUEST', 13),
    (59, NOW() - INTERVAL 40 DAY, 0, '지영님과 친구가 되었습니다. 교류를 시작해보세요!', 13, 'FRIEND_REQUEST', 12),
    (60, NOW() - INTERVAL 36 DAY, 1, '현우님이 친구 요청을 보냈습니다.', 14, 'FRIEND_REQUEST', 12),
    (61, NOW() - INTERVAL 35 DAY, 0, '민수님과 친구가 되었습니다. 교류를 시작해보세요!', 12, 'FRIEND_REQUEST', 14),
    (62, NOW() - INTERVAL 31 DAY, 1, '민수님이 친구 요청을 보냈습니다.', 12, 'FRIEND_REQUEST', 18),
    (63, NOW() - INTERVAL 30 DAY, 0, '채원님과 친구가 되었습니다. 교류를 시작해보세요!', 18, 'FRIEND_REQUEST', 12),
    (64, NOW() - INTERVAL 25 DAY, 0, '서연님이 친구 요청을 보냈습니다.', 15, 'FRIEND_REQUEST', 12),
    (65, NOW() - INTERVAL 20 DAY, 0, '민수님이 친구 요청을 보냈습니다.', 12, 'FRIEND_REQUEST', 16),
    (66, NOW() - INTERVAL 18 DAY, 1, '하늘님이 친구 요청을 보냈습니다.', 17, 'FRIEND_REQUEST', 12),
    (67, NOW() - INTERVAL 17 DAY, 1, '민수님과 친구가 되었습니다. 교류를 시작해보세요!', 12, 'FRIEND_REQUEST', 12);

SET FOREIGN_KEY_CHECKS = 1;
