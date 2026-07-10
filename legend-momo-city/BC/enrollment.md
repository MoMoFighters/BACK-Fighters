# enrollment BC

## 1. 개요
enrollment(수강신청) 바운디드 컨텍스트는 학생이 강의를 수강신청하는 기능과, 수강신청 결과로 만들어지는 "건물(Building)" 게이미피케이션 정보를 관리하는 책임을 가진다. 수강신청 시 중복 신청 여부와 강의 상태를 검증하고, 카테고리별 건물을 생성하며, 학생의 학습 진척도와 건물 성장 정보를 조회하는 기능도 함께 제공한다.

## 2. 패키지 구조

### domain
- `event` : `EnrollmentCompletedEvent` (수강신청 완료 이벤트)
- `exception` : `BuildingSelfAccessException`, `DuplicateEnrollmentException`, `EnrollmentLectureNotFoundException`, `InvalidEnrollmentLectureStatusException`
- `model` : `Building`, `Enrollment`
- `repository` : `BuildingRepository`, `EnrollmentRepository` (인터페이스)

### application
- `command` : `CreateEnrollmentCommand`
- `query` : `EnrollmentQuery` (내부에 `GetEnrollmentProgressQuery` record 포함)
- `port` : `EnrollmentLecturePort`, `ProgressPort`, `StudentAccountPort`
- `usecase` : `EnrollmentCommandUseCase`, `EnrollmentQueryUsecase`
- `service` : `EnrollmentCommandService`, `EnrollmentQueryService`

### infrastructure
- `adapter` : `AuthStudentAccountAdapter`, `EnrollmentRepositoryAdapter`, `LectureEnrollmentAdapter`, `LectureEnrollmentQueryAdapter`, `UserBuildingsAdapter`
- `event` : `EnrollmentMetricsEventHandler`
- `metrics` : `EnrollmentMetrics`
- `persistence` : `BuildingJpaEntity`, `BuildingRepositoryAdapter`, `EnrollmentJpaEntity`, `EnrollmentJpaRepository`, `SpringDataBuildingRepository`

### presentation/api
- `BuildingController` — 건물 조회 API
- `EnrollmentController` — 수강신청 생성 API
- `EnrollmentExceptionHandler` — enrollment 패키지 전용 예외 처리기(`@RestControllerAdvice`)
- `EnrollmentProgressController` — 학습 진척도 조회 API
- `response` : `CreateEnrollmentResponse`, `EnrollmentProgressResponse`

### presentation/endpoint
- `BuildingEndpoint` — actuator 커스텀 엔드포인트(`@Endpoint(id="building")`). 현재 내부에 실제 메서드는 없고 설명 주석만 있음(아래 3번 참고).

## 3. 진행 상태

**구현되어 있는 기능**
- 수강신청 생성(`EnrollmentCommandService.createEnrollment`): 중복 신청 검증 → 강의 상태(ACTIVE) 검증 → 카테고리 건물 없으면 생성 → 수강신청 저장 → `EnrollmentCompletedEvent` 발행
- 카테고리별 건물 자동 생성 로직(위치값 1~5 검증, 같은 위치에 다른 카테고리 건물이 있으면 예외)
- 내 건물 목록 조회 / 친구 마을 건물 목록 조회(`EnrollmentQueryService`), 본인 Id로 조회 시 예외, 친구 관계 아니면 예외
- 학습 진척도 조회(`getProgress`): 카테고리 없으면 전체 평균 진척도만, 카테고리 있으면 해당 카테고리 진척도+건물 레벨+경험치+건물 이미지 URL
- 수강신청 완료 이벤트를 구독해 Micrometer 지표(수강신청 누적, 건물 획득 누적)를 기록하는 `EnrollmentMetricsEventHandler` + `EnrollmentMetrics`
- lecture BC가 enrollment 정보를 필요로 할 때 쓰는 `LectureEnrollmentQueryAdapter` (lecture 쪽 포트 구현), user BC가 건물 정보를 필요로 할 때 쓰는 `UserBuildingsAdapter` (user 쪽 포트 구현)

**비어있거나 미완성으로 보이는 부분**
- `presentation/endpoint/BuildingEndpoint` 클래스는 `@Endpoint(id="building")`만 선언되어 있고 실제 `@ReadOperation` 등 메서드가 없다. 주석에 설계 의도(건물 획득/레벨업 누적 지표 노출)는 적혀 있으나 구현은 없는 상태.
- `EnrollmentMetrics.recordBuildingLevelUp()` 메서드는 존재하지만, enrollment BC 코드 내에서 이를 호출하는 곳은 확인되지 않음(레벨업 트리거 로직 자체가 이 BC 안에는 없음, 확인 필요).
- `Enrollment` 도메인의 `totalProgress`, `completedCount` 필드는 실제로는 항상 0으로 생성(`create`)되고, DB에서 진척도를 즉시 갱신하는 로직은 이 BC 코드 안에서 보이지 않음(진척도는 viewing 쪽 `ProgressPort`/`CategoryProgressPort`를 통해 별도로 계산해서 조회 시점에 가져오는 구조로 보임, 확인 필요).

