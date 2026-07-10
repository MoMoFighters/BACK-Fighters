# report BC

## 1. 개요
report(신고) BC는 회원이 게시글, 댓글, 강의, 챕터, 리뷰, 채팅, 페이지 등을 신고 접수하는 기능과, 관리자가 접수된 신고 목록/상세를 조회하고 처리 완료로 표시하는 기능을 담당한다. 다른 바운디드 컨텍스트(admin, user 등)가 신고 데이터를 필요로 할 때 사용할 수 있게 여러 포트(Port)를 제공하는 역할도 함께 맡고 있다.

## 2. 패키지 구조

### domain (도메인 계층)
- `domain/model`
  - `Report` — 신고 한 건을 표현하는 핵심 엔티티(순수 도메인 객체, JPA 무관)
  - `ReportReason` — 신고 사유 enum
  - `ReportTargetType` — 신고 대상 종류 enum
- `domain/repository`
  - `ReportRepository` — 저장소 계약 인터페이스 (내부에 `ReportPage` record 포함)
- `domain/exception`
  - `ReportNotFoundException`

### application (응용 계층)
- `application/command`
  - `SubmitReportCommand` — 신고 접수 UseCase 입력 값 묶음(record)
- `application/usecase`
  - `ReportCommandUseCase` — 쓰기(신고 접수/처리) 계약 인터페이스
  - `ReportQueryUseCase` — 조회 계약 인터페이스 (내부에 `ReportList`, `ReportDetail` record 포함)
- `application/service`
  - `ReportCommandService` — `ReportCommandUseCase` 구현체
  - `ReportQueryService` — `ReportQueryUseCase` 구현체
- `application/port`
  - `ChapterParentPort` — CHAPTER 타입 신고의 부모 강의 ID 조회 (lecture BC가 구현)
  - `ChatContentPort` — CHAT 타입 신고의 채팅 내용 조회 (chat BC가 구현)
  - `CommentContentPort` — 댓글 내용 조회
  - `ReviewContentPort` — 리뷰 내용 조회
  - `ReportUserNamePort` — 신고자/피신고자 이름 일괄 조회 (user BC가 구현)

### infrastructure (인프라 계층)
- `infrastructure/persistence`
  - `ReportJpaEntity` — `report` 테이블과 매핑되는 JPA 엔티티
  - `ReportRepositoryAdapter` — `ReportRepository` 구현체(도메인 ↔ JPA 엔티티 변환)
  - `SpringDataReportRepository` — Spring Data JPA 리포지토리
- `infrastructure/adapter`
  - `PendingReportAdapter` — admin BC의 `PendingReportPort` 구현 (미처리 신고 목록)
  - `RecentReportAdapter` — admin BC의 `RecentReportPort` 구현 (최근 신고 목록)
  - `ReportStatsAdapter` — admin BC의 `ReportStatsPort` 구현 (미처리 신고 수)
  - `UserReportListAdapter` — user BC의 `UserReportListPort` 구현 (특정 유저가 당한 신고 목록)

### presentation (표현 계층)
- `presentation/api`
  - `ReportController` — 회원용 신고 접수 API
  - `AdminReportController` — 관리자용 신고 목록/상세/처리 API
  - `ReportExceptionHandler` — report 패키지 전용 예외 → HTTP 응답 변환
- `presentation/api/request`
  - `SubmitReportRequest`
- `presentation/api/response`
  - `ReportDetailResponse`
  - `ReportListResponse` (내부에 `Item` record 포함)

## 3. 진행 상태

### 구현되어 있는 기능
- 회원의 신고 접수 (`ReportController.submitReport`, MS-20)
- 관리자의 신고 목록 조회 — 전체 최근순 / 처리 여부(`isResolved`) 필터, 페이지네이션 포함 (MS-1)
- 관리자의 신고 상세 조회 — 신고자/피신고자 이름, 신고 대상 원문 내용(리뷰/댓글/채팅), 대상 삭제 여부(`isDeleted`), CHAPTER 타입일 때 부모 강의 ID까지 포함 (MS-2)
- 관리자의 신고 처리 완료 처리 (`PATCH /{id}/resolve`, MS-3)
- admin BC 대시보드용 포트 3종(대기 신고 목록, 최근 신고 목록, 미처리 신고 수) 제공
- user BC용 "내가 신고당한 목록" 포트 제공

