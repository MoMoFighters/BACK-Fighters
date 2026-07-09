USE `momo`;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
--  1. post — 10개 large 원본
-- =====================================================================
INSERT INTO `post` (`id`, `created_at`, `updated_at`, `category`, `deleted_at`, `post_like`, `thumbnail_url`, `title`, `user_id`, `view_count`) VALUES
  (1, NOW() - INTERVAL 90 DAY, NOW() - INTERVAL 90 DAY, 'ART', NULL, 30, 'https://d1w7ptjpsyo7f4.cloudfront.net/community/images/e23f166f-d620-440d-b77a-1780cadfff02_lecture46Thumbnail.jpg', '드로잉 도구 추천해주세요', 60, 164),
  (2, NOW() - INTERVAL 89 DAY, NOW() - INTERVAL 89 DAY, 'STUDY', NULL, 0, 'https://d1w7ptjpsyo7f4.cloudfront.net/community/images/92bb0466-b007-4e21-a670-2ca21aa743e2_chapter43Thumbnail.jpg', '영어회화 스터디 모집', 22, 21),
  (3, NOW() - INTERVAL 88 DAY, NOW() - INTERVAL 88 DAY, 'FITNESS', NULL, 1, 'https://d1w7ptjpsyo7f4.cloudfront.net/community/images/4c4ce488-71f6-47ba-b6af-eccea12f3075_chapter30Thumbnail.jpg', '아침 루틴 공유합니다', 78, 52),
  (4, NOW() - INTERVAL 87 DAY, NOW() - INTERVAL 87 DAY, 'FITNESS', NULL, 1, 'https://d1w7ptjpsyo7f4.cloudfront.net/community/images/45c84c27-0e10-4c5d-9b22-f06d9b7c5b0b_chapter04Thumbnail.jpg', '운동 부상 조심하세요', 31, 41),
  (5, NOW() - INTERVAL 86 DAY, NOW() - INTERVAL 86 DAY, 'STUDY', NULL, 30, 'https://d1w7ptjpsyo7f4.cloudfront.net/community/images/189bc226-5fac-4d78-8cd3-dd7781458b94_lecture13Thumbnail.jpg', '공부 인증샷', 73, 111),
  (6, NOW() - INTERVAL 85 DAY, NOW() - INTERVAL 85 DAY, 'FREE', NULL, 8, 'https://d1w7ptjpsyo7f4.cloudfront.net/community/images/d8d690c4-039a-483b-bdbf-bd6a7658366c_CommunityFreeThumbnail.png', '공부 인증샷', 30, 74),
  (7, NOW() - INTERVAL 84 DAY, NOW() - INTERVAL 84 DAY, 'FREE', NULL, 30, 'https://d1w7ptjpsyo7f4.cloudfront.net/community/images/d8d690c4-039a-483b-bdbf-bd6a7658366c_CommunityFreeThumbnail.png', '잔디 채우는 재미', 80, 222),
  (8, NOW() - INTERVAL 83 DAY, NOW() - INTERVAL 83 DAY, 'FITNESS', NULL, 30, 'https://d1w7ptjpsyo7f4.cloudfront.net/community/images/9ffaa983-86d0-4b5b-96ba-dd325f0c8929_lecture14Thumbnail.jpg', '운동 부상 조심하세요', 42, 115),
  (9, NOW() - INTERVAL 82 DAY, NOW() - INTERVAL 82 DAY, 'ART', NULL, 1, 'https://d1w7ptjpsyo7f4.cloudfront.net/community/images/62d2067d-4e34-417a-be6c-4878cc2ed798_lecture08Thumbnail.jpeg', '수채화 그림 그려봤어요', 86, 54),
  (10, NOW() - INTERVAL 81 DAY, NOW() - INTERVAL 81 DAY, 'FITNESS', NULL, 3, 'https://d1w7ptjpsyo7f4.cloudfront.net/community/images/ed14f966-4c4d-4f12-bb5d-60d713ea4c13_lecture21Thumbnail.jpg', '스트레칭 효과 좋네요', 45, 43);
