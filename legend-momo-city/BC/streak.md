# streak BC

## 1. 개요

streak BC는 사용자의 학습(영상 시청) 지속 기록을 "잔디"(GitHub의 커밋 잔디처럼 날짜별로 활동량을 시각화하는 것) 형태로 관리하는 바운디드 컨텍스트다. 사용자가 특정 날짜에 얼마나 시청했는지(`dailyWatchedSeconds`)를 누적하고, 그 시간을 기준으로 레벨(`StreakLevel` 0~4단계)을 계산해서 저장한다. 이 BC는 직접 잔디를 생성하지 않고, viewing BC가 발행하는 `ProgressSavedEvent`를 구독해서 잔디를 누적시키며, 메인 페이지 진입 시 한 달치 잔디 조회, 친구의 잔디 조회 기능을 제공한다.

## 2. 패키지 구조

### domain
- `domain/model/`
  - `Streak` — 잔디 엔티티 (도메인 모델)
  - `StreakLevel` — 잔디 레벨 Enum
- `domain/repository/`
  - `StreakRepository` — 저장/조회를 위한 도메인 인터페이스 (포트)

### application
- `application/usecase/`
  - `StreakCommandUseCase` — 잔디 누적 유스케이스 인터페이스
  - `StreakQueryUseCase` — 잔디 월간 조회 유스케이스 인터페이스
- `application/service/`
  - `StreakCommandService` — `StreakCommandUseCase` 구현체 (잔디 누적 처리)
  - `StreakQueryService` — `StreakQueryUseCase` 구현체 (월간/친구 잔디 조회)

### infrastructure
- `infrastructure/adapter/`
  - `StreakRepositoryAdapter` — `StreakRepository` 구현체 (도메인 ↔ JPA 엔티티 변환)
- `infrastructure/config/`
  - `StreakRedisCacheConfig` — streak 전용 Redis 캐시 설정 (Jackson 직렬화, TTL)
- `infrastructure/event/`
  - `StreakEventHandler` — viewing BC의 `ProgressSavedEvent` 구독, 잔디 누적 트리거
- `infrastructure/metrics/`
  - `StreakMetrics` — Micrometer 기반 메트릭(신규 생성 횟수, 레벨업 횟수) 등록
- `infrastructure/persistence/`
  - `StreakJpaEntity` — JPA 엔티티 (streak 테이블 매핑)
  - `StreakJpaRepository` — Spring Data JPA 리포지토리

### presentation
- `presentation/api/`
  - `StreakController` — REST 컨트롤러
- `presentation/api/common/`
  - `StreakExceptionHandler` — streak 전용 예외 처리 (`@RestControllerAdvice`)
  - `StreakResponseCode` — API 응답 코드 상수
- `presentation/api/response/`
  - `StreakMonthlyResponse` — 월간 조회 응답 DTO (record)
  - `StreakResponse` — 날짜별 단건 응답 DTO (record)

## 3. 진행 상태

### 구현되어 있는 기능
- 잔디 누적: `StreakEventHandler`가 viewing BC의 `ProgressSavedEvent`를 구독(`@Async`, `AFTER_COMMIT`)해서 `StreakCommandUseCase.accumulate()` 호출 → 오늘 잔디가 없으면 신규 생성, 있으면 시청 시간 누적 후 레벨 재계산.
- 잔디 신규 생성/레벨업 시 `StreakMetrics`로 Micrometer 카운터 기록.
- 잔디 누적 시 해당 사용자·연·월 캐시를 `@CacheEvict`로 무효화.
- 잔디 월간 조회: `StreakQueryService.getMonthlyStreak()` — 동일 연-월 범위인지 검증 후 조회, `@Cacheable`로 Redis 캐싱(TTL은 자정까지 남은 시간).
- 친구 잔디 월간 조회: `getFriendMonthlyStreak()` — 내부적으로 `getMonthlyStreak()`을 재호출하는 방식으로 구현(별도 캐시 어노테이션 없음, 확인 필요: 위임 호출 시 `@Cacheable`이 프록시를 안 거쳐 실제 캐싱이 적용되지 않을 수 있음).
- REST API 2개 제공 (아래 4번 참고).
- streak 전용 예외 처리(`StreakExceptionHandler`)로 날짜 관련 예외를 400으로 변환.

### 비어있거나 미완성으로 보이는 부분
- 도메인 모델(`domain/model`)에 값객체(VO)가 따로 없고 `Streak`, `StreakLevel` 두 클래스만 존재. 잔디 "삭제" 기능은 코드상 없음(생성/누적/조회만 존재).
- 테스트 코드는 이 경로(`streak` 패키지) 안에서는 발견되지 않음 (확인 필요: `src/test` 쪽은 탐색 대상에서 제외됨).
- `StreakJpaEntity`에 `updatedAt` 컬럼이 없고 `createdAt`만 존재 (`@PrePersist`만 있고 `@PreUpdate` 없음) — 누적(수정) 시점 추적이 안 됨.