### 비어있거나 미완성으로 보이는 부분
- `ChatContentPort`를 사용하는 CHAT 타입 신고 상세 조회 로직은 코드상 활성화되어 있으나, `ReportQueryService`의 필드 주석에 "정림님 ChatContentAdapter 완료 후 활성화"라고 적혀 있어, 실제 chat BC 쪽 구현 어댑터가 완료되지 않았을 가능성이 있다. (확인 필요)
- `resolveTargetContent`가 잡는 예외는 `ReviewNotFoundException`, `CommunityNotFoundException` 두 종류뿐이라, CHAT 관련 not-found 예외가 별도로 존재한다면 삭제 여부 판단에서 빠질 수 있다. (확인 필요)

### TODO/FIXME 주석
코드 전체를 grep한 결과 `TODO`, `FIXME` 키워드는 발견되지 않았다.

### status / ReportStatus 리팩토링 관련 확인 결과
실제 코드를 확인한 결과, **`ReportStatus`라는 enum은 report BC 어디에도 존재하지 않는다.** `Report` 도메인 모델(`domain/model/Report.java`)에는 `status` 필드 대신 `boolean isResolved`와 `LocalDateTime resolvedAt` 필드가 있고, 처리 완료를 표시하는 비즈니스 메서드는 `resolve()`이다(이미 처리된 건이면 아무 것도 하지 않고 반환하는 멱등 처리).

다만 이전에 논의됐던 "isRead + markAsRead()로 리팩토링" 계획과는 **다른 이름으로 이미 리팩토링이 진행되어 있다.** 코드에는 `isRead`/`markAsRead()`라는 이름은 존재하지 않으며, `ReportCommandService.java` 66번째 줄에 다음과 같은 주석만 옛 설계를 가리키는 흔적으로 남아있다.

> `// 2. 도메인 행위 호출 (isRead=true, handledAt=now, handlerAdminId 기록)`

즉, 실제 필드/메서드명은 `isResolved` / `resolve()` / `resolvedAt`이고, 위 주석은 리팩토링 이전 설계를 설명하던 것이 그대로 남아있는 것으로 보인다(주석과 실제 코드가 불일치). 정리하면:
- `status` 필드 → 없음 (boolean `isResolved`로 대체되어 있음)
- `ReportStatus` enum → 없음
- `isRead` / `markAsRead()` → 코드에 없음, 대신 `isResolved` / `resolve()`가 동일한 역할을 수행

## 4. API 목록

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| POST | /api/v1/reports | ReportController.submitReport | 로그인한 회원이 게시글/댓글/강의/채택 등을 신고한다. (Javadoc 기반) |
| GET | /api/v1/reports | AdminReportController.getReports | 최근 N개의 신고를 조회한다. isResolved 파라미터로 처리 여부 필터링 가능 (Javadoc 기반) |
| GET | /api/v1/reports/{id} | AdminReportController.getReport | 신고 1건의 전체 정보를 조회한다. (Javadoc 기반) |
| PATCH | /api/v1/reports/{id}/resolve | AdminReportController.resolveReport | 신고를 처리 완료 상태로 변경한다. isResolved=true, resolvedAt 기록. (Javadoc 기반) |

참고: `ReportController`와 `AdminReportController`는 동일한 base path(`/api/v1/reports`)를 공유하지만, 클래스 레벨 `@PreAuthorize`로 권한을 분리한다(`ReportController`는 인증된 사용자 누구나, `AdminReportController`는 `ADMIN` 권한 필수).

## 5. 도메인 모델

### Report (엔티티)
주요 필드:
- `id` (Long) — 식별자
- `reporterUserId` (Long, 필수) — 신고자 ID
- `targetType` (ReportTargetType, 필수) — 신고 대상 종류
- `targetId` (Long) — 신고 대상 ID (`PAGE` 타입이 아니면 필수)
- `reportedUserId` (Long, nullable) — 신고당한 유저 ID
- `targetPath` (String, nullable) — 신고 대상 URL
- `reason` (ReportReason, 필수) — 신고 사유
- `detail` (String, nullable) — 자유 설명
- `isResolved` (boolean) — 처리 완료 여부
- `createdAt` (LocalDateTime, 필수) — 신고 시각
- `resolvedAt` (LocalDateTime, nullable) — 처리 완료 시각

