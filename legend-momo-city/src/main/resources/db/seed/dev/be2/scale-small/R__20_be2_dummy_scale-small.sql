USE `momo`;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `building`;
TRUNCATE TABLE `review`;

-- =====================================================================
--  1. review
-- =====================================================================
INSERT INTO `review` (`id`, `content`, `created_at`, `deleted_at`, `lecture_id`, `rating`, `status`, `user_id`) VALUES
  (1, '정말 좋은 강의입니다!', NOW() - INTERVAL 90 DAY, NULL, 1, 5, 'ACTIVE', 12),
  (2, '기대보다 훨씬 좋았어요', NOW() - INTERVAL 80 DAY, NULL, 2, 4, 'ACTIVE', 13),
  (3, '초보자에게 딱 맞아요', NOW() - INTERVAL 70 DAY, NULL, 3, 5, 'ACTIVE', 14),
  (4, '강사님 설명이 명확해요', NOW() - INTERVAL 60 DAY, NULL, 1, 4, 'ACTIVE', 15),
  (5, '다음 강의도 기대됩니다', NOW() - INTERVAL 50 DAY, NULL, 2, 5, 'ACTIVE', 16),
  (6, '내용이 기대보다 적어요', NOW() - INTERVAL 40 DAY, NULL, 3, 2, 'ACTIVE', 17),
  (7, '삭제된 리뷰', NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 10 DAY, 1, 3, 'DELETED', 18),
  (8, '도움이 많이 됐어요', NOW() - INTERVAL 20 DAY, NULL, 2, 5, 'ACTIVE', 19);
-- =====================================================================
--  2. building
-- =====================================================================
INSERT INTO `building` (`id`, `created_at`, `updated_at`, `category`, `level`, `position`, `user_id`) VALUES
  (1, NOW() - INTERVAL 90 DAY, NOW() - INTERVAL 90 DAY, 'FITNESS', 2, 1, 12),
  (2, NOW() - INTERVAL 80 DAY, NOW() - INTERVAL 80 DAY, 'COOK', 1, 2, 13),
  (3, NOW() - INTERVAL 70 DAY, NOW() - INTERVAL 70 DAY, 'STUDY', 3, 3, 14),
  (4, NOW() - INTERVAL 60 DAY, NOW() - INTERVAL 60 DAY, 'FITNESS', 1, 1, 15),
  (5, NOW() - INTERVAL 50 DAY, NOW() - INTERVAL 50 DAY, 'BEAUTY', 2, 2, 16);

SET FOREIGN_KEY_CHECKS = 1;
