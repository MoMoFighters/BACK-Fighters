# admin BC

## 1. 개요

admin(관리자) 바운디드 컨텍스트는 관리자 전용 화면(대시보드)에 필요한 통계·현황 데이터를 모아서 보여주고, 관리자 공지사항을 작성·수정·삭제·고정하는 기능, 그리고 시스템 접근 로그와 에러 로그를 조회하는 기능을 담당한다. 회원, 신고, 강의, 게시글 등 다른 바운디드 컨텍스트(BC)의 통계 수치는 직접 조회하지 않고, admin BC가 정의한 포트(Port) 인터페이스를 통해 각 BC의 어댑터가 값을 채워주는 방식으로 연결되어 있다.

## 2. 패키지 구조

### domain (도메인 계층)
- `domain/access` — 접근 로그 도메인
  - `AccessLog` : 접근 로그 한 건을 표현하는 도메인 모델 (읽기 전용, restore로만 복원)
  - `AccessLogAction` : 접근 로그 행위 enum
  - `AccessLogRepository` : 접근 로그 저장/조회 계약 인터페이스
- `domain/audit` — 에러 로그(감사) 도메인
  - `ErrorLevel` : 에러 심각도 enum
  - `ErrorLog` : 에러 로그 한 건을 표현하는 불변 도메인 모델
  - `ErrorLogRepository` : 에러 로그 저장/조회 계약 인터페이스
- `domain/notice` — 관리자 공지 도메인
  - `AdminNotice` : 공지 한 건을 표현하는 도메인 모델 (생성/수정/고정/고정해제 메서드 보유)
  - `AdminNoticeRepository` : 공지 저장/조회/삭제 계약 인터페이스

### application (애플리케이션 계층)
- `application/command`
  - `CreateNoticeCommand` : 공지 작성 요청 데이터를 담는 record
- `application/port` — 다른 BC 또는 인프라로부터 데이터를 받기 위한 포트 인터페이스
  - `LectureStatsPort`, `MemberStatsPort`, `PostStatsPort` : 강의/회원/게시글 통계 포트
  - `MonthlyCount` : 월별 집계 공통 record
  - `PendingReportPort`, `RecentReportPort`, `ReportStatsPort` : 신고 관련 포트 (report BC 어댑터가 구현)
  - `PendingTeacherPort` : 강사 승인 대기 목록 포트 (user BC 어댑터가 구현)
  - `UserNamePort` : 사용자 ID → 이름/역할 변환 포트 (user BC 어댑터가 구현)
  - `SystemHealthPort` : 인프라 상태 체크 포트 (admin BC 자체 구현)
- `application/service` — usecase 구현체
  - `AccessLogQueryService`, `AdminDashboardQueryService`, `AdminNoticeCommandService`, `AdminNoticeQueryService`, `ErrorLogQueryService`, `MonthlyStatsQueryService`
  - `FixedTotalPage<T>` : 고정 공지 병합 페이지네이션을 위해 `Page<T>`를 직접 구현한 커스텀 클래스
- `application/usecase` — usecase 계약(인터페이스)
  - `AccessLogQueryUseCase`, `AdminDashboardQueryUseCase`, `AdminNoticeCommandUseCase`, `AdminNoticeQueryUseCase`, `ErrorLogQueryUseCase`, `MonthlyStatsQueryUseCase`

### infrastructure (인프라 계층)
- `infrastructure/adapter`
  - `AccessLogRepositoryAdapter`, `AdminNoticeRepositoryAdapter`, `ErrorLogRepositoryAdapter` : 도메인 Repository 인터페이스의 JPA 구현체
  - `SystemHealthAdapter` : DB(JdbcTemplate)·메일(JavaMailSender) 상태를 직접 체크하는 `SystemHealthPort` 구현체 (파일 저장소는 "정상"으로 임시 하드코딩)
- `infrastructure/persistence`
  - `AccessLogJpaEntity`, `AdminNoticeJpaEntity`, `ErrorLogJpaEntity` : JPA 엔티티
  - `SpringDataAccessLogRepository`, `SpringDataAdminNoticeRepository`, `SpringDataErrorLogRepository` : Spring Data JPA 인터페이스