### TODO/FIXME 주석
- 코드 내에서 TODO/FIXME 주석은 발견되지 않음.

## 4. API 목록

컨트롤러 클래스 레벨 매핑: `@RequestMapping("/api/v2")` (`StreakController`)

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| GET | /api/v2/streak | StreakController.getMonthlyStreak | 잔디 월간 조회. 메인 페이지 진입 시 한 달 치 잔디 조회, 시청기록 없는 날짜는 포함 안 함 (Javadoc/Operation 주석 기반) |
| GET | /api/v2/streak/users/{targetUserId} | StreakController.getFriendMonthlyStreak | 친구 잔디 월간 조회. 친구 도시 방문 시 친구의 한달치 잔디 조회 (Javadoc/Operation 주석 기반) |

## 5. 도메인 모델

### Streak (domain/model/Streak.java)
필드
- `id` (Long)
- `userId` (Long)
- `streakDate` (LocalDate)
- `dailyWatchedSeconds` (int) — 해당 날짜의 누적 시청 시간(초)
- `level` (StreakLevel)

비즈니스 메서드
- `static create(userId, streakDate, watchedSeconds)` — 신규 잔디 생성. 생성 시점에 `StreakLevel.from()`으로 레벨 계산.
- `static reconstitute(id, userId, streakDate, dailyWatchedSeconds, level)` — DB에서 조회한 값으로 도메인 객체 복원 (JPA 엔티티 → 도메인 변환용).
- `accumulate(watchedSeconds)` — `dailyWatchedSeconds`에 값을 더하고 `level`을 재계산. `watchedSeconds <= 0`이면 무시(음수 방어 주석 존재).
- getter만 존재 (setter 없음, 불변성 유지 방식).

### StreakLevel (domain/model/StreakLevel.java) — Enum
값 목록: `LEVEL0`, `LEVEL1`, `LEVEL2`, `LEVEL3`, `LEVEL4`

비즈니스 메서드
- `static from(int seconds)` — 시청 시간(초) 기준으로 레벨 판정
  - `seconds <= 0` → LEVEL0
  - `seconds <= 600` → LEVEL1
  - `seconds <= 1800` → LEVEL2
  - `seconds <= 3600` → LEVEL3
  - 그 외 → LEVEL4

주석에 명시된 정책: "잔디 레벨 정책 ENUM, daily_watched_seconds 기준으로 레벨 결정"

## 6. ERD 스키마 대조

### `streak` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT NOT NULL (PK) | id (Long) | |
| user_id | BIGINT NULL | userId (Long) | DB는 NULL 허용인데 JPA는 `@Column(nullable = false)`로 선언되어 있음 (확인 필요) |
| daily_watched_seconds | INT NULL DEFAULT 0 | dailyWatchedSeconds (int) | DB는 NULL 허용인데 JPA는 `@Column(nullable = false)`로 선언되어 있음, DB의 DEFAULT 0도 JPA 쪽에는 명시 안 됨 (확인 필요) |
| streak_date | DATE NULL | streakDate (LocalDate) | DB는 NULL 허용인데 JPA는 `@Column(nullable = false)`로 선언되어 있음 (확인 필요) |
| level | ENUM('LEVEL0','LEVEL1','LEVEL2','LEVEL3','LEVEL4') NULL | level (StreakLevel, `@Enumerated(EnumType.STRING)`) | SQL 원문에 `'LEVEL1''LEVEL2'` 사이 콤마 누락(ERD 툴 원문 오타로 추정, 5단계 LEVEL0~LEVEL4로 해석). 코드 `StreakLevel` enum도 LEVEL0~LEVEL4 5개 값으로 일치. DB는 NULL 허용인데 JPA는 `@Column(nullable = false)`로 선언되어 있음 (확인 필요) |
| created_at | DATETIME NULL | createdAt (LocalDateTime) | DB는 NULL 허용인데 JPA는 `@Column(nullable = false, updatable = false)`로 선언되어 있음, `@PrePersist`로 값 채움 (확인 필요) |

- DB에는 있는데 JPA 엔티티에 없는 컬럼: 없음 (6개 컬럼 모두 대응 필드 존재)

### DB에 없는 JPA 필드
- 없음. `StreakJpaEntity`는 `BaseTimeEntity`를 import하고 있으나 실제로 `extends`하지 않고 필드도 직접 선언(`createdAt`)해서 사용 중이라, import만 있고 미사용 상태로 보임 (확인 필요).

### 참고
- 전반적으로 SQL 스키마의 `NULL` 허용 컬럼들이 JPA 엔티티에서는 전부 `nullable = false`로 더 엄격하게 선언되어 있음. 애플리케이션 계층에서는 값이 항상 채워지므로 실질적 문제는 아닐 수 있으나, DB 제약과 JPA 제약이 어긋나 있는 점은 확인이 필요함.
