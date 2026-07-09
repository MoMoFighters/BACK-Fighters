USE `momo`;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
--  1. user_oauth
-- =====================================================================
INSERT INTO `user_oauth` (`id`, `created_at`, `provider`, `provider_id`, `user_id`) VALUES
  (1, NOW() - INTERVAL 24 DAY, 1, 'kakao_001', 29),
  (2, NOW() - INTERVAL 18 DAY, 2, 'google_001', 30);
-- =====================================================================
--  2. store
-- =====================================================================
INSERT INTO `store` (`id`, `price`, `type`, `url`, `name`, `created_at`) VALUES
  (1, 30, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item01.png', 'cat1', NOW() - INTERVAL 57 DAY),
  (2, 30, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item02.png', 'cat2', NOW() - INTERVAL 54 DAY),
  (3, 50, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item03.png', 'cat3', NOW() - INTERVAL 51 DAY),
  (4, 50, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item04.png', 'cat4', NOW() - INTERVAL 48 DAY),
  (5, 50, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item05.png', 'dog1', NOW() - INTERVAL 45 DAY),
  (6, 80, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item06.png', 'dog2', NOW() - INTERVAL 42 DAY),
  (7, 80, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item07.png', 'dog3', NOW() - INTERVAL 39 DAY),
  (8, 100, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item08.png', 'dog4', NOW() - INTERVAL 36 DAY),
  (9, 100, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item09.png', 'penguin1', NOW() - INTERVAL 33 DAY),
  (10, 100, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item10.png', 'penguin2', NOW() - INTERVAL 30 DAY),
  (11, 120, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item11.png', 'penguin3', NOW() - INTERVAL 27 DAY),
  (12, 120, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item12.png', 'penguin4', NOW() - INTERVAL 24 DAY),
  (13, 150, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item13.png', 'rabbit1', NOW() - INTERVAL 21 DAY),
  (14, 150, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item14.png', 'rabbit2', NOW() - INTERVAL 18 DAY),
  (15, 200, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item15.png', 'rabbit3', NOW() - INTERVAL 15 DAY),
  (16, 200, 'PROFILE', 'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item16.png', 'rabbit4', NOW() - INTERVAL 12 DAY);
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
