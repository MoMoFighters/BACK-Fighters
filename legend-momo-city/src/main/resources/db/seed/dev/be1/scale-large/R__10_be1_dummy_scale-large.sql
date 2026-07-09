USE `momo`;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `admin_notice`;
TRUNCATE TABLE `report`;
TRUNCATE TABLE `error_log`;
TRUNCATE TABLE `access_log`;

-- =====================================================================
--  1. access_log — 50건
-- =====================================================================
INSERT INTO `access_log` (`id`, `action`, `created_at`, `ip`, `user_id`) VALUES
  (1, 'LOGIN', NOW() - INTERVAL 0 HOUR, '192.168.1.1', 12),
  (2, 'VIEW', NOW() - INTERVAL 1 HOUR, '192.168.2.2', 13),
  (3, 'LOGOUT', NOW() - INTERVAL 2 HOUR, '192.168.3.3', 14),
  (4, 'LOGIN', NOW() - INTERVAL 3 HOUR, '192.168.4.4', 15),
  (5, 'VIEW', NOW() - INTERVAL 4 HOUR, '192.168.5.5', 16),
  (6, 'LOGOUT', NOW() - INTERVAL 5 HOUR, '192.168.1.6', 17),
  (7, 'LOGIN', NOW() - INTERVAL 6 HOUR, '192.168.2.7', 18),
  (8, 'VIEW', NOW() - INTERVAL 7 HOUR, '192.168.3.8', 19),
  (9, 'LOGOUT', NOW() - INTERVAL 8 HOUR, '192.168.4.9', 20),
  (10, 'LOGIN', NOW() - INTERVAL 9 HOUR, '192.168.5.10', 21),
  (11, 'VIEW', NOW() - INTERVAL 10 HOUR, '192.168.1.11', 22),
  (12, 'LOGOUT', NOW() - INTERVAL 11 HOUR, '192.168.2.12', 23),
  (13, 'LOGIN', NOW() - INTERVAL 12 HOUR, '192.168.3.13', 24),
  (14, 'VIEW', NOW() - INTERVAL 13 HOUR, '192.168.4.14', 25),
  (15, 'LOGOUT', NOW() - INTERVAL 14 HOUR, '192.168.5.15', 26),
  (16, 'LOGIN', NOW() - INTERVAL 15 HOUR, '192.168.1.16', 27),
  (17, 'VIEW', NOW() - INTERVAL 16 HOUR, '192.168.2.17', 28),
  (18, 'LOGOUT', NOW() - INTERVAL 17 HOUR, '192.168.3.18', 29),
  (19, 'LOGIN', NOW() - INTERVAL 18 HOUR, '192.168.4.19', 30),
  (20, 'VIEW', NOW() - INTERVAL 19 HOUR, '192.168.5.20', 31),
  (21, 'LOGOUT', NOW() - INTERVAL 20 HOUR, '192.168.1.21', 32),
  (22, 'LOGIN', NOW() - INTERVAL 21 HOUR, '192.168.2.22', 33),
  (23, 'VIEW', NOW() - INTERVAL 22 HOUR, '192.168.3.23', 34),
  (24, 'LOGOUT', NOW() - INTERVAL 23 HOUR, '192.168.4.24', 35),
  (25, 'LOGIN', NOW() - INTERVAL 24 HOUR, '192.168.5.25', 36),
  (26, 'VIEW', NOW() - INTERVAL 25 HOUR, '192.168.1.26', 37),
  (27, 'LOGOUT', NOW() - INTERVAL 26 HOUR, '192.168.2.27', 38),
  (28, 'LOGIN', NOW() - INTERVAL 27 HOUR, '192.168.3.28', 39),
  (29, 'VIEW', NOW() - INTERVAL 28 HOUR, '192.168.4.29', 40),
  (30, 'LOGOUT', NOW() - INTERVAL 29 HOUR, '192.168.5.30', 41),
  (31, 'LOGIN', NOW() - INTERVAL 30 HOUR, '192.168.1.31', 42),
  (32, 'VIEW', NOW() - INTERVAL 31 HOUR, '192.168.2.32', 43),
  (33, 'LOGOUT', NOW() - INTERVAL 32 HOUR, '192.168.3.33', 44),
  (34, 'LOGIN', NOW() - INTERVAL 33 HOUR, '192.168.4.34', 45),
  (35, 'VIEW', NOW() - INTERVAL 34 HOUR, '192.168.5.35', 46),
  (36, 'LOGOUT', NOW() - INTERVAL 35 HOUR, '192.168.1.36', 47),
  (37, 'LOGIN', NOW() - INTERVAL 36 HOUR, '192.168.2.37', 48),
  (38, 'VIEW', NOW() - INTERVAL 37 HOUR, '192.168.3.38', 49),
  (39, 'LOGOUT', NOW() - INTERVAL 38 HOUR, '192.168.4.39', 50),
  (40, 'LOGIN', NOW() - INTERVAL 39 HOUR, '192.168.5.40', 51),
  (41, 'VIEW', NOW() - INTERVAL 40 HOUR, '192.168.1.41', 52),
  (42, 'LOGOUT', NOW() - INTERVAL 41 HOUR, '192.168.2.42', 53),
  (43, 'LOGIN', NOW() - INTERVAL 42 HOUR, '192.168.3.43', 54),
  (44, 'VIEW', NOW() - INTERVAL 43 HOUR, '192.168.4.44', 55),
  (45, 'LOGOUT', NOW() - INTERVAL 44 HOUR, '192.168.5.45', 56),
  (46, 'LOGIN', NOW() - INTERVAL 45 HOUR, '192.168.1.46', 57),
  (47, 'VIEW', NOW() - INTERVAL 46 HOUR, '192.168.2.47', 58),
  (48, 'LOGOUT', NOW() - INTERVAL 47 HOUR, '192.168.3.48', 59),
  (49, 'LOGIN', NOW() - INTERVAL 48 HOUR, '192.168.4.49', 60),
  (50, 'VIEW', NOW() - INTERVAL 49 HOUR, '192.168.5.50', 61);
