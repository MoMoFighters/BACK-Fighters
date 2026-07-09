USE `momo`;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `admin_notice`;
TRUNCATE TABLE `report`;
TRUNCATE TABLE `error_log`;
TRUNCATE TABLE `access_log`;

-- =====================================================================
--  1. access_log
-- =====================================================================
INSERT INTO `access_log` (`id`, `action`, `created_at`, `ip`, `user_id`) VALUES
  (1, 'LOGIN', NOW() - INTERVAL 0 HOUR, '192.168.1.1', 12),
  (2, 'VIEW', NOW() - INTERVAL 1 HOUR, '192.168.1.2', 13),
  (3, 'LOGOUT', NOW() - INTERVAL 2 HOUR, '192.168.1.3', 14),
  (4, 'LOGIN', NOW() - INTERVAL 3 HOUR, '192.168.1.4', 15),
  (5, 'VIEW', NOW() - INTERVAL 4 HOUR, '192.168.1.5', 16),
  (6, 'LOGOUT', NOW() - INTERVAL 5 HOUR, '192.168.1.6', 17),
  (7, 'LOGIN', NOW() - INTERVAL 6 HOUR, '192.168.1.7', 18),
  (8, 'VIEW', NOW() - INTERVAL 7 HOUR, '192.168.1.8', 19),
  (9, 'LOGOUT', NOW() - INTERVAL 8 HOUR, '192.168.1.9', 20),
  (10, 'LOGIN', NOW() - INTERVAL 9 HOUR, '192.168.1.10', 21),
  (11, 'VIEW', NOW() - INTERVAL 10 HOUR, '192.168.1.11', 22),
  (12, 'LOGOUT', NOW() - INTERVAL 11 HOUR, '192.168.1.12', 23),
  (13, 'LOGIN', NOW() - INTERVAL 12 HOUR, '192.168.1.13', 24),
  (14, 'VIEW', NOW() - INTERVAL 13 HOUR, '192.168.1.14', 25),
  (15, 'LOGOUT', NOW() - INTERVAL 14 HOUR, '192.168.1.15', 26);
-- =====================================================================
--  2. error_log
-- =====================================================================
INSERT INTO `error_log` (`id`, `created_at`, `updated_at`, `level`, `message`, `occurred_at`, `source`) VALUES
  (1, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 'ERROR', 'NullPointerException in LectureService', NOW() - INTERVAL 3 DAY, 'LectureService'),
  (2, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 'WARNING', 'Slow query detected: 3.2s', NOW() - INTERVAL 2 DAY, 'QueryMonitor'),
  (3, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 'ERROR', 'DB connection timeout', NOW() - INTERVAL 1 DAY, 'DataSource'),
  (4, NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 6 HOUR, 'CRITICAL', 'Out of memory error', NOW() - INTERVAL 6 HOUR, 'JVM');
-- =====================================================================
--  3. report
-- =====================================================================
INSERT INTO `report` (`id`, `created_at`, `detail`, `is_resolved`, `reason`, `reported_user_id`, `reporter_user_id`, `resolved_at`, `target_id`, `target_path`, `target_type`) VALUES
  (1, NOW() - INTERVAL 10 DAY, NULL, 0, 'SPAM', NULL, 12, NULL, 1, NULL, 'POST'),
  (2, NOW() - INTERVAL 8 DAY, '욕설 포함', 0, 'ABUSE', 13, 12, NULL, 3, NULL, 'COMMENT'),
  (3, NOW() - INTERVAL 5 DAY, NULL, 1, 'INAPPROPRIATE', NULL, 14, NOW() - INTERVAL 3 DAY, 2, NULL, 'POST');
-- =====================================================================
--  4. admin_notice
-- =====================================================================
INSERT INTO `admin_notice` (`id`, `content`, `created_at`, `is_pinned`, `title`, `updated_at`) VALUES
  (1, '서버 점검 공지', NOW() - INTERVAL 30 DAY, 1, '[공지] 서버 정기 점검 안내', NOW() - INTERVAL 30 DAY),
  (2, '새로운 기능이 추가되었습니다', NOW() - INTERVAL 20 DAY, 0, '[업데이트] 캘린더 기능 개선', NOW() - INTERVAL 20 DAY),
  (3, '이벤트 안내', NOW() - INTERVAL 10 DAY, 0, '[이벤트] 출석 체크 이벤트', NOW() - INTERVAL 10 DAY);

SET FOREIGN_KEY_CHECKS = 1;
