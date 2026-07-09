USE `momo`;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
--  1. user_oauth
-- =====================================================================
INSERT INTO `user_oauth` (`id`, `created_at`, `provider`, `provider_id`, `user_id`) VALUES
  (1, NOW() - INTERVAL 24 DAY, 'KAKAO', 'kakao_001', 29),
  (2, NOW() - INTERVAL 18 DAY, 2, 'google_001', 30);
-- =====================================================================
--  2. store
-- =====================================================================
INSERT INTO `store` (`id`, `created_at`, `name`, `price`, `type`, `url`) VALUES
  (1, NOW() - INTERVAL 120 DAY, '프로필 이미지 1', 100, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item01.png'),
  (2, NOW() - INTERVAL 120 DAY, '프로필 이미지 2', 150, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item02.png'),
  (3, NOW() - INTERVAL 120 DAY, '프로필 이미지 3', 200, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item03.png'),
  (4, NOW() - INTERVAL 120 DAY, '프로필 이미지 4', 250, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item04.png'),
  (5, NOW() - INTERVAL 120 DAY, '프로필 이미지 5', 300, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item05.png');
-- =====================================================================
--  3. order_history
-- =====================================================================
INSERT INTO `order_history` (`id`, `amount`, `created_at`, `item_id`, `reason`, `type`, `user_id`) VALUES
  (1, 100, NOW() - INTERVAL 90 DAY, 1, 'PROFILE', 'USED', 12),
  (2, 79, NOW() - INTERVAL 80 DAY, NULL, 'COMPLETE', 'GAINED', 13),
  (3, 50, NOW() - INTERVAL 70 DAY, NULL, 'REVIEW', 'GAINED', 14),
  (4, 100, NOW() - INTERVAL 60 DAY, 2, 'PROFILE', 'USED', 15),
  (5, 137, NOW() - INTERVAL 50 DAY, NULL, 'COMPLETE', 'GAINED', 16);

SET FOREIGN_KEY_CHECKS = 1;