-- =====================================================================
--  2. error_log — 20건
-- =====================================================================
INSERT INTO `error_log` (`id`, `created_at`, `updated_at`, `level`, `message`, `occurred_at`, `source`) VALUES
  (1, NOW() - INTERVAL 0 DAY, NOW() - INTERVAL 0 DAY, 'ERROR', '에러 메시지 1', NOW() - INTERVAL 0 DAY, 'Service1'),
  (2, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 'WARNING', '에러 메시지 2', NOW() - INTERVAL 1 DAY, 'Service2'),
  (3, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 'CRITICAL', '에러 메시지 3', NOW() - INTERVAL 2 DAY, 'Service3'),
  (4, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 'ERROR', '에러 메시지 4', NOW() - INTERVAL 3 DAY, 'Service4'),
  (5, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 'WARNING', '에러 메시지 5', NOW() - INTERVAL 4 DAY, 'Service5'),
  (6, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 'CRITICAL', '에러 메시지 6', NOW() - INTERVAL 5 DAY, 'Service1'),
  (7, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 'ERROR', '에러 메시지 7', NOW() - INTERVAL 6 DAY, 'Service2'),
  (8, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 'WARNING', '에러 메시지 8', NOW() - INTERVAL 7 DAY, 'Service3'),
  (9, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 'CRITICAL', '에러 메시지 9', NOW() - INTERVAL 8 DAY, 'Service4'),
  (10, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 'ERROR', '에러 메시지 10', NOW() - INTERVAL 9 DAY, 'Service5'),
  (11, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, 'WARNING', '에러 메시지 11', NOW() - INTERVAL 10 DAY, 'Service1'),
  (12, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY, 'CRITICAL', '에러 메시지 12', NOW() - INTERVAL 11 DAY, 'Service2'),
  (13, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY, 'ERROR', '에러 메시지 13', NOW() - INTERVAL 12 DAY, 'Service3'),
  (14, NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 13 DAY, 'WARNING', '에러 메시지 14', NOW() - INTERVAL 13 DAY, 'Service4'),
  (15, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 14 DAY, 'CRITICAL', '에러 메시지 15', NOW() - INTERVAL 14 DAY, 'Service5'),
  (16, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY, 'ERROR', '에러 메시지 16', NOW() - INTERVAL 15 DAY, 'Service1'),
  (17, NOW() - INTERVAL 16 DAY, NOW() - INTERVAL 16 DAY, 'WARNING', '에러 메시지 17', NOW() - INTERVAL 16 DAY, 'Service2'),
  (18, NOW() - INTERVAL 17 DAY, NOW() - INTERVAL 17 DAY, 'CRITICAL', '에러 메시지 18', NOW() - INTERVAL 17 DAY, 'Service3'),
  (19, NOW() - INTERVAL 18 DAY, NOW() - INTERVAL 18 DAY, 'ERROR', '에러 메시지 19', NOW() - INTERVAL 18 DAY, 'Service4'),
  (20, NOW() - INTERVAL 19 DAY, NOW() - INTERVAL 19 DAY, 'WARNING', '에러 메시지 20', NOW() - INTERVAL 19 DAY, 'Service5');
