USE `momo`;
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
INSERT INTO `notification` (`id`, `created_at`, `is_read`, `message`, `ref_id`, `type`, `user_id`) VALUES
  (1, NOW() - INTERVAL 2 DAY, 0, '김민수님이 댓글을 달았습니다', 1, 'POST', 13),
  (2, NOW() - INTERVAL 1 DAY, 0, '이지영님이 좋아요를 눌렀습니다', 1, 'POST', 12),
  (3, NOW() - INTERVAL 6 HOUR, 0, '친구 요청이 왔습니다', 15, 'FRIEND_REQUEST', 12);

SET FOREIGN_KEY_CHECKS = 1;
