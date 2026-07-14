SET FOREIGN_KEY_CHECKS = 0;
use momo;
-- =====================================================================
--  1. user_oauth
-- =====================================================================
INSERT INTO `user_oauth` (`id`, `created_at`, `provider`, `provider_id`, `user_id`) VALUES
  (1, NOW() - INTERVAL 24 DAY, 'KAKAO', 'kakao_001', 29),
  (2, NOW() - INTERVAL 18 DAY, 2, 'google_001', 30);
-- =====================================================================
--  2. store
-- =====================================================================
INSERT INTO `store`
(`id`, `price`, `type`, `url`, `name`, `created_at`)
VALUES
    (1,  150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item01.png', 'cat1',      NOW() - INTERVAL 60 DAY),
    (2,  150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item02.png', 'cat2',        NOW() - INTERVAL 58 DAY),
    (3,  150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item03.png', 'cat3',      NOW() - INTERVAL 55 DAY),
    (4,  150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item04.png', 'cat4',      NOW() - INTERVAL 52 DAY),
    (5,  150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item05.png', 'dog1',      NOW() - INTERVAL 50 DAY),
    (6,  150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item06.png', 'dog2',       NOW() - INTERVAL 48 DAY),
    (7,  150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item07.png', 'dog3',           NOW() - INTERVAL 45 DAY),
    (8,  150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item08.png', 'dog4',         NOW() - INTERVAL 42 DAY),
    (9,  150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item09.png', 'penguin1',           NOW() - INTERVAL 40 DAY),
    (10, 150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item10.png', 'penguin2',    NOW() - INTERVAL 38 DAY),
    (11, 150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item11.png', 'penguin3',         NOW() - INTERVAL 35 DAY),
    (12, 150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item12.png', 'penguin4',           NOW() - INTERVAL 32 DAY),
    (13, 150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item13.png', 'rabbit1',     NOW() - INTERVAL 28 DAY),
    (14, 150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item14.png', 'rabbit2',       NOW() - INTERVAL 24 DAY),
    (15, 150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item15.png', 'rabbit3',    NOW() - INTERVAL 20 DAY),
    (16, 150, 'PROFILE',  'https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/store/item16.png', 'rabbit4',      NOW() - INTERVAL 15 DAY);
--  3. order_history
-- =====================================================================
INSERT INTO `order_history` (`id`, `amount`, `created_at`, `item_id`, `reason`, `type`, `user_id`) VALUES
  (1, 100, NOW() - INTERVAL 90 DAY, 1, 'PROFILE', 'USED', 12),
  (2, 79, NOW() - INTERVAL 80 DAY, NULL, 'COMPLETE', 'GAINED', 13),
  (3, 50, NOW() - INTERVAL 70 DAY, NULL, 'REVIEW', 'GAINED', 14),
  (4, 100, NOW() - INTERVAL 60 DAY, 2, 'PROFILE', 'USED', 15),
  (5, 137, NOW() - INTERVAL 50 DAY, NULL, 'COMPLETE', 'GAINED', 16);

-- =====================================================================
--  payment
-- =====================================================================
INSERT INTO `payment` (`id`, `user_id`, `payment_id`, `original_payment_id`, `plan`, `price`, `status`, `created_at`, `updated_at`) VALUES

-- 김민수 (12) - PLUS 결제 시도했다가 실패
(1,  12, 'pay-12-001', NULL, 'PLUS', 30000, 'FAILED',  NOW() - INTERVAL 100 DAY, NOW() - INTERVAL 100 DAY),

-- 이지영 (13) - PLUS 구독 중, 결제 성공
(2,  13, 'pay-13-001', NULL, 'PLUS', 30000, 'SUCCESS', NOW() - INTERVAL 90 DAY,  NOW() - INTERVAL 90 DAY),

-- 박현우 (14) - PRO 구독 중, PLUS -> PRO 업그레이드 이력
(3,  14, 'pay-14-001', NULL, 'PLUS', 30000, 'SUCCESS', NOW() - INTERVAL 80 DAY,  NOW() - INTERVAL 80 DAY),
(4,  14, 'pay-14-002', NULL, 'PRO',  20000, 'SUCCESS', NOW() - INTERVAL 50 DAY,  NOW() - INTERVAL 50 DAY),

-- 최서연 (15) - PLUS 결제 후 환불
(5,  15, 'pay-15-001', NULL,          'PLUS', 30000, 'SUCCESS', NOW() - INTERVAL 70 DAY, NOW() - INTERVAL 70 DAY),
(6,  15, 'pay-15-002', 'pay-15-001',  'PLUS', 30000, 'REFUND',  NOW() - INTERVAL 68 DAY, NOW() - INTERVAL 68 DAY),

-- 강도현 (16) - PLUS 결제 시도했는데 취소 처리 실패
(7,  16, 'pay-16-001', NULL, 'PLUS', 30000, 'CANCEL_FAILED', NOW() - INTERVAL 60 DAY, NOW() - INTERVAL 60 DAY),

-- 윤하늘 (17) - PLUS 구독 중
(8,  17, 'pay-17-001', NULL, 'PLUS', 30000, 'SUCCESS', NOW() - INTERVAL 55 DAY, NOW() - INTERVAL 55 DAY),

-- 임채원 (18) - PRO 구독 중, 직접 PRO 결제
(9,  18, 'pay-18-001', NULL, 'PRO', 50000, 'SUCCESS', NOW() - INTERVAL 45 DAY, NOW() - INTERVAL 45 DAY),

-- 오승준 (19) - PLUS 결제 후 환불
(10, 19, 'pay-19-001', NULL,         'PLUS', 30000, 'SUCCESS', NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 40 DAY),
(11, 19, 'pay-19-002', 'pay-19-001', 'PLUS', 30000, 'REFUND',  NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 38 DAY),

-- 신유진 (20) - PLUS 결제 실패
(12, 20, 'pay-20-001', NULL, 'PLUS', 30000, 'FAILED', NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY),

-- 류지훈 (21) - PLUS 결제 성공 후 환불
(13, 21, 'pay-21-001', NULL,         'PLUS', 30000, 'SUCCESS', NOW() - INTERVAL 25 DAY, NOW() - INTERVAL 25 DAY),
(14, 21, 'pay-21-002', 'pay-21-001', 'PLUS', 30000, 'REFUND',  NOW() - INTERVAL 23 DAY, NOW() - INTERVAL 23 DAY),

-- 문소희 (22) - PRO 구독 중
(15, 22, 'pay-22-001', NULL, 'PRO', 50000, 'SUCCESS', NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY),

-- 배태양 (23) - PLUS 결제 시도 중 (PENDING)
(16, 23, 'pay-23-001', NULL, 'PLUS', 30000, 'PENDING', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),

-- 카카오유저 (29) - PLUS 결제 성공
(17, 29, 'pay-29-001', NULL, 'PLUS', 30000, 'SUCCESS', NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY),

-- 구글유저 (30) - PRO 결제 실패 후 재시도 성공
(18, 30, 'pay-30-001', NULL, 'PRO', 50000, 'FAILED',  NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(19, 30, 'pay-30-002', NULL, 'PRO', 50000, 'SUCCESS', NOW() - INTERVAL 8 DAY,  NOW() - INTERVAL 8 DAY);
SET FOREIGN_KEY_CHECKS = 1;