-- =====================================================================
--  3. report — 30건
-- =====================================================================
INSERT INTO `report` (`id`, `created_at`, `detail`, `is_resolved`, `reason`, `reported_user_id`, `reporter_user_id`, `resolved_at`, `target_id`, `target_path`, `target_type`) VALUES
  (1, NOW() - INTERVAL 0 DAY, NULL, 0, 'SPAM', NULL, 12, NULL, 1, NULL, 'POST'),
  (2, NOW() - INTERVAL 5 DAY, NULL, 0, 'SPAM', NULL, 13, NULL, 2, NULL, 'POST'),
  (3, NOW() - INTERVAL 10 DAY, NULL, 0, 'SPAM', NULL, 14, NULL, 3, NULL, 'POST'),
  (4, NOW() - INTERVAL 15 DAY, NULL, 0, 'SPAM', NULL, 15, NULL, 4, NULL, 'POST'),
  (5, NOW() - INTERVAL 20 DAY, NULL, 0, 'SPAM', NULL, 16, NULL, 5, NULL, 'POST'),
  (6, NOW() - INTERVAL 25 DAY, NULL, 0, 'SPAM', NULL, 17, NULL, 6, NULL, 'POST'),
  (7, NOW() - INTERVAL 30 DAY, NULL, 0, 'SPAM', NULL, 18, NULL, 7, NULL, 'POST'),
  (8, NOW() - INTERVAL 35 DAY, NULL, 0, 'SPAM', NULL, 19, NULL, 8, NULL, 'POST'),
  (9, NOW() - INTERVAL 40 DAY, NULL, 0, 'SPAM', NULL, 20, NULL, 9, NULL, 'POST'),
  (10, NOW() - INTERVAL 45 DAY, NULL, 0, 'SPAM', NULL, 21, NULL, 10, NULL, 'POST'),
  (11, NOW() - INTERVAL 50 DAY, NULL, 0, 'SPAM', NULL, 22, NULL, 11, NULL, 'POST'),
  (12, NOW() - INTERVAL 55 DAY, NULL, 0, 'SPAM', NULL, 23, NULL, 12, NULL, 'POST'),
  (13, NOW() - INTERVAL 60 DAY, NULL, 0, 'SPAM', NULL, 24, NULL, 13, NULL, 'POST'),
  (14, NOW() - INTERVAL 65 DAY, NULL, 0, 'SPAM', NULL, 25, NULL, 14, NULL, 'POST'),
  (15, NOW() - INTERVAL 70 DAY, NULL, 0, 'SPAM', NULL, 26, NULL, 15, NULL, 'POST'),
  (16, NOW() - INTERVAL 75 DAY, NULL, 0, 'SPAM', NULL, 27, NULL, 16, NULL, 'POST'),
  (17, NOW() - INTERVAL 80 DAY, NULL, 0, 'SPAM', NULL, 28, NULL, 17, NULL, 'POST'),
  (18, NOW() - INTERVAL 85 DAY, NULL, 0, 'SPAM', NULL, 29, NULL, 18, NULL, 'POST'),
  (19, NOW() - INTERVAL 90 DAY, NULL, 0, 'SPAM', NULL, 30, NULL, 19, NULL, 'POST'),
  (20, NOW() - INTERVAL 95 DAY, NULL, 0, 'SPAM', NULL, 31, NULL, 20, NULL, 'POST'),
  (21, NOW() - INTERVAL 100 DAY, NULL, 0, 'SPAM', NULL, 32, NULL, 21, NULL, 'POST'),
  (22, NOW() - INTERVAL 105 DAY, NULL, 0, 'SPAM', NULL, 33, NULL, 22, NULL, 'POST'),
  (23, NOW() - INTERVAL 110 DAY, NULL, 0, 'SPAM', NULL, 34, NULL, 23, NULL, 'POST'),
  (24, NOW() - INTERVAL 115 DAY, NULL, 0, 'SPAM', NULL, 35, NULL, 24, NULL, 'POST'),
  (25, NOW() - INTERVAL 120 DAY, NULL, 0, 'SPAM', NULL, 36, NULL, 25, NULL, 'POST'),
  (26, NOW() - INTERVAL 125 DAY, NULL, 0, 'SPAM', NULL, 37, NULL, 26, NULL, 'POST'),
  (27, NOW() - INTERVAL 130 DAY, NULL, 0, 'SPAM', NULL, 38, NULL, 27, NULL, 'POST'),
  (28, NOW() - INTERVAL 135 DAY, NULL, 0, 'SPAM', NULL, 39, NULL, 28, NULL, 'POST'),
  (29, NOW() - INTERVAL 140 DAY, NULL, 0, 'SPAM', NULL, 40, NULL, 29, NULL, 'POST'),
  (30, NOW() - INTERVAL 145 DAY, NULL, 0, 'SPAM', NULL, 41, NULL, 30, NULL, 'POST');
