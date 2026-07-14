SET FOREIGN_KEY_CHECKS = 0;
use momo;
-- =====================================================================
--  1. user_oauth — 30건
-- =====================================================================
INSERT INTO `user_oauth` (`id`, `created_at`, `provider`, `provider_id`, `user_id`) VALUES
  (1, NOW() - INTERVAL 0 DAY, 'KAKAO', 'oauth_user_001', 12),
  (2, NOW() - INTERVAL 5 DAY, 'GOOGLE', 'oauth_user_002', 13),
  (3, NOW() - INTERVAL 10 DAY, 'KAKAO', 'oauth_user_003', 14),
  (4, NOW() - INTERVAL 15 DAY, 'GOOGLE', 'oauth_user_004', 15),
  (5, NOW() - INTERVAL 20 DAY, 'KAKAO', 'oauth_user_005', 16),
  (6, NOW() - INTERVAL 25 DAY, 'GOOGLE', 'oauth_user_006', 17),
  (7, NOW() - INTERVAL 30 DAY, 'KAKAO', 'oauth_user_007', 18),
  (8, NOW() - INTERVAL 35 DAY, 'GOOGLE', 'oauth_user_008', 19),
  (9, NOW() - INTERVAL 40 DAY, 'KAKAO', 'oauth_user_009', 20),
  (10, NOW() - INTERVAL 45 DAY, 'GOOGLE', 'oauth_user_010', 21),
  (11, NOW() - INTERVAL 50 DAY, 'KAKAO', 'oauth_user_011', 22),
  (12, NOW() - INTERVAL 55 DAY, 'GOOGLE', 'oauth_user_012', 23),
  (13, NOW() - INTERVAL 60 DAY, 'KAKAO', 'oauth_user_013', 24),
  (14, NOW() - INTERVAL 65 DAY, 'GOOGLE', 'oauth_user_014', 25),
  (15, NOW() - INTERVAL 70 DAY, 'KAKAO', 'oauth_user_015', 26),
  (16, NOW() - INTERVAL 75 DAY, 'GOOGLE', 'oauth_user_016', 27),
  (17, NOW() - INTERVAL 80 DAY, 'KAKAO', 'oauth_user_017', 28),
  (18, NOW() - INTERVAL 85 DAY, 'GOOGLE', 'oauth_user_018', 29),
  (19, NOW() - INTERVAL 90 DAY, 'KAKAO', 'oauth_user_019', 30),
  (20, NOW() - INTERVAL 95 DAY, 'GOOGLE', 'oauth_user_020', 31),
  (21, NOW() - INTERVAL 100 DAY, 'KAKAO', 'oauth_user_021', 32),
  (22, NOW() - INTERVAL 105 DAY, 'GOOGLE', 'oauth_user_022', 33),
  (23, NOW() - INTERVAL 110 DAY, 'KAKAO', 'oauth_user_023', 34),
  (24, NOW() - INTERVAL 115 DAY, 'GOOGLE', 'oauth_user_024', 35),
  (25, NOW() - INTERVAL 120 DAY, 'KAKAO', 'oauth_user_025', 36),
  (26, NOW() - INTERVAL 125 DAY, 'GOOGLE', 'oauth_user_026', 37),
  (27, NOW() - INTERVAL 130 DAY, 'KAKAO', 'oauth_user_027', 38),
  (28, NOW() - INTERVAL 135 DAY, 'GOOGLE', 'oauth_user_028', 39),
  (29, NOW() - INTERVAL 140 DAY, 'KAKAO', 'oauth_user_029', 40),
  (30, NOW() - INTERVAL 145 DAY, 2, 'oauth_user_030', 41);