비즈니스 메서드:
- `static Report submit(...)` — 회원이 신고 버튼을 누를 때 사용하는 생성 팩토리 메서드. `id=null`, `isResolved=false`, `createdAt=now`로 신규 생성한다.
- `static Report restore(...)` — DB에서 꺼낸 모든 값을 그대로 받아 도메인 객체로 복원하는 팩토리 메서드.
- `resolve()` — 신고를 처리 완료 상태로 바꾼다. 이미 처리된 건이면 아무 동작도 하지 않고 즉시 반환한다(멱등 처리). 처리 시 `isResolved=true`, `resolvedAt=현재시각`으로 설정.
- 생성자는 `private`이며, 필수 값(`reporterUserId`, `targetType`, `targetType != PAGE`일 때의 `targetId`, `reason`, `createdAt`)이 없으면 `DomainRuleViolationException`을 던진다.

### ReportReason (enum)
값 목록: `SPAM`, `ABUSE`, `INAPPROPRIATE`, `COPYRIGHT`, `OTHER`
- `toKorean()` 메서드로 한국어 표시용 문자열 변환: SPAM→"스팸/광고", ABUSE→"욕설/혐오 표현", INAPPROPRIATE→"부적절한 내용", COPYRIGHT→"저작권 침해", OTHER→"기타"

### ReportTargetType (enum)
값 목록: `POST`, `COMMENT`, `LECTURE`, `CHAPTER`, `REVIEW`, `CHAT`, `PAGE`
- `PAGE` 타입은 `targetId`가 null이어도 허용된다(도메인 생성자에서 검증).

### ReportRepository.ReportPage (record, 도메인 계층)
- `List<Report> reports`, `long totalElements` — 페이지네이션 결과를 Spring 타입 없이 순수 도메인으로 전달하기 위한 값 묶음.

## 6. ERD 스키마 대조

### `report` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | id |  |
| reporter_user_id | BIGINT | reporterUserId |  |
| reported_user_id | BIGINT | reportedUserId |  |
| target_type | ENUM('LECTURE','CHAPTER','POST','REVIEW','COMMENT','CHAT','PAGE') | targetType |  |
| target_id | BIGINT | targetId |  |
| target_path | VARCHAR(500) | targetPath |  |
| reason | ENUM('SPAM','ABUSE','INAPPROPRIATE','COPYRIGHT','OTHER') | reason |  |
| detail | TEXT | detail | DB는 TEXT, JPA는 `@Column(length = 1000)`(VARCHAR(1000) 기본) — 타입 불일치 가능성 (확인 필요) |
| is_resolved | BOOLEAN | isResolved |  |
| resolved_at | DATETIME | resolvedAt |  |
| created_at | DATETIME | createdAt |  |

DB에 없는 JPA 필드: 없음.

### enum 값 대조
- **target_type / ReportTargetType**: 값 집합은 동일(`LECTURE`, `CHAPTER`, `POST`, `REVIEW`, `COMMENT`, `CHAT`, `PAGE` 7개, 모두 일치). 다만 선언 순서가 다르다.
  - DB: `LECTURE, CHAPTER, POST, REVIEW, COMMENT, CHAT, PAGE`
  - 코드(`ReportTargetType`): `POST, COMMENT, LECTURE, CHAPTER, REVIEW, CHAT, PAGE`
  - JPA 컬럼이 `String`(`@Column(name = "target_type", length = 20)`)으로 저장되므로 순서 차이가 매핑 오류를 일으키지는 않지만, DB `ENUM` 타입 정의 순서와 코드 enum 선언 순서를 맞춰두는 것이 유지보수에 유리하다.
- **reason / ReportReason**: 값 집합과 순서 모두 일치 — `SPAM, ABUSE, INAPPROPRIATE, COPYRIGHT, OTHER`.