### presentation (표현 계층)
- `presentation/api`
  - `AccessLogController`, `AdminDashboardController`, `AdminNoticeController`, `ErrorLogController`
- `presentation/api/request`
  - `CreateNoticeRequest`, `UpdateNoticeRequest`, `DeleteNoticesRequest`
- `presentation/api/response`
  - `AccessLogResponse`, `AdminNoticeDetailResponse`, `AdminNoticeListResponse`, `AdminNoticePageResponse`, `DashboardSummaryResponse`, `ErrorLogResponse`, `MonthlyStatsResponse`

## 3. 진행 상태

### 구현되어 있는 기능
- 관리자 공지 CRUD 전체(작성/목록조회/상세조회/수정/단건삭제/선택삭제/고정/고정해제) — 컨트롤러부터 도메인까지 전 계층 구현 완료
- 접근 로그 목록 조회(전체/action 필터), 최근 N개 조회
- 에러 로그 최근 N개 조회 (레벨 필터 조회 메서드는 `ErrorLogRepository`에 정의되어 있으나, 컨트롤러/usecase에서 호출하는 곳은 확인되지 않음 — 사용 여부 확인 필요)
- 관리자 대시보드 요약 통계(cards, systemHealth, pendingTasks, recentReports, recentNotices, recentAccessLogs) 조합 조회
- 대시보드 월별 운영 추이(누적/신규) 통계 조회
- 시스템 상태 체크(웹서비스/DB/메일) — 단, 파일 저장소 체크는 "S3 연동 전 임시 정상 처리"로 코드 주석에 명시되어 있음(고정값 반환, 실제 체크 없음)

### 비어있거나 미완성으로 보이는 부분
- `LectureStatsPort`, `MemberStatsPort(일부)`, `PendingReportPort`, `RecentReportPort`, `ReportStatsPort`, `PostStatsPort`, `PendingTeacherPort`, `UserNamePort` 는 admin BC 안에 인터페이스(포트)만 존재하고, 구현체(어댑터)는 admin 패키지 안에서 발견되지 않았다. 코드 주석에 따르면 각각 lecture, report, user 등 다른 BC 쪽에서 어댑터를 구현하는 구조로 보인다. (실제 어댑터 구현 위치는 admin BC 범위 밖이라 확인 필요)
- `SystemHealthAdapter`의 파일 저장소 상태 체크는 실제 체크 로직 없이 "정상" 문자열을 고정 반환한다.
- `ErrorLogRepository.findByLevel()` 메서드가 정의되어 있지만, admin BC 내에서 이를 호출하는 서비스/컨트롤러 코드는 확인되지 않았다(미사용 가능성, 확인 필요).

### TODO/FIXME 주석
grep 결과 admin 패키지 내에 TODO/FIXME 주석은 발견되지 않았다.

