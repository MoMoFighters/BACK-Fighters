# review BC

## 1. 개요

review 바운디드 컨텍스트는 "수강평(강의 리뷰)" 기능을 담당한다. 학생이 자신이 신청(수강)한 강의에 대해 별점과 후기 내용을 등록하고, 관리자가 부적절한 수강평을 삭제(실제로는 상태만 DELETED로 바꾸는 소프트 삭제)할 수 있게 한다. 또한 특정 강의의 수강평 목록을 최신순으로 페이지 단위로 조회하는 기능과, 다른 바운디드 컨텍스트(강의 BC, 신고 BC)에 강의별 평균 별점/리뷰 개수, 특정 리뷰 내용을 제공하는 연동 기능도 포함한다.

## 2. 패키지 구조

### domain
- `domain/model` — `Review`(수강평 도메인 모델), `ReviewStatus`(수강평 상태 열거형)
- `domain/repository` — `ReviewRepository`(수강평 저장소 인터페이스)
- `domain/event` — `ReviewCreatedEvent`, `ReviewDeletedEvent`(수강평 생성/삭제 이벤트)
- `domain/exception` — `DuplicateReviewException`, `ReviewAccessDeniedException`, `ReviewNotFoundException`

### application
- `application/command` — `ReviewCommand`(내부에 `CreateReviewCommand` 레코드 포함)
- `application/query` — `ReviewQuery`(내부에 `GetReviewListQuery` 레코드 포함)
- `application/usecase` — `ReviewCommandUseCase`(등록/삭제 인터페이스), `ReviewQueryUseCase`(목록 조회 인터페이스)
- `application/service` — `ReviewCommandService`(등록/삭제 구현), `ReviewQueryService`(목록 조회 구현)

### infrastructure
- `infrastructure/adapter` — `LectureReviewQueryAdapter`(lecture BC의 `LectureReviewQueryPort` 구현, 강의별 리뷰 통계 제공), `ReviewContentAdapter`(report BC의 `ReviewContentPort` 구현, 리뷰 내용 제공), `ReviewRepositoryAdapter`(도메인 `ReviewRepository` 구현)
- `infrastructure/handler` — `ReviewMetricsEventHandler`(리뷰 생성/삭제 이벤트를 받아 메트릭 기록)
- `infrastructure/persistence` — `ReviewJpaEntity`(JPA 엔티티), `ReviewJpaRepository`(Spring Data JPA 인터페이스, 내부에 `ReviewStatsProjection` 포함)
- `infrastructure/event` — 실제 클래스 파일은 존재하지 않으나 `ReviewMetricsEventHandler`의 패키지 선언(`com.wanted.momocity.review.infrastructure.event`)이 이 경로를 참조함 (확인 필요: 실제 파일 위치는 `infrastructure/handler` 디렉터리이지만 package 문은 `infrastructure.event`로 되어 있어 패키지 경로와 실제 디렉터리가 불일치)

### presentation
- `presentation/api` — `ReviewController`(REST 컨트롤러), `ReviewExceptionHandler`(예외 처리)
- `presentation/api/request` — `CreateReviewRequest`
- `presentation/api/response` — `CreateReviewResponse`, `CreateReviewSuccessResponse`, `ReviewListResponse`

## 3. 진행 상태

### 구현되어 있는 기능
- 수강평 등록 (`POST /api/v1/lectures/{lectureId}/reviews`): 강의 존재 확인 → 수강 여부 확인 → 중복 작성 여부 확인 → 도메인 객체 생성/검증 → 저장 → 포인트 지급(30점) 및 포인트 내역 저장 → 이벤트 발행
- 수강평 삭제(관리자 전용, 소프트 삭제, `DELETE /api/v1/lectures/reviews/{reviewId}`)
- 수강평 목록 조회(강의별, 최신순, 페이지네이션, ACTIVE 상태만, `GET /api/v1/lectures/{lectureId}/reviews`)
- 강의 BC 연동: 강의별 평균 별점/리뷰 개수 단건 및 다건(Map) 조회 제공 (`LectureReviewQueryAdapter`)
- report BC 연동: 신고 상세 조회 시 리뷰 내용 및 삭제 여부 판단을 위한 내용 제공 (`ReviewContentAdapter`)
- 리뷰 생성/삭제 시 메트릭 기록(`MomoMetrics`, 트랜잭션 커밋 후 비동기적으로 이벤트 처리)
- 동시 요청으로 인한 DB unique 제약조건 위반(`DataIntegrityViolationException`) 시 409 중복 예외로 변환하는 방어 로직

### 비어있거나 미완성으로 보이는 부분
- `presentation/api/response/CreateReviewResponse`가 정의되어 있으나, `ReviewController`의 등록 API는 이 응답 대신 데이터 없는 `CreateReviewSuccessResponse`를 반환한다. `CreateReviewResponse`는 현재 어디에서도 사용되지 않는 것으로 보인다 (확인 필요: 다른 곳에서 참조하는지는 grep으로는 확인되지 않음).
- `ReviewJpaEntity`의 `@Table` 어노테이션 내 `uniqueConstraints`(user_id, lecture_id 조합 유니크 제약)가 주석 처리되어 있다. 애플리케이션 코드(`ReviewCommandService`)는 `DataIntegrityViolationException`을 잡아 처리하는 로직이 있지만, 실제 DB 제약이 주석 처리된 상태라 동시성 상황에서 중복 저장을 막지 못할 가능성이 있다 (확인 필요).
- `infrastructure/event` 패키지 선언과 실제 파일이 위치한 `infrastructure/handler` 디렉터리가 일치하지 않는다.
- `Review` 도메인 모델에는 상태(status) 필드가 없고, `ReviewStatus`는 리포지토리/JPA 엔티티 레벨에서만 사용된다. 즉 도메인 모델이 삭제 여부를 표현하지 못하는 구조로 보인다 (확인 필요: 의도된 설계인지, 도메인 모델에 상태를 추가할 계획인지는 코드만으로는 알 수 없음).

