# calendar BC

## 1. 개요
calendar 바운디드 컨텍스트는 사용자가 등록하는 할 일(Todo)과 메모(Memo)를 관리하는 영역이다. 한 사용자가 날짜별로 할 일을 등록/수정/삭제/완료 체크하거나, 기간이 있는 메모를 등록/수정/삭제할 수 있고, 월별/일별로 조회할 수 있게 해준다. 일별 조회 시에는 viewing 도메인(강의 시청 영역)에서 "오늘 수강한 챕터 목록"을 가져와 함께 보여준다.

## 2. 패키지 구조

### domain
- `domain/model/Calendar.java` — 핵심 도메인 모델(Todo/Memo 공통 엔티티)
- `domain/repository/CalendarRepository.java` — 저장소 인터페이스(도메인이 필요로 하는 저장 기능 선언)
- `domain/exception/CalendarAccessDeniedException.java` — 본인 소유가 아닌 데이터 접근 시 예외
- `domain/exception/CalendarNotFoundException.java` — 대상 데이터를 찾을 수 없을 때 예외

### application
- `application/command/` — 쓰기 작업 요청을 담는 Command 객체 7종: `CreateTodoCommand`, `UpdateTodoCommand`, `DeleteTodoCommand`, `CheckTodoCommand`, `CreateMemoCommand`, `UpdateMemoCommand`, `DeleteMemoCommand`
- `application/usecase/CalendarCommandUseCase.java` — 쓰기 작업 유스케이스 인터페이스(오버로딩된 `handle()` 메서드로 Command 종류별 처리)
- `application/usecase/CalendarQueryUseCase.java` — 조회 작업 유스케이스 인터페이스(월별/일별 조회)
- `application/service/CalendarCommandService.java` — `CalendarCommandUseCase` 구현체, 트랜잭션 경계 안에서 도메인 규칙 검증 + 저장 조율
- `application/service/CalendarQueryService.java` — `CalendarQueryUseCase` 구현체, 읽기 전용 트랜잭션
- `application/port/TodayChapterPort.java` — 오늘 수강한 챕터 목록을 조회하는 포트 인터페이스(calendar 도메인이 viewing 도메인에 의존하지 않도록 의존성 역전, 구현체는 viewing 쪽에 있을 것으로 추정 — 이 경로에는 구현체 없음)
- `application/port/TodayChapterInfo.java` — `TodayChapterPort`의 반환 DTO(강의 제목/ID, 카테고리, 챕터 제목)

### infrastructure
- `infrastructure/config/CalendarRedisCacheConfig.java` — calendar 도메인 전용 Redis 캐시 설정(월별 캘린더 응답을 캐싱하기 위해 record 타입에 맞춘 Jackson 직렬화 설정)
- `infrastructure/persistence/CalendarJpaEntity.java` — DB `calendar` 테이블과 매핑되는 JPA 엔티티
- `infrastructure/persistence/CalendarJpaRepository.java` — Spring Data JPA 리포지토리(월별 조회 쿼리, 스케줄러용 오늘자 조회 쿼리 포함)
- `infrastructure/persistence/CalendarRepositoryAdapter.java` — `CalendarRepository` 구현체, 도메인 객체 ↔ JPA 엔티티 변환 담당
- `infrastructure/scheduler/CalendarNotificationScheduler.java` — 매일 오전 9시(`cron = "0 0 9 * * *"`)에 오늘 일정을 조회하는 스케줄러

### presentation
- `presentation/api/CalendarController.java` — REST 컨트롤러, `/api/v1/calendar` 하위 API 8개 제공
- `presentation/api/common/CalendarExceptionHandler.java` — calendar 전용 예외를 403/404로 변환하는 `@RestControllerAdvice`
- `presentation/api/common/CalendarResponseCode.java` — calendar 전용 응답 코드 상수(`CALENDAR-*`)
- `presentation/api/request/` — 요청 DTO 5종: `CheckTodoRequest`, `CreateMemoRequest`, `CreateTodoRequest`, `UpdateMemoRequest`, `UpdateTodoRequest`
- `presentation/api/response/` — 응답 DTO 4종: `DailyCalendarResponse`, `MemoResponse`, `MonthlyCalendarResponse`, `TodoResponse`