## 4. API 목록

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| GET | /api/v1/logs/access | AccessLogController.getAccessLogs | 접근 로그를 page/limit 기준으로 조회. action 파라미터로 필터링 가능 (ADMIN 전용) |
| GET | /api/v1/dashboard/summary | AdminDashboardController.getDashboardSummary | 관리자 대시보드 요약 통계(회원/신고/강의 수 등)를 한 번에 조회 (ADMIN 전용) |
| GET | /api/v1/dashboard/monthly-stats | AdminDashboardController.getMonthlyStats | 연도별 월별 회원수/강의수/게시글수(누적) 조회. year 미입력 시 현재 연도 (ADMIN 전용) |
| GET | /api/v1/dashboard/monthly-stats/sub | AdminDashboardController.getMonthlyNewStats | 연도별 월별 신규 가입 회원/강의/게시글 수(신규분) 조회. year 미입력 시 현재 연도 (ADMIN 전용) |
| POST | /api/v1/admin-notices | AdminNoticeController.createNotice | 관리자가 공지를 작성한다 (ADMIN 전용) |
| GET | /api/v1/admin-notices | AdminNoticeController.getNoticeList | 공지 목록 조회. isPinned 파라미터 없으면 전체, 있으면 필터 조회 (ADMIN/TEACHER/STUDENT 열람 가능) |
| GET | /api/v1/admin-notices/{id} | AdminNoticeController.getNoticeDetail | 공지 id로 단건 상세 정보 조회 (ADMIN/TEACHER/STUDENT 열람 가능) |
| PUT | /api/v1/admin-notices/{id} | AdminNoticeController.updateNotice | 공지 title과 content 수정. isPinned는 수정 불가 (ADMIN 전용) |
| DELETE | /api/v1/admin-notices/{id} | AdminNoticeController.deleteNotice | 공지 id로 단건 삭제 (ADMIN 전용) |
| DELETE | /api/v1/admin-notices | AdminNoticeController.deleteNotices | id 목록으로 여러 공지를 한 번에 삭제 (ADMIN 전용) |
| PATCH | /api/v1/admin-notices/{id}/pin | AdminNoticeController.pinNotice | 공지를 상단에 고정. 기존 고정 공지는 자동 해제됨 (ADMIN 전용) |
| PATCH | /api/v1/admin-notices/{id}/unpin | AdminNoticeController.unpinNotice | 고정된 공지의 고정을 해제 (ADMIN 전용) |
| GET | /api/v1/error-logs | ErrorLogController.getRecentErrorLogs | 최근 N개의 에러 로그 조회. FE 대시보드 에러 로그 위젯 호출용 (ADMIN 전용) |

## 5. 도메인 모델

### AccessLog (`domain/access`)
읽기 전용 도메인 모델로 `restore()`로만 복원되며 setter가 없다.
- **필드**: `id`(Long), `userId`(Long, null이면 비로그인), `ip`(String), `action`(AccessLogAction), `createdAt`(LocalDateTime)
- **정적 팩토리 메서드**:
  - `restore(...)` : DB에서 꺼낸 값을 도메인 객체로 복원
  - `create(userId, ip, action)` : 신규 접근 로그 생성 (id는 null, createdAt은 now())
- setter 없이 getter만 존재 (단순 조회용 getter, 비즈니스 메서드 없음)

**AccessLogAction (enum)**: `LOGIN`, `LOGOUT`, `FORBIDDEN` — access_log.action 컬럼에 String으로 저장

### ErrorLog (`domain/audit`)
모든 필드가 final인 완전 불변 객체. private 생성자에서 필수값 검증(레벨/출처/메시지/발생시각 null 또는 빈값이면 `DomainRuleViolationException` 발생)을 수행하는 "항상 유효한 객체" 패턴을 사용한다.
- **필드**: `id`(Long), `level`(ErrorLevel), `source`(String, 에러 출처), `message`(String), `occurredAt`(LocalDateTime)
- **정적 팩토리 메서드**:
  - `occur(level, source, message)` : 신규 에러 발생 시 (id=null, occurredAt=now())
  - `restore(...)` : DB에서 복원 시 모든 필드 채움
- getter만 존재, 변경 메서드 없음(불변)

**ErrorLevel (enum)**: `CRITICAL`(서비스 중단 수준, 즉시 대응 필요), `ERROR`(기능 실패, 사용자 영향 있음), `WARNING`(경고, 사용자 영향 없음)

### AdminNotice (`domain/notice`)
- **필드**: `id`(Long, final), `title`(String), `content`(String), `isPinned`(boolean), `createdAt`(LocalDateTime, final), `updatedAt`(LocalDateTime)
- **정적 팩토리 메서드**:
  - `create(title, content, isPinned)` : 신규 생성, createdAt/updatedAt을 동일한 시각으로 설정(나노초 오차 방지)
  - `restore(...)` : DB 복원용
- **비즈니스 메서드**:
  - `update(title, content)` : title/content만 변경 가능(isPinned는 이 메서드로 변경 불가), updatedAt 갱신
  - `pin()` : isPinned를 true로 변경, updatedAt 갱신
  - `unpin()` : isPinned를 false로 변경, updatedAt 갱신