-- =====================================================================
--  2. post_like — 20건
-- =====================================================================
INSERT INTO `post_like` (`id`, `created_at`, `post_id`, `user_id`) VALUES
  (1, NOW() - INTERVAL 0 DAY, 1, 12),
  (2, NOW() - INTERVAL 3 DAY, 2, 13),
  (3, NOW() - INTERVAL 6 DAY, 3, 14),
  (4, NOW() - INTERVAL 9 DAY, 4, 15),
  (5, NOW() - INTERVAL 12 DAY, 5, 16),
  (6, NOW() - INTERVAL 15 DAY, 6, 17),
  (7, NOW() - INTERVAL 18 DAY, 7, 18),
  (8, NOW() - INTERVAL 21 DAY, 8, 19),
  (9, NOW() - INTERVAL 24 DAY, 9, 20),
  (10, NOW() - INTERVAL 27 DAY, 10, 21),
  (11, NOW() - INTERVAL 30 DAY, 1, 22),
  (12, NOW() - INTERVAL 33 DAY, 2, 23),
  (13, NOW() - INTERVAL 36 DAY, 3, 24),
  (14, NOW() - INTERVAL 39 DAY, 4, 25),
  (15, NOW() - INTERVAL 42 DAY, 5, 12),
  (16, NOW() - INTERVAL 45 DAY, 6, 13),
  (17, NOW() - INTERVAL 48 DAY, 7, 14),
  (18, NOW() - INTERVAL 51 DAY, 8, 15),
  (19, NOW() - INTERVAL 54 DAY, 9, 16),
  (20, NOW() - INTERVAL 57 DAY, 10, 17);
-- =====================================================================
--  3. comment — 10건
-- =====================================================================
INSERT INTO `comment` (`id`, `created_at`, `updated_at`, `content`, `deleted_at`, `parent_id`, `post_id`, `user_id`) VALUES
  (1, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, '좋은 글이에요!', NULL, NULL, 1, 12),
  (2, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, '저도 동감해요', NULL, NULL, 1, 13),
  (3, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, '감사합니다!', NULL, NULL, 2, 14),
  (4, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, '도움이 됐어요', NULL, NULL, 2, 15),
  (5, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, '응원합니다 :)', NULL, NULL, 3, 16),
  (6, NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 12 HOUR, '좋아요 눌렀어요', NULL, NULL, 3, 17),
  (7, NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 6 HOUR, '잘 봤습니다', NULL, NULL, 4, 18),
  (8, NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 3 HOUR, '공감해요', NULL, 1, 4, 12),
  (9, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR, 'ㅎㅎ저도요', NULL, 8, 4, 13),
  (10, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR, '감사합니다', NULL, NULL, 5, 19);