## 3. 진행 상태

### 구현되어 있는 기능
- Todo 등록/수정/삭제/체크(완료 토글)
- Memo 등록/수정/삭제(종료일 nullable, 종료일이 시작일보다 이전이면 도메인에서 예외)
- 월별 캘린더 조회(해당 월의 Memo 목록, Redis 캐시 적용)
- 일별 캘린더 조회(해당 날짜의 Todo 목록 + 오늘 수강한 챕터 목록)
- 본인 소유 데이터만 수정/삭제/체크 가능하도록 권한 검증(`isOwnedBy()`)
- 매일 오전 9시 전체 유저의 오늘자 일정을 조회하는 스케줄러

### 비어있거나 미완성으로 보이는 부분
- `CalendarNotificationScheduler`는 오늘 날짜 기준 일정을 조회해서 로그(`log.info`)만 남기고 끝난다. 실제 알림 발송(푸시, 이메일 등) 로직은 없다. 조회 결과를 사용하는 코드가 없어 "알림 발송"이라는 클래스/메서드명과 실제 동작(조회 후 로그만 남김) 사이에 괴리가 있다.
- `TodayChapterPort`의 구현체(Adapter)가 calendar 패키지 경로 안에는 없다. 주석에서는 "viewing 도메인에서 구현체(TodayChapterAdapter) 제공"이라고 명시하고 있어 viewing BC 쪽에 있을 것으로 보이나, 이 조사 범위(calendar 패키지) 밖이라 실제 존재 여부는 확인 필요.
- `CalendarJpaRepository`의 `findAllByStartLessThanEqualAndEndGreaterThanEqual` 쿼리는 Memo처럼 `end` 컬럼이 있는 경우를 가정한 파생 쿼리 메서드인데, Todo는 `end`가 null이다. Todo 데이터가 이 쿼리로 정상 조회되는지는 코드만으로는 확실하지 않음(확인 필요).
- `CalendarJpaEntity`에는 `createdAt` 등 생성일시 컬럼이 보이지 않는다. `Calendar` 도메인 모델의 주석에는 "createdAt 은 JpaEntity 에서 관리"라고 되어 있으나 실제 `CalendarJpaEntity.java`에는 해당 필드가 없다(확인 필요, `@EntityListeners`나 상위 클래스 상속 등으로 별도 관리될 수도 있음).

### TODO/FIXME 주석
- 코드 내에 실제 TODO/FIXME 마커 주석은 없음(grep 결과 "TODO"라는 문자열은 모두 `Calendar.Category.TODO` 열거값 관련 코드였고, 작업 예정을 뜻하는 주석은 발견되지 않음).

## 4. API 목록

컨트롤러 클래스 레벨 매핑: `@RequestMapping("/api/v1/calendar")` (`CalendarController`)

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| GET | /api/v1/calendar/monthly | CalendarController.getMonthlyCalendar | 해당 월 전체 Todo/Memo 를 반환합니다. (Javadoc `@Operation` 원문, 실제로는 Memo만 반환) |
| GET | /api/v1/calendar/daily | CalendarController.getDailyCalendar | 해당 날짜의 Todo 와 오늘 수강한 챕터 목록을 반환합니다. (Javadoc 원문) |
| POST | /api/v1/calendar/todo | CalendarController.createTodo | 새로운 Todo 를 등록합니다. (Javadoc 원문) |
| PATCH | /api/v1/calendar/todo/{calendarId} | CalendarController.updateTodo | 기존 Todo 를 수정합니다. (Javadoc 원문) |
| DELETE | /api/v1/calendar/todo/{calendarId} | CalendarController.deleteTodo | 기존 Todo 를 삭제합니다. (Javadoc 원문) |
| PATCH | /api/v1/calendar/todo/{calendarId}/check | CalendarController.checkTodo | Todo 의 완료 상태를 변경합니다. Memo 에는 호출 불가. (Javadoc 원문) |
| POST | /api/v1/calendar/memo | CalendarController.createMemo | 새로운 Memo 를 등록합니다. end 는 nullable. (Javadoc 원문) |
| PATCH | /api/v1/calendar/memo/{calendarId} | CalendarController.updateMemo | 기존 Memo 를 수정합니다. end 는 nullable. (Javadoc 원문) |
| DELETE | /api/v1/calendar/memo/{calendarId} | CalendarController.deleteMemo | 기존 Memo 를 삭제합니다. (Javadoc 원문) |

