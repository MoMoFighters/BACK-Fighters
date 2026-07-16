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
    -- 50
    (1,   50, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/rabbit.png',     '토끼',       NOW() - INTERVAL 68 DAY),
    (2,   50, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/pig.png',        '돼지',       NOW() - INTERVAL 66 DAY),
    (3,   50, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/frog.png',       '개구리',     NOW() - INTERVAL 64 DAY),
    (4,   50, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/cat.png',        '고양이',     NOW() - INTERVAL 62 DAY),

    -- 100
    (5,  100, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/hamstar.png',    '햄스터',     NOW() - INTERVAL 60 DAY),
    (6,  100, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/lion.png',       '사자',       NOW() - INTERVAL 58 DAY),
    (7,  100, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/otter.png',      '수달',       NOW() - INTERVAL 56 DAY),
    (8,  100, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/panda.png',      '판다',       NOW() - INTERVAL 54 DAY),

    -- 150
    (9,  150, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/chick.png',      '병아리',     NOW() - INTERVAL 52 DAY),
    (10, 150, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/dolphin.png',    '돌고래',     NOW() - INTERVAL 50 DAY),
    (11, 150, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/desertfox.png',  '사막여우',   NOW() - INTERVAL 48 DAY),
    (12, 150, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/gorila.png',     '고릴라',     NOW() - INTERVAL 46 DAY),
    (13, 150, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/quokka.png',     '쿼카',       NOW() - INTERVAL 44 DAY),
    (14, 150, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/hedgehog.png',   '고슴도치',   NOW() - INTERVAL 42 DAY),
    (15, 150, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/cow.png',        '젖소',       NOW() - INTERVAL 40 DAY),
    (16, 150, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/penguin.png',    '펭귄',       NOW() - INTERVAL 38 DAY),

    -- 200
    (17, 200, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/monkey.png',     '원숭이',     NOW() - INTERVAL 36 DAY),
    (18, 200, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/sheep.png',      '양',         NOW() - INTERVAL 34 DAY),
    (19, 200, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/squirrel.png',   '다람쥐',     NOW() - INTERVAL 32 DAY),
    (20, 200, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/tiger.png',      '호랑이',     NOW() - INTERVAL 30 DAY),

    -- 250
    (21, 250, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/zebra.png',      '얼룩말',     NOW() - INTERVAL 28 DAY),
    (22, 250, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/rhinoceros.png', '코뿔소',     NOW() - INTERVAL 26 DAY),
    (23, 250, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/elephant.png',   '코끼리',     NOW() - INTERVAL 24 DAY),
    (24, 250, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/polarbear.png',  '북극곰',     NOW() - INTERVAL 22 DAY),

    -- 300
    (25, 300, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/giraffe.png',    '기린',       NOW() - INTERVAL 20 DAY),
    (26, 300, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/dear.png',       '사슴',       NOW() - INTERVAL 18 DAY),
    (27, 300, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/redpanda.png',   '레서판다',   NOW() - INTERVAL 16 DAY),
    (28, 300, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/eagal.png',      '독수리',     NOW() - INTERVAL 14 DAY),

    -- 350
    (29, 350, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/raccoon.png',    '라쿤',       NOW() - INTERVAL 12 DAY),
    (30, 350, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/koala.png',      '코알라',     NOW() - INTERVAL 10 DAY),
    (31, 350, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/parrot.png',     '앵무새',     NOW() - INTERVAL 8 DAY),
    (32, 350, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/jaguar.png',     '재규어',     NOW() - INTERVAL 6 DAY),

    -- 1000 (프리미엄)
    (33, 1000, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/momo-gorila.png', '프리미엄 고릴라', NOW() - INTERVAL 4 DAY),
    (34, 1000, 'PROFILE', 'https://momocity-media.s3.ap-northeast-2.amazonaws.com/store/momo-cat.png',    '프리미엄 고양이', NOW() - INTERVAL 2 DAY);--  3. order_history
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
    (1,  12, 'pay-12-001', NULL, 'PLUS', 29900, 'FAILED',  NOW() - INTERVAL 100 DAY, NOW() - INTERVAL 100 DAY),

-- 이지영 (13) - PLUS 구독 중, 결제 성공
    (2,  13, 'pay-13-001', NULL, 'PLUS', 29900, 'SUCCESS', NOW() - INTERVAL 90 DAY,  NOW() - INTERVAL 90 DAY),

-- 박현우 (14) - PRO 구독 중, PLUS -> PRO 업그레이드 이력
    (3,  14, 'pay-14-001', NULL, 'PLUS', 29900, 'SUCCESS', NOW() - INTERVAL 80 DAY,  NOW() - INTERVAL 80 DAY),
    (4,  14, 'pay-14-002', NULL, 'PRO',  20000, 'SUCCESS', NOW() - INTERVAL 50 DAY,  NOW() - INTERVAL 50 DAY),

-- 최서연 (15) - PLUS 결제 후 환불
    (5,  15, 'pay-15-001', NULL,          'PLUS', 29900, 'SUCCESS', NOW() - INTERVAL 70 DAY, NOW() - INTERVAL 70 DAY),
    (6,  15, 'pay-15-002', 'pay-15-001',  'PLUS', 29900, 'REFUND',  NOW() - INTERVAL 68 DAY, NOW() - INTERVAL 68 DAY),

-- 강도현 (16) - PLUS 결제 시도했는데 취소 처리 실패
    (7,  16, 'pay-16-001', NULL, 'PLUS', 29900, 'CANCEL_FAILED', NOW() - INTERVAL 60 DAY, NOW() - INTERVAL 60 DAY),

-- 윤하늘 (17) - PLUS 구독 중
    (8,  17, 'pay-17-001', NULL, 'PLUS', 29900, 'SUCCESS', NOW() - INTERVAL 55 DAY, NOW() - INTERVAL 55 DAY),

-- 임채원 (18) - PRO 구독 중, 직접 PRO 결제
    (9,  18, 'pay-18-001', NULL, 'PRO', 49900, 'SUCCESS', NOW() - INTERVAL 45 DAY, NOW() - INTERVAL 45 DAY),

-- 오승준 (19) - PLUS 결제 후 환불
    (10, 19, 'pay-19-001', NULL,         'PLUS', 29900, 'SUCCESS', NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 40 DAY),
    (11, 19, 'pay-19-002', 'pay-19-001', 'PLUS', 29900, 'REFUND',  NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 38 DAY),

-- 신유진 (20) - PLUS 결제 실패
    (12, 20, 'pay-20-001', NULL, 'PLUS', 29900, 'FAILED', NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY),

-- 류지훈 (21) - PLUS 결제 성공 후 환불
    (13, 21, 'pay-21-001', NULL,         'PLUS', 29900, 'SUCCESS', NOW() - INTERVAL 25 DAY, NOW() - INTERVAL 25 DAY),
    (14, 21, 'pay-21-002', 'pay-21-001', 'PLUS', 29900, 'REFUND',  NOW() - INTERVAL 23 DAY, NOW() - INTERVAL 23 DAY),

-- 문소희 (22) - PRO 구독 중
    (15, 22, 'pay-22-001', NULL, 'PRO', 49900, 'SUCCESS', NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY),

-- 배태양 (23) - PLUS 결제 시도 중 (PENDING)
    (16, 23, 'pay-23-001', NULL, 'PLUS', 29900, 'PENDING', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),

-- 카카오유저 (29) - PLUS 결제 성공
    (17, 29, 'pay-29-001', NULL, 'PLUS', 29900, 'SUCCESS', NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY),

-- 구글유저 (30) - PRO 결제 실패 후 재시도 성공
    (18, 30, 'pay-30-001', NULL, 'PRO', 49900, 'FAILED',  NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
    (19, 30, 'pay-30-002', NULL, 'PRO', 49900, 'SUCCESS', NOW() - INTERVAL 8 DAY,  NOW() - INTERVAL 8 DAY);
SET FOREIGN_KEY_CHECKS = 1;