-- =====================================================================
--  4. learning_history — 24건
-- =====================================================================
INSERT INTO `learning_history` (`id`, `created_at`, `updated_at`, `chapter_id`, `is_completed`, `last_position_sec`, `last_watched_at`, `lecture_id`, `progress_rate`, `user_id`, `version`, `watched_seconds`) VALUES
  (1, NOW() - INTERVAL 50 DAY, NOW() - INTERVAL 50 DAY, 1, 0, 0, NULL, 1, 0, 12, 1, 0),
  (2, NOW() - INTERVAL 49 DAY, NOW() - INTERVAL 49 DAY, 2, 0, 0, NULL, 2, 1, 13, 1, 60),
  (3, NOW() - INTERVAL 48 DAY, NOW() - INTERVAL 48 DAY, 3, 0, 0, NULL, 3, 2, 14, 1, 120),
  (4, NOW() - INTERVAL 47 DAY, NOW() - INTERVAL 47 DAY, 4, 0, 0, NULL, 1, 3, 15, 1, 180),
  (5, NOW() - INTERVAL 46 DAY, NOW() - INTERVAL 46 DAY, 5, 0, 0, NULL, 2, 4, 16, 1, 240),
  (6, NOW() - INTERVAL 45 DAY, NOW() - INTERVAL 45 DAY, 6, 0, 0, NULL, 3, 5, 17, 1, 300),
  (7, NOW() - INTERVAL 44 DAY, NOW() - INTERVAL 44 DAY, 7, 0, 0, NULL, 1, 6, 18, 1, 360),
  (8, NOW() - INTERVAL 43 DAY, NOW() - INTERVAL 43 DAY, 8, 0, 0, NULL, 2, 7, 19, 1, 420),
  (9, NOW() - INTERVAL 42 DAY, NOW() - INTERVAL 42 DAY, 9, 0, 0, NULL, 3, 8, 20, 1, 480),
  (10, NOW() - INTERVAL 41 DAY, NOW() - INTERVAL 41 DAY, 10, 0, 0, NULL, 1, 9, 21, 1, 540),
  (11, NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 40 DAY, 1, 0, 0, NULL, 2, 10, 22, 1, 600),
  (12, NOW() - INTERVAL 39 DAY, NOW() - INTERVAL 39 DAY, 2, 0, 0, NULL, 3, 11, 23, 1, 660),
  (13, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 38 DAY, 3, 0, 0, NULL, 1, 12, 24, 1, 720),
  (14, NOW() - INTERVAL 37 DAY, NOW() - INTERVAL 37 DAY, 4, 0, 0, NULL, 2, 13, 25, 1, 780),
  (15, NOW() - INTERVAL 36 DAY, NOW() - INTERVAL 36 DAY, 5, 0, 0, NULL, 3, 14, 26, 1, 840),
  (16, NOW() - INTERVAL 35 DAY, NOW() - INTERVAL 35 DAY, 6, 0, 0, NULL, 1, 15, 27, 1, 900),
  (17, NOW() - INTERVAL 34 DAY, NOW() - INTERVAL 34 DAY, 7, 0, 0, NULL, 2, 16, 28, 1, 960),
  (18, NOW() - INTERVAL 33 DAY, NOW() - INTERVAL 33 DAY, 8, 0, 0, NULL, 3, 17, 29, 1, 1020),
  (19, NOW() - INTERVAL 32 DAY, NOW() - INTERVAL 32 DAY, 9, 0, 0, NULL, 1, 18, 12, 1, 1080),
  (20, NOW() - INTERVAL 31 DAY, NOW() - INTERVAL 31 DAY, 10, 0, 0, NULL, 2, 19, 13, 1, 1140),
  (21, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 1, 0, 0, NULL, 3, 20, 14, 1, 1200),
  (22, NOW() - INTERVAL 29 DAY, NOW() - INTERVAL 29 DAY, 2, 0, 0, NULL, 1, 21, 15, 1, 1260),
  (23, NOW() - INTERVAL 28 DAY, NOW() - INTERVAL 28 DAY, 3, 0, 0, NULL, 2, 22, 16, 1, 1320),
  (24, NOW() - INTERVAL 27 DAY, NOW() - INTERVAL 27 DAY, 4, 0, 0, NULL, 3, 23, 17, 1, 1380);