주의: `getMonthlyCalendar`의 `@Operation` 설명은 "Todo/Memo 를 반환합니다"라고 되어 있지만, 실제 `CalendarQueryService.handle()` 코드는 `MEMO` 카테고리만 필터링해서 반환한다(Todo는 일별 조회 API에서만 반환). 문서(Javadoc)와 실제 동작이 다른 부분이므로 참고.

총 9개 API.

## 5. 도메인 모델

### Calendar (domain/model/Calendar.java)
Todo와 Memo를 하나의 클래스로 표현하는 핵심 엔티티.

**주요 필드**
- `id` (Long) — 식별자
- `userId` (Long) — 소유자
- `title` (String) — 제목
- `category` (Category) — TODO 또는 MEMO
- `start` (LocalDate) — 시작일(모든 항목 공통)
- `end` (LocalDate, nullable) — 종료일, Memo 전용(Todo는 항상 null)
- `isCompleted` (boolean) — 완료 여부, Todo 전용

**Enum: Category**
- `TODO`
- `MEMO`

**비즈니스 메서드**
- `createTodo(userId, title, start)` — Todo 신규 생성용 정적 팩토리. `isCompleted=false`, `end=null`로 고정.
- `createMemo(userId, title, start, end)` — Memo 신규 생성용 정적 팩토리. `end`가 `start`보다 이전이면 `DomainRuleViolationException` 발생.
- `toggleComplete()` — Todo의 완료 상태를 반전시킴. `category`가 MEMO면 `DomainRuleViolationException` 발생(Memo는 체크 불가라는 규칙을 도메인에서 강제).
- `update(title, start, end)` — 제목/시작일/종료일 수정. `end`가 `start`보다 이전이면 예외.
- `isOwnedBy(userId)` — 전달된 userId가 이 항목의 소유자인지 비교. 서비스 계층의 권한 검증에서 사용.
- `reconstitute(id, userId, title, category, start, end, isCompleted)` — DB에서 조회한 데이터로 도메인 객체를 복원하기 위한 정적 팩토리(신규 생성용 `create*`와 구분).

### TodayChapterInfo (application/port/TodayChapterInfo.java)
`TodayChapterPort`의 반환 DTO. 필드: `lectureTitle`(String), `lectureId`(Long), `category`(String), `chapterTitle`(String). calendar 도메인이 viewing 도메인을 직접 참조하지 않도록 만든 값 객체.

### 예외 클래스
- `CalendarAccessDeniedException` — `RuntimeException` 상속, 본인 소유가 아닌 항목에 접근 시 발생(메시지만 전달)
- `CalendarNotFoundException` — `RuntimeException` 상속, 대상 항목을 찾을 수 없을 때 발생(메시지만 전달)

## 6. ERD 스키마 대조

### `calendar` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | id | |
| user_id | BIGINT | userId | |
| start | DATE | start | |
| title | VARCHAR(255) | title | |
| end | DATE | end | |
| category | ENUM('MEMO','TODO') | category | `@Enumerated(EnumType.STRING)`으로 매핑됨 |
| is_completed | BOOLEAN | isCompleted | |
| created_at | DATETIME | 없음 | JPA에 없음 - 확인 필요(3장 "비어있거나 미완성으로 보이는 부분"에서 이미 지적된 것과 동일한 문제) |

### DB에 없는 JPA 필드
- 없음(`CalendarJpaEntity`의 필드 6개 모두 DB 컬럼과 대응됨)

### Todo/Memo 테이블·엔티티 구조 확인
DB는 `calendar` 테이블 하나에 `category` ENUM('MEMO','TODO')으로 구분하고 있고, 코드도 동일하게 `CalendarJpaEntity` 클래스 하나만 존재한다(`infrastructure/persistence/` 디렉토리에 Todo용/Memo용 엔티티가 별도로 없음, `Calendar.Category` enum으로 구분). 즉 "테이블만 통합되어 있고 엔티티는 분리된" 구조가 아니라, 테이블과 JPA 엔티티 모두 Todo/Memo를 하나로 통합해서 다루는 구조다.