- getter: `getId`, `getTitle`, `getContent`, `isPinned`, `getCreatedAt`, `getUpdatedAt`

### 기타 포트 관련 값 객체 (application/port, 도메인은 아니지만 데이터 계약)
- `MonthlyCount(month, count)` — 월별 집계 공통 record
- `SystemHealthPort.HealthStatus(webService, database, fileStorage, mailService)` — 인프라 상태값 record
- `UserNamePort.UserInfo(name, role)`, `PendingReportPort.PendingReportItem`, `RecentReportPort.RecentReportItem`, `PendingTeacherPort.PendingTeacherItem` — 각 포트가 반환하는 데이터 record들

## 6. ERD 스키마 대조

대조 대상 코드: `AdminNoticeJpaEntity`(`infrastructure/persistence/AdminNoticeJpaEntity.java`), `AccessLogJpaEntity`(`infrastructure/persistence/AccessLogJpaEntity.java`)

### `admin_notice` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | id (Long, `@Id @GeneratedValue(IDENTITY)`) | |
| title | VARCHAR(200) | title (String) | JPA에는 길이 제약(`@Column(length=...)`) 명시 없음. DB의 VARCHAR(200) 제약은 엔티티 코드만 봐서는 확인 불가(확인 필요) |
| content | TEXT | content (String, `@Column(columnDefinition = "TEXT")`) | |
| is_pinned | BOOLEAN | isPinned (boolean) | |
| created_at | DATETIME | createdAt (LocalDateTime, `@Column(updatable = false)`) | |
| updated_at | DATETIME | updatedAt (LocalDateTime) | |

**DB에 없는 JPA 필드**: 없음

**is_pinned 관련 최근 버그 수정 확인**: 커밋 `f259de4 [BUG] isPinned 상단고정 예외처리`를 확인한 결과, `AdminNoticeJpaEntity` 자체에는 변경이 없었고, 수정은 `application/service/AdminNoticeQueryService.java`에 있었다. 고정 공지 1건 + 일반 공지 목록을 합쳐 반환할 때, 합친 리스트 크기(`merged.size()`)가 요청한 페이지 크기(`pageable.getPageSize()`)를 넘는 극단적인 경우(예: size=1)를 `merged.subList(0, pageable.getPageSize())`로 잘라내는 로직이 추가됨. 즉 이번 버그 수정은 엔티티/컬럼 매핑 문제가 아니라 애플리케이션 계층의 페이지네이션 병합 로직 문제였다.

### `access_log` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | id (Long, `@Id @GeneratedValue(IDENTITY)`) | |
| user_id | BIGINT | userId (Long, `@Column(name = "user_id")`) | |
| ip | VARCHAR(45) | ip (String, `@Column(name = "ip", length = 45)`) | |
| action | ENUM('LOGIN','LOGOUT','FORBIDDEN') | action (String, `@Column(name = "action", length = 20)`) | DB는 ENUM이지만 JPA 필드는 String으로 매핑되어 있고, `AccessLogAction.valueOf(action)` / `.name()`으로 도메인 enum과 수동 변환함(`@Enumerated` 미사용) |
| created_at | DATETIME | createdAt (LocalDateTime, `@Column(name = "created_at")`) | |

**DB에 없는 JPA 필드**: 없음

**action ENUM 일치 여부 확인**: `domain/access/AccessLogAction.java`의 enum 값은 `LOGIN, LOGOUT, FORBIDDEN`으로, DB의 `ENUM('LOGIN', 'LOGOUT', 'FORBIDDEN')`과 값 자체는 일치한다. 다만 JPA 엔티티는 이 enum을 직접 매핑(`@Enumerated`)하지 않고 String으로 저장한 뒤 `toDomain()`/`toEntity()`에서 수동으로 `valueOf()`/`.name()` 변환을 하고 있어, enum에 새 값을 추가할 경우 DB의 ENUM 정의도 함께 수정해야 한다(코드만으로는 자동 동기화되지 않음).