-- =====================================================================
--  2. store — 8건
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
-- =====================================================================
--  3. order_history — 50건
-- =====================================================================
INSERT INTO `order_history` (`id`, `amount`, `created_at`, `item_id`, `reason`, `type`, `user_id`) VALUES
  (1, 50, NOW() - INTERVAL 0 DAY, 1, 'PROFILE', 'USED', 12),
  (2, 60, NOW() - INTERVAL 3 DAY, NULL, 'COMPLETE', 'GAINED', 13),
  (3, 70, NOW() - INTERVAL 6 DAY, NULL, 'REVIEW', 'GAINED', 14),
  (4, 80, NOW() - INTERVAL 9 DAY, 4, 'BUS', 'USED', 15),
  (5, 90, NOW() - INTERVAL 12 DAY, NULL, 'GUESTBOOK', 'GAINED', 16),
  (6, 100, NOW() - INTERVAL 15 DAY, NULL, 'PROFILE', 'GAINED', 17),
  (7, 110, NOW() - INTERVAL 18 DAY, 7, 'COMPLETE', 'USED', 18),
  (8, 120, NOW() - INTERVAL 21 DAY, NULL, 'REVIEW', 'GAINED', 19),
  (9, 130, NOW() - INTERVAL 24 DAY, NULL, 'BUS', 'GAINED', 20),
  (10, 140, NOW() - INTERVAL 27 DAY, 2, 'GUESTBOOK', 'USED', 21),
  (11, 150, NOW() - INTERVAL 30 DAY, NULL, 'PROFILE', 'GAINED', 22),
  (12, 160, NOW() - INTERVAL 33 DAY, NULL, 'COMPLETE', 'GAINED', 23),
  (13, 170, NOW() - INTERVAL 36 DAY, 5, 'REVIEW', 'USED', 24),
  (14, 180, NOW() - INTERVAL 39 DAY, NULL, 'BUS', 'GAINED', 25),
  (15, 190, NOW() - INTERVAL 42 DAY, NULL, 'GUESTBOOK', 'GAINED', 26),
  (16, 200, NOW() - INTERVAL 45 DAY, 8, 'PROFILE', 'USED', 27),
  (17, 210, NOW() - INTERVAL 48 DAY, NULL, 'COMPLETE', 'GAINED', 28),
  (18, 220, NOW() - INTERVAL 51 DAY, NULL, 'REVIEW', 'GAINED', 29),
  (19, 230, NOW() - INTERVAL 54 DAY, 3, 'BUS', 'USED', 30),
  (20, 240, NOW() - INTERVAL 57 DAY, NULL, 'GUESTBOOK', 'GAINED', 31),
  (21, 250, NOW() - INTERVAL 60 DAY, NULL, 'PROFILE', 'GAINED', 32),
  (22, 260, NOW() - INTERVAL 63 DAY, 6, 'COMPLETE', 'USED', 33),
  (23, 270, NOW() - INTERVAL 66 DAY, NULL, 'REVIEW', 'GAINED', 34),
  (24, 280, NOW() - INTERVAL 69 DAY, NULL, 'BUS', 'GAINED', 35),
  (25, 290, NOW() - INTERVAL 72 DAY, 1, 'GUESTBOOK', 'USED', 36),
  (26, 300, NOW() - INTERVAL 75 DAY, NULL, 'PROFILE', 'GAINED', 37),
  (27, 310, NOW() - INTERVAL 78 DAY, NULL, 'COMPLETE', 'GAINED', 38),
  (28, 320, NOW() - INTERVAL 81 DAY, 4, 'REVIEW', 'USED', 39),
  (29, 330, NOW() - INTERVAL 84 DAY, NULL, 'BUS', 'GAINED', 40),
  (30, 340, NOW() - INTERVAL 87 DAY, NULL, 'GUESTBOOK', 'GAINED', 41),
  (31, 350, NOW() - INTERVAL 90 DAY, 7, 'PROFILE', 'USED', 42),
  (32, 360, NOW() - INTERVAL 93 DAY, NULL, 'COMPLETE', 'GAINED', 43),
  (33, 370, NOW() - INTERVAL 96 DAY, NULL, 'REVIEW', 'GAINED', 44),
  (34, 380, NOW() - INTERVAL 99 DAY, 2, 'BUS', 'USED', 45),
  (35, 390, NOW() - INTERVAL 102 DAY, NULL, 'GUESTBOOK', 'GAINED', 46),
  (36, 400, NOW() - INTERVAL 105 DAY, NULL, 'PROFILE', 'GAINED', 47),
  (37, 410, NOW() - INTERVAL 108 DAY, 5, 'COMPLETE', 'USED', 48),
  (38, 420, NOW() - INTERVAL 111 DAY, NULL, 'REVIEW', 'GAINED', 49),
  (39, 430, NOW() - INTERVAL 114 DAY, NULL, 'BUS', 'GAINED', 50),
  (40, 440, NOW() - INTERVAL 117 DAY, 8, 'GUESTBOOK', 'USED', 51),
  (41, 450, NOW() - INTERVAL 120 DAY, NULL, 'PROFILE', 'GAINED', 52),
  (42, 460, NOW() - INTERVAL 123 DAY, NULL, 'COMPLETE', 'GAINED', 53),
  (43, 470, NOW() - INTERVAL 126 DAY, 3, 'REVIEW', 'USED', 54),
  (44, 480, NOW() - INTERVAL 129 DAY, NULL, 'BUS', 'GAINED', 55),
  (45, 490, NOW() - INTERVAL 132 DAY, NULL, 'GUESTBOOK', 'GAINED', 56),
  (46, 500, NOW() - INTERVAL 135 DAY, 6, 'PROFILE', 'USED', 57),
  (47, 510, NOW() - INTERVAL 138 DAY, NULL, 'COMPLETE', 'GAINED', 58),
  (48, 520, NOW() - INTERVAL 141 DAY, NULL, 'REVIEW', 'GAINED', 59),
  (49, 530, NOW() - INTERVAL 144 DAY, 1, 'BUS', 'USED', 60),
  (50, 540, NOW() - INTERVAL 147 DAY, NULL, 'GUESTBOOK', 'GAINED', 61);


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