-- =====================================================================
--  4. admin_notice — 10건
-- =====================================================================
INSERT INTO `admin_notice` (`id`, `content`, `created_at`, `is_pinned`, `title`, `updated_at`) VALUES
  (1, '공지 내용 1', NOW() - INTERVAL 0 DAY, 1, '[공지] 공지사항 1', NOW() - INTERVAL 0 DAY),
  (2, '공지 내용 2', NOW() - INTERVAL 10 DAY, 0, '[공지] 공지사항 2', NOW() - INTERVAL 10 DAY),
  (3, '공지 내용 3', NOW() - INTERVAL 20 DAY, 0, '[공지] 공지사항 3', NOW() - INTERVAL 20 DAY),
  (4, '공지 내용 4', NOW() - INTERVAL 30 DAY, 0, '[공지] 공지사항 4', NOW() - INTERVAL 30 DAY),
  (5, '공지 내용 5', NOW() - INTERVAL 40 DAY, 0, '[공지] 공지사항 5', NOW() - INTERVAL 40 DAY),
  (6, '공지 내용 6', NOW() - INTERVAL 50 DAY, 0, '[공지] 공지사항 6', NOW() - INTERVAL 50 DAY),
  (7, '공지 내용 7', NOW() - INTERVAL 60 DAY, 0, '[공지] 공지사항 7', NOW() - INTERVAL 60 DAY),
  (8, '공지 내용 8', NOW() - INTERVAL 70 DAY, 0, '[공지] 공지사항 8', NOW() - INTERVAL 70 DAY),
  (9, '공지 내용 9', NOW() - INTERVAL 80 DAY, 0, '[공지] 공지사항 9', NOW() - INTERVAL 80 DAY),
  (10, '공지 내용 10', NOW() - INTERVAL 90 DAY, 0, '[공지] 공지사항 10', NOW() - INTERVAL 90 DAY);

SET FOREIGN_KEY_CHECKS = 1;