### TODO/FIXME 주석
- 코드 내에 TODO/FIXME 주석은 발견되지 않았다.

## 4. API 목록

컨트롤러 클래스 레벨 매핑: `@RequestMapping("/api/v1/lectures")` (`ReviewController`)

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| POST | /api/v1/lectures/{lectureId}/reviews | ReviewController.createReview | 로그인한 학생이 수강 중인 강의에 수강평을 등록합니다. |
| DELETE | /api/v1/lectures/reviews/{reviewId} | ReviewController.deleteReview | 관리자가 수강평을 삭제합니다. 실제 row를 삭제하지 않고 DELETED 상태로 변경합니다. (ROLE_ADMIN 권한 필요) |
| GET | /api/v1/lectures/{lectureId}/reviews | ReviewController.getReviews | 특정 강의의 ACTIVE 상태 수강평 목록을 최신순으로 페이지 조회합니다. |

## 5. 도메인 모델

### Review (domain/model/Review.java)
- 주요 필드
  - `id` (Long): 수강평 ID
  - `userId` (Long): 작성자 사용자 ID (필수)
  - `lectureId` (Long): 대상 강의 ID (필수)
  - `rating` (int): 별점, 1~5 범위만 허용
  - `content` (String): 수강평 내용, null/공백 불가, 저장 시 trim 처리
  - `createdAt` (LocalDateTime): 생성 시각
- 비즈니스 메서드
  - `create(userId, lectureId, rating, content)`: 신규 수강평 생성용 정적 팩토리 메서드. id/createdAt은 null로 시작.
  - `reconstitute(id, userId, lectureId, rating, content, createdAt)`: DB에서 조회한 값으로 도메인 객체를 복원하는 정적 팩토리 메서드.
  - 생성자(private)에서 userId/lectureId null 체크, rating 1~5 범위 체크, content null/공백 체크를 수행하며 위반 시 `DomainRuleViolationException` 발생.
  - 단순 getter만 존재(Lombok `@Getter`), 필드가 모두 `final`이라 setter는 없음(불변 객체).

### ReviewStatus (domain/model/ReviewStatus.java)
- Enum 값
  - `ACTIVE`: 모든 등록된 강의(수강평)는 기본적으로 이 상태
  - `DELETED`: 관리자만 변경 가능한 삭제 상태
- 주석에 "모든 등록된 강의는 ACTIVE 상태이다"라고 되어 있으나 실제로는 수강평(리뷰)의 상태를 의미하는 것으로 보임 (확인 필요: 주석 표현과 실제 의미가 다소 어긋남).

### 이벤트
- `ReviewCreatedEvent(reviewId, userId, lectureId)`: 수강평 생성 시 발행되는 레코드형 이벤트
- `ReviewDeletedEvent(reviewId)`: 수강평 삭제 시 발행되는 레코드형 이벤트

### 예외
- `DuplicateReviewException`: 동일 강의에 이미 수강평을 작성한 경우
- `ReviewAccessDeniedException`: 신청(수강)하지 않은 강의에 리뷰를 작성하려는 경우
- `ReviewNotFoundException`: 수강평을 찾을 수 없는 경우

### ReviewJpaEntity (infrastructure/persistence, 참고용)
- 도메인 모델과 별개로 JPA 엔티티에는 `status`(ReviewStatus, 기본값 ACTIVE) 필드가 추가로 존재하며, `softDelete()` 메서드로 상태를 DELETED로 변경하는 비즈니스성 메서드를 가지고 있음.

## 6. ERD 스키마 대조

`ReviewJpaEntity.java`(`src/main/java/com/wanted/momocity/review/infrastructure/persistence/ReviewJpaEntity.java`) 기준으로 대조함.

### `review` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | id (Long) | |
| user_id | BIGINT | userId (Long) | DB는 NULL 허용인데 JPA는 `nullable = false`로 되어 있음 (확인 필요) |
| lecture_id | BIGINT | lectureId (Long) | DB는 NULL 허용인데 JPA는 `nullable = false`로 되어 있음 (확인 필요) |
| rating | TINYINT | rating (int) | DB는 NULL 허용인데 JPA는 기본형 int(null 불가) + `nullable = false`로 되어 있음 (확인 필요) |
| content | TEXT | content (String) | DB는 NULL 허용인데 JPA는 `nullable = false`로 되어 있음 (확인 필요) |
| status | ENUM('ACTIVE', 'DELETED') | status (ReviewStatus, `@Enumerated(EnumType.STRING)`) | DB는 NULL 허용인데 JPA는 `nullable = false`, 기본값 `ACTIVE`로 되어 있음 (확인 필요) |
| created_at | DATETIME | createdAt (LocalDateTime, `@CreatedDate`) | DB는 NULL 허용인데 JPA는 `nullable = false, updatable = false`로 되어 있음 (확인 필요) |
| deleted_at | DATETIME | 없음 | JPA에 없음 - 확인 필요. `softDelete()`는 `status`만 DELETED로 바꿀 뿐, 삭제 시각을 기록하는 필드/로직이 코드상 존재하지 않음 |

### DB에 없는 JPA 필드
- 없음 (JPA 엔티티의 모든 필드가 DB 컬럼과 이름 대응됨)