**TODO/FIXME 주석**
- 코드 내 TODO/FIXME 문자열은 grep으로 확인되지 않음(없음).

## 4. API 목록

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| POST | /api/v1/lectures/{lectureId}/enrollments | EnrollmentController.createEnrollment | 로그인한 학생이 특정 강의를 수강신청합니다. |
| GET | /api/v1/user/buildings | BuildingController.renderingBuildings | 로그인 후 학생의 메인페이지 렌더링을 위한 정보 전달(카테고리, 포지션, 레벨) |
| GET | /api/v1/user/{userId}/buildings | BuildingController.getFriendBuildings | 친구 마을 방문 시 해당 사용자의 건물 정보를 조회한다. |
| GET | /api/v1/enrollments/progress | EnrollmentProgressController.getProgress | 로그인한 학생의 전체 학습 진척도 또는 카테고리별 학습 진척도와 건물 성장 정보를 조회합니다. |

## 5. 도메인 모델

### Building
- 필드: `id`, `userId`, `category`(`Category` enum), `position`, `level`, `createdAt`, `updatedAt`
- 비즈니스 메서드: `create(userId, category, position)` — 신규 건물을 항상 level 1로 생성하는 정적 팩토리 메서드. 나머지는 단순 getter만 존재.

### Enrollment
- 필드: `id`, `userId`, `lectureId`, `totalProgress`(int), `completedCount`(int), `enrolledAt`
- 생성자는 private, 대신 두 개의 정적 팩토리 메서드 제공
  - `create(userId, lectureId)` — 신규 수강신청 생성. `totalProgress`/`completedCount`는 0, `enrolledAt`은 현재 시각으로 설정
  - `reconstitute(id, userId, lectureId, totalProgress, completedCount, enrolledAt)` — DB 조회 결과를 도메인 객체로 복원
- 생성 시 아래 검증 로직을 모두 수행(위반 시 `DomainRuleViolationException`)
  - `userId`, `lectureId`, `enrolledAt` null 금지
  - `totalProgress`는 0~100 범위
  - `completedCount`는 0 이상

### Category (enum, `global.domain.model.Category` — enrollment BC 밖에서 정의되었지만 Building이 사용)
- 값: `FITNESS`, `STUDY`, `COOK`, `BEAUTY` (각 값마다 프로필 이미지 URL을 가짐. 4개 값만 grep으로 확인, 그 이상 있는지는 확인 필요)

### 이벤트/예외 (도메인 모델은 아니지만 domain 패키지 소속)
- `EnrollmentCompletedEvent(studentId, lectureId)` — 수강신청 완료 후 발행되는 record 이벤트
- `DuplicateEnrollmentException`, `InvalidEnrollmentLectureStatusException`, `EnrollmentLectureNotFoundException`, `BuildingSelfAccessException` — 모두 `RuntimeException`을 상속한 단순 예외 클래스

## 6. ERD 스키마 대조

대상 엔티티: `EnrollmentJpaEntity`, `BuildingJpaEntity` (둘 다 `infrastructure/persistence` 아래 실제로 존재함. `BuildingEndpoint`는 actuator 엔드포인트일 뿐이고, `building` 테이블에 대응하는 JPA 엔티티 자체는 정상 구현되어 있음).

### `enrollment` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | id (Long) | |
| user_id | BIGINT | userId (Long) | |
| lecture_id | BIGINT | lectureId (Long) | |
| total_progress | INT | totalProgress (int) | |
| completed_count | INT | completedCount (int) | |
| enrolled_at | DATETIME | enrolledAt (LocalDateTime) | |
| is_completed | BOOLEAN | 없음 | JPA에 없음 - 확인 필요 |

DB에 없는 JPA 필드: 없음

### `building` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | id (Long) | |
| user_id | BIGINT | userId (Long) | |
| category | ENUM('FITNESS','STUDY','COOK','BEAUTY','ART') | category (`Category`, `@Enumerated(EnumType.STRING)`) | `global.domain.model.Category` 실제 enum 값이 FITNESS/STUDY/COOK/BEAUTY/ART 5개로 DB와 일치(2번 섹션의 "4개 값만 확인"은 재확인 결과 5개가 맞음) |
| position | INT | position (Long) | 타입 불일치: DB는 INT, JPA는 Long - 확인 필요 |
| level | ENUM('LEVEL1','LEVEL2','LEVEL3') | level (Integer) | 타입 불일치: DB는 문자열 ENUM('LEVEL1'~'LEVEL3'), JPA/도메인은 `@Enumerated` 없이 순수 Integer(1부터 시작). DB 스키마의 ENUM 정의와 코드가 일치하지 않음 - 확인 필요 |
| created_at | DATETIME | createdAt (LocalDateTime, `BaseTimeEntity` 상속) | |
| updated_at | DATETIME | updatedAt (LocalDateTime, `BaseTimeEntity` 상속) | |

DB에 없는 JPA 필드: 없음