-- =====================================================================
--  5. streak — 56건
-- =====================================================================
INSERT INTO `streak` (`id`, `created_at`, `daily_watched_seconds`, `level`, `streak_date`, `user_id`) VALUES
  (1, CURDATE() - INTERVAL 0 DAY, 600, 'LEVEL1', CURDATE() - INTERVAL 0 DAY, 12),
  (2, CURDATE() - INTERVAL 1 DAY, 700, 'LEVEL1', CURDATE() - INTERVAL 1 DAY, 12),
  (3, CURDATE() - INTERVAL 2 DAY, 800, 'LEVEL1', CURDATE() - INTERVAL 2 DAY, 12),
  (4, CURDATE() - INTERVAL 3 DAY, 900, 'LEVEL1', CURDATE() - INTERVAL 3 DAY, 12),
  (5, CURDATE() - INTERVAL 4 DAY, 1000, 'LEVEL1', CURDATE() - INTERVAL 4 DAY, 12),
  (6, CURDATE() - INTERVAL 5 DAY, 1100, 'LEVEL1', CURDATE() - INTERVAL 5 DAY, 12),
  (7, CURDATE() - INTERVAL 6 DAY, 1200, 'LEVEL1', CURDATE() - INTERVAL 6 DAY, 12),
  (8, CURDATE() - INTERVAL 0 DAY, 600, 'LEVEL1', CURDATE() - INTERVAL 0 DAY, 13),
  (9, CURDATE() - INTERVAL 1 DAY, 700, 'LEVEL1', CURDATE() - INTERVAL 1 DAY, 13),
  (10, CURDATE() - INTERVAL 2 DAY, 800, 'LEVEL1', CURDATE() - INTERVAL 2 DAY, 13),
  (11, CURDATE() - INTERVAL 3 DAY, 900, 'LEVEL1', CURDATE() - INTERVAL 3 DAY, 13),
  (12, CURDATE() - INTERVAL 4 DAY, 1000, 'LEVEL1', CURDATE() - INTERVAL 4 DAY, 13),
  (13, CURDATE() - INTERVAL 5 DAY, 1100, 'LEVEL1', CURDATE() - INTERVAL 5 DAY, 13),
  (14, CURDATE() - INTERVAL 6 DAY, 1200, 'LEVEL1', CURDATE() - INTERVAL 6 DAY, 13),
  (15, CURDATE() - INTERVAL 0 DAY, 600, 'LEVEL1', CURDATE() - INTERVAL 0 DAY, 14),
  (16, CURDATE() - INTERVAL 1 DAY, 700, 'LEVEL1', CURDATE() - INTERVAL 1 DAY, 14),
  (17, CURDATE() - INTERVAL 2 DAY, 800, 'LEVEL1', CURDATE() - INTERVAL 2 DAY, 14),
  (18, CURDATE() - INTERVAL 3 DAY, 900, 'LEVEL1', CURDATE() - INTERVAL 3 DAY, 14),
  (19, CURDATE() - INTERVAL 4 DAY, 1000, 'LEVEL1', CURDATE() - INTERVAL 4 DAY, 14),
  (20, CURDATE() - INTERVAL 5 DAY, 1100, 'LEVEL1', CURDATE() - INTERVAL 5 DAY, 14),
  (21, CURDATE() - INTERVAL 6 DAY, 1200, 'LEVEL1', CURDATE() - INTERVAL 6 DAY, 14),
  (22, CURDATE() - INTERVAL 0 DAY, 600, 'LEVEL1', CURDATE() - INTERVAL 0 DAY, 15),
  (23, CURDATE() - INTERVAL 1 DAY, 700, 'LEVEL1', CURDATE() - INTERVAL 1 DAY, 15),
  (24, CURDATE() - INTERVAL 2 DAY, 800, 'LEVEL1', CURDATE() - INTERVAL 2 DAY, 15),
  (25, CURDATE() - INTERVAL 3 DAY, 900, 'LEVEL1', CURDATE() - INTERVAL 3 DAY, 15),
  (26, CURDATE() - INTERVAL 4 DAY, 1000, 'LEVEL1', CURDATE() - INTERVAL 4 DAY, 15),
  (27, CURDATE() - INTERVAL 5 DAY, 1100, 'LEVEL1', CURDATE() - INTERVAL 5 DAY, 15),
  (28, CURDATE() - INTERVAL 6 DAY, 1200, 'LEVEL1', CURDATE() - INTERVAL 6 DAY, 15),
  (29, CURDATE() - INTERVAL 0 DAY, 600, 'LEVEL1', CURDATE() - INTERVAL 0 DAY, 16),
  (30, CURDATE() - INTERVAL 1 DAY, 700, 'LEVEL1', CURDATE() - INTERVAL 1 DAY, 16),
  (31, CURDATE() - INTERVAL 2 DAY, 800, 'LEVEL1', CURDATE() - INTERVAL 2 DAY, 16),
  (32, CURDATE() - INTERVAL 3 DAY, 900, 'LEVEL1', CURDATE() - INTERVAL 3 DAY, 16),
  (33, CURDATE() - INTERVAL 4 DAY, 1000, 'LEVEL1', CURDATE() - INTERVAL 4 DAY, 16),
  (34, CURDATE() - INTERVAL 5 DAY, 1100, 'LEVEL1', CURDATE() - INTERVAL 5 DAY, 16),
  (35, CURDATE() - INTERVAL 6 DAY, 1200, 'LEVEL1', CURDATE() - INTERVAL 6 DAY, 16),
  (36, CURDATE() - INTERVAL 0 DAY, 600, 'LEVEL1', CURDATE() - INTERVAL 0 DAY, 17),
  (37, CURDATE() - INTERVAL 1 DAY, 700, 'LEVEL1', CURDATE() - INTERVAL 1 DAY, 17),
  (38, CURDATE() - INTERVAL 2 DAY, 800, 'LEVEL1', CURDATE() - INTERVAL 2 DAY, 17),
  (39, CURDATE() - INTERVAL 3 DAY, 900, 'LEVEL1', CURDATE() - INTERVAL 3 DAY, 17),
  (40, CURDATE() - INTERVAL 4 DAY, 1000, 'LEVEL1', CURDATE() - INTERVAL 4 DAY, 17),
  (41, CURDATE() - INTERVAL 5 DAY, 1100, 'LEVEL1', CURDATE() - INTERVAL 5 DAY, 17),
  (42, CURDATE() - INTERVAL 6 DAY, 1200, 'LEVEL1', CURDATE() - INTERVAL 6 DAY, 17),
  (43, CURDATE() - INTERVAL 0 DAY, 600, 'LEVEL1', CURDATE() - INTERVAL 0 DAY, 18),
  (44, CURDATE() - INTERVAL 1 DAY, 700, 'LEVEL1', CURDATE() - INTERVAL 1 DAY, 18),
  (45, CURDATE() - INTERVAL 2 DAY, 800, 'LEVEL1', CURDATE() - INTERVAL 2 DAY, 18),
  (46, CURDATE() - INTERVAL 3 DAY, 900, 'LEVEL1', CURDATE() - INTERVAL 3 DAY, 18),
  (47, CURDATE() - INTERVAL 4 DAY, 1000, 'LEVEL1', CURDATE() - INTERVAL 4 DAY, 18),
  (48, CURDATE() - INTERVAL 5 DAY, 1100, 'LEVEL1', CURDATE() - INTERVAL 5 DAY, 18),
  (49, CURDATE() - INTERVAL 6 DAY, 1200, 'LEVEL1', CURDATE() - INTERVAL 6 DAY, 18),
  (50, CURDATE() - INTERVAL 0 DAY, 600, 'LEVEL1', CURDATE() - INTERVAL 0 DAY, 19),
  (51, CURDATE() - INTERVAL 1 DAY, 700, 'LEVEL1', CURDATE() - INTERVAL 1 DAY, 19),
  (52, CURDATE() - INTERVAL 2 DAY, 800, 'LEVEL1', CURDATE() - INTERVAL 2 DAY, 19),
  (53, CURDATE() - INTERVAL 3 DAY, 900, 'LEVEL1', CURDATE() - INTERVAL 3 DAY, 19),
  (54, CURDATE() - INTERVAL 4 DAY, 1000, 'LEVEL1', CURDATE() - INTERVAL 4 DAY, 19),
  (55, CURDATE() - INTERVAL 5 DAY, 1100, 'LEVEL1', CURDATE() - INTERVAL 5 DAY, 19),
  (56, CURDATE() - INTERVAL 6 DAY, 1200, 'LEVEL1', CURDATE() - INTERVAL 6 DAY, 19);
-- =====================================================================
--  6. calendar — 6건
-- =====================================================================
INSERT INTO `calendar` (`id`, `category`, `end`, `is_completed`, `start`, `title`, `user_id`) VALUES
  (1, 'MEMO', NULL, 0, CURDATE(), '오늘의 목표', 12),
  (2, 'TODO', CURDATE() + INTERVAL 3 DAY, 0, CURDATE(), '강의 수강하기', 12),
  (3, 'MEMO', NULL, 1, CURDATE() - INTERVAL 1 DAY, '완료한 강의 메모', 13),
  (4, 'TODO', CURDATE() + INTERVAL 7 DAY, 0, CURDATE(), '프로그래밍 복습', 14),
  (5, 'MEMO', NULL, 0, CURDATE(), '요리 레시피 메모', 15),
  (6, 'TODO', NULL, 1, CURDATE() - INTERVAL 2 DAY, '완료 운동', 16);

SET FOREIGN_KEY_CHECKS = 1;
