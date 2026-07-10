# viewing BC

## 1. 개요

viewing BC는 사용자가 강의 영상을 "실제로 시청하는 과정"을 담당하는 바운디드 컨텍스트다. S3 Presigned URL 발급, 시청 진척도(watchedSeconds, progressRate) 저장, 챕터 완료 처리, 순차 시청 제한, 이어보기(resume) 기능을 제공한다.

`infrastructure/stomp` 패키지를 코드로 확인한 결과, 실시간 통신(웹소켓/STOMP) 기능이 실제로 구현되어 있다. 프론트가 5~10초 주기로 진척도를 HTTP PATCH 뿐 아니라 STOMP 메시지(`/pub/viewing/progress`)로도 전송할 수 있고, STOMP 연결이 끊기는 순간(`ViewingSessionDisconnectHandler`)에 마지막 재생 위치를 자동으로 저장하는 용도로 쓰인다. 즉 HTTP REST API와 STOMP 실시간 채널 두 가지 경로로 동일한 진척도 저장 유스케이스(`ViewingCommandUseCase`)를 재사용하는 구조다.

## 2. 패키지 구조

**domain**
- `event`: `ChapterCompletedEvent`, `CourseCompletedEvent`, `ProgressSavedEvent`
- `exception`: `ViewingAccessDeniedException`, `ViewingNotFoundException`
- `model`: `Chapter`, `LearningHistory`, `Lecture`
- `repository`: `LearningHistoryRepository` (인터페이스)

**application**
- `command`: `GetStreamingUrlCommand`, `SaveProgressCommand`
- `policy`: `EnrollmentAccessPolicy`, `SequentialAccessPolicy`
- `port`: `CategoryProgressInfo`, `CategoryProgressPort`, `ChapterPort`, `ChapterProgressInfo`, `EnrollmentPort`, `LectureChapterProgressPort`, `LecturePort`, `S3Port`, `UserPort`
- `service`: `PointGrantService`, `ViewingCommandService`, `ViewingQueryService`
- `usecase`: `ViewingCommandUseCase`, `ViewingQueryUseCase`

**infrastructure**
- `adapter`: `ChapterProgressAdapter`, `LearningHistoryRepositoryAdapter`, `LectureChapterProgressAdapter`, `ProgressAdapter`
- `catalog`: `ChapterCatalogAdapter`, `EnrollmentCatalogAdapter`, `LectureCatalogAdapter`, `TodayChapterAdapter`, `UserCatalogAdapter`
- `event`: `ViewingEventHandler`
- `metrics`: `ViewingMetrics`
- `persistence`: `LearningHistoryJpaEntity`, `LearningHistoryJpaRepository`
- `stomp`: `ViewingProgressChannelInterceptor`, `ViewingSessionDisconnectHandler`, `ViewingSessionRegistry`

**presentation**
- `api`: `ViewingController`
- `api/common`: `ViewingExceptionHandler`, `ViewingResponseCode`
- `api/request`: `SaveExitRequest`, `SaveProgressRequest`
- `api/response`: `ChapterProgressResponse`, `ChapterResumeResponse`, `ContinueLearningResponse`, `LectureMetaResponse`, `MyLecturesResponse`, `SaveExitResponse`, `SaveProgressResponse`, `StreamingUrlResponse`, `TotalProgressResponse`
- `api/stomp`: `ViewingProgressMessage`, `ViewingStompController`

## 3. 진행 상태

### 구현되어 있는 기능
- S3 Presigned URL 발급 (`getStreamingUrl`)
- 강의 메타데이터 조회 (`getLectureMeta`) — 챕터 목록, 진척도, 접근 가능 여부 포함
- 진척도 저장 (`saveProgress` / STOMP `/pub/viewing/progress`) — 낙관적 락(`@Version`) + 재시도 3회 로직 포함
- 나가기 시 마지막 재생 위치 저장 (`saveExit`), STOMP 연결 끊김 시 자동 저장 (`ViewingSessionDisconnectHandler`)
- 챕터 이어보기 조회 (`getChapterResume`)
- 카테고리별/전체 이어보기 조회 (`getContinueLearning`, `CategoryProgressPort`)
- 강의 전체 진척도 조회 (`getTotalProgress`), 챕터별 진척도 조회 (`getChapterProgress`)
- 내 수강 강의 목록 조회 (`getMyLectures`)
- 순차 시청 제한 정책 (`SequentialAccessPolicy`) — 이전 챕터 미완료 시 접근 차단, 관리자/강사는 예외
- 수강 여부 접근 제어 정책 (`EnrollmentAccessPolicy`) — 관리자 전체 접근, 강사 본인 강의 접근, 학생 수강신청 확인
- 챕터 완료 시 포인트 지급 (`PointGrantService`, `ViewingEventHandler`, `ChapterCompletedEvent` 기반, `@Async` + `AFTER_COMMIT`)
- Redis 캐싱 (`ChapterCatalogAdapter`, `LectureCatalogAdapter` — TTL 1시간, StringRedisTemplate 직접 사용)
- Micrometer 메트릭 (`ViewingMetrics` — 낙관적 락 충돌, 캐시 히트/미스, skip 차단, 저장 처리시간, S3 발급시간, watchedSeconds 분포)
- 외부 BC 연동 포트 구현: `enrollment`(진척도 제공, `ProgressAdapter`), `calendar`(오늘 학습 챕터 제공, `TodayChapterAdapter`), 팀원 BC가 `CategoryProgressPort`/`LectureChapterProgressPort`를 주입받아 사용

### 비어있거나 미완성으로 보이는 부분
- `CourseCompletedEvent`(도메인 이벤트, totalProgress=100 도달 시 발행 의도)는 정의만 있고, 코드 전체에서 `eventPublisher.publishEvent(new CourseCompletedEvent(...))` 형태의 실제 발행 코드를 찾지 못함 — 발행부 미구현으로 보임 (확인 필요).
- `ProgressSavedEvent`(잔디 누적용 이벤트로 주석에 설명됨)도 정의만 있고, `ViewingCommandService`/`ViewingQueryService` 어디에서도 실제로 발행하는 코드가 없음 — 미구현으로 보임 (확인 필요).
- `SaveExitResponse` 클래스가 존재하지만, `ViewingController.saveExit()`는 실제로 `SaveProgressResponse`를 반환하고 있어 `SaveExitResponse`는 어디에서도 사용되지 않는 것으로 보임 (확인 필요).
- `GetStreamingUrlCommand` record가 정의되어 있으나, `ViewingQueryService.getStreamingUrl()`은 이 Command 객체를 쓰지 않고 `(userId, lectureId, chapterId)` 파라미터를 그대로 받는 방식으로 구현되어 있어 미사용 클래스로 보임 (확인 필요).
- `ViewingController`의 "내 수강 강의 목록 조회" 매핑 경로가 주석(`// GET /api/v1/users/me/lectures`)과 실제 `@GetMapping` 값(`/user/me/lectures`, users가 아닌 user 단수)이 다름 — 오탈자로 보임 (확인 필요).

### TODO/FIXME
코드 전체를 확인한 결과 TODO/FIXME 주석은 발견되지 않았다.

## 4. API 목록

컨트롤러 클래스 레벨 매핑: `@RequestMapping("/api/v1")` (`ViewingController`)

| Method/Type | URL 또는 destination | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| GET | /api/v1/lectures/{lectureId}/chapters/{chapterId}/stream | ViewingController.getStreamingUrl | 챕터 클릭 시 해당 챕터 영상 URL 발급, 유효시간 1시간 (Swagger 설명) |
| GET | /api/v1/lectures/{lectureId}/meta | ViewingController.getLectureMeta | 플레이어 UI 상단에 강의 제목, 강사명, 현재 차시 표시용 (Swagger 설명) |
| PATCH | /api/v1/lectures/{lectureId}/chapters/{chapterId}/progress | ViewingController.saveProgress | 프론트에서 5~10초 주기로 현재 재생 위치 전송, 90% 이상 시청 시 챕터 완료 처리 (Swagger 설명) |
| PATCH | /api/v1/lectures/{lectureId}/chapters/{chapterId}/exit | ViewingController.saveExit | 강의실 나가기, 나갈 때 마지막 재생 위치 저장 (Swagger 설명) |
| GET | /api/v1/lectures/{lectureId}/chapters/{chapterId}/resume | ViewingController.getChapterResume | 마지막 재생 위치 반환, 시청 기록 없으면 lastPositionSec: 0 반환 (Swagger 설명) |
| GET | /api/v1/enrollments/continue-learning | ViewingController.getContinueLearning | 카테고리별 최근 이어보기 정보를 조회 (Swagger 설명) |
| GET | /api/v1/lectures/{lectureId}/progress | ViewingController.getTotalProgress | 완료 챕터 durationSec + 미완료 챕터 watchedSeconds 합산으로 계산 (Swagger 설명) |
| GET | /api/v1/lectures/{lectureId}/chapters/progress | ViewingController.getChapterProgress | 미시청 챕터는 watchedSeconds: 0, progressRate: 0, isCompleted: false 반환 (Swagger 설명) |
| GET | /api/v1/user/me/lectures | ViewingController.getMyLectures | 내 수강 강의 목록 조회, 수강 강의 없으면 lectures: [] 빈 배열 반환 (Swagger 설명) |

**STOMP/웹소켓**

| Type | Destination | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| @MessageMapping | /pub/viewing/progress (WebSocketConfig의 /pub prefix + "/viewing/progress") | ViewingStompController.handleProgress | STOMP 메시지로 진척도를 받아 세션 정보 갱신 후 기존 HTTP 진척도 저장 로직(ViewingCommandUseCase)을 재사용해 저장 (주석 기반) |

이 외에 STOMP CONNECT 시점 JWT 인증은 `@MessageMapping`이 아니라 `ViewingProgressChannelInterceptor`(ChannelInterceptor)가 처리하며, 연결 종료 시 처리는 `ViewingSessionDisconnectHandler`가 Spring 이벤트(`SessionDisconnectEvent`)로 처리한다 (엔드포인트가 아니므로 표에는 별도 기재하지 않음).

## 5. 도메인 모델

### Chapter (domain/model/Chapter.java)
catalog 컨텍스트가 소유하는 챕터를 viewing이 READ 전용으로 복원한 도메인 객체. `create()`가 없고 `reconstitute()`만 존재(주석에 명시: "생성/수정 없이 조회만 하기 때문에 create()는 생성하지 않음").

주요 필드: `id`, `lectureId`, `chapterThumbnailUrl`, `title`, `orderNo`, `videoUrl`, `durationSec`

비즈니스 메서드: 없음 (getter 전용, `reconstitute()`는 정적 팩토리)

### Lecture (domain/model/Lecture.java)
catalog 컨텍스트 소유 강의를 viewing이 READ 전용으로 복원한 도메인 객체.

주요 필드: `id`, `teacherId`, `title`, `thumbnailUrl`, `category`, `instructorName`, `status`

Enum: `VideoStatus` — `WAITING`, `ACTIVE`, `HOLD`, `DELETED` (단, `status` 필드 자체는 String으로 보관되고, `isViewable()`은 문자열 `"ACTIVE"`와 직접 비교한다)

비즈니스 메서드:
- `isViewable()`: `status`가 `"ACTIVE"`인지 확인해 수강(시청) 가능 여부를 판단

### LearningHistory (domain/model/LearningHistory.java)
viewing BC가 직접 소유하는 시청 기록 애그리거트. 사용자별·챕터별 시청 진행 상태를 표현하는 핵심 도메인 모델.

주요 필드: `id`, `userId`, `lectureId`, `chapterId`, `watchedSeconds`(실제로 본 최대 위치, 뒤로 감아도 감소 안 함), `isCompleted`, `lastPositionSec`(이어보기용 마지막 재생 위치), `progressRate`, `version`(낙관적 락용), `lastWatchedAt`

비즈니스 메서드:
- `create(userId, lectureId, chapterId)`: 신규 시청 기록 생성 (정적 팩토리)
- `updateProgress(playbackSeconds, durationSec)`: 진척도 갱신 로직. `playbackSeconds`가 `watchedSeconds`보다 크고 그 차이가 30초 이하일 때만 "정상 진척"으로 보고 `watchedSeconds`/`lastWatchedAt`을 갱신한다. 뒤로 감기나 30초 초과 앞으로 당기기는 무시. `progressRate`는 항상 `watchedSeconds/durationSec*100`으로 재계산(최대 100). 반환값 `boolean`은 정상 진척 여부(스킵 차단 판단용)
- `complete(durationSec)`: `watchedSeconds >= durationSec * 0.9`이면 `isCompleted=true`, `progressRate=100`으로 챕터 완료 처리
- `saveLastPosition(lastPositionSec)`: 나가기 시점 이어보기 위치 저장. 완료된 챕터면 전달받은 위치 그대로 저장하고, 미완료면 `watchedSeconds`로 대체 저장(앞으로 당기거나 뒤로 간 경우 실제 시청 위치로 되돌림)
- `reconstitute(...)`: DB 조회 데이터로 도메인 객체 복원 (정적 팩토리)

### 도메인 이벤트 (domain/event)
- `ChapterCompletedEvent(userId, lectureId, chapterId, watchedSeconds, occurredAt)`: 챕터 시청 완료 시 발행 (실제 발행 코드 확인됨, `ViewingCommandService`)
- `CourseCompletedEvent(userId, lectureId, occurredAt)`: totalProgress=100 도달 시 발행하도록 주석에 설명되어 있으나, 실제 발행 코드는 발견되지 않음 (확인 필요)
- `ProgressSavedEvent(userId, lectureId, chapterId, watchedSeconds, date)`: saveProgress 호출마다 발행해 "잔디 누적"에 쓰인다고 주석에 설명되어 있으나, 실제 발행 코드는 발견되지 않음 (확인 필요)

### 도메인 예외 (domain/exception)
- `ViewingAccessDeniedException`: 수강 권한 없음 (403 매핑)
- `ViewingNotFoundException`: 리소스 없음 (404 매핑)

## 6. ERD 스키마 대조

`infrastructure/persistence/LearningHistoryJpaEntity.java`(테이블명 `learning_history`, `@Table(name = "learning_history")`)를 대응 엔티티로 확인했다. `created_at`/`updated_at`은 이 클래스가 상속하는 `BaseTimeEntity`(`global/infrastructure/persistence/BaseTimeEntity.java`)에 선언되어 있다.

### `learning_history` 테이블

| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | LearningHistoryJpaEntity.id (Long, `@Id @GeneratedValue(IDENTITY)`) | |
| user_id | BIGINT | LearningHistoryJpaEntity.userId (Long) | DB는 NULL 허용인데 JPA는 `nullable = false`로 선언 — 제약 불일치 (확인 필요) |
| lecture_id | BIGINT | LearningHistoryJpaEntity.lectureId (Long) | DB는 NULL 허용인데 JPA는 `nullable = false`로 선언 — 제약 불일치 (확인 필요) |
| chapter_id | BIGINT | LearningHistoryJpaEntity.chapterId (Long) | DB는 NULL 허용인데 JPA는 `nullable = false`로 선언 — 제약 불일치 (확인 필요) |
| watched_seconds | INT | LearningHistoryJpaEntity.watchedSeconds (int) | DB는 NULL 허용인데 JPA는 primitive int + `nullable = false` — 제약 불일치 (확인 필요) |
| last_position_sec | INT | LearningHistoryJpaEntity.lastPositionSec (int) | DB는 NULL 허용인데 JPA는 primitive int + `nullable = false` — 제약 불일치 (확인 필요) |
| progress_rate | INT | LearningHistoryJpaEntity.progressRate (int) | DB는 NULL 허용인데 JPA는 primitive int + `nullable = false` — 제약 불일치 (확인 필요) |
| is_completed | BOOLEAN | LearningHistoryJpaEntity.isCompleted (boolean) | DB는 NULL 허용인데 JPA는 primitive boolean + `nullable = false` — 제약 불일치 (확인 필요) |
| last_watched_at | DATE | LearningHistoryJpaEntity.lastWatchedAt (LocalDateTime) | 타입 불일치: DB는 DATE(날짜만), JPA는 LocalDateTime(날짜+시간) |
| version | BIGINT | LearningHistoryJpaEntity.version (Long, `@Version`) | DB는 NULL 허용인데 JPA는 `nullable = false`로 선언 — 제약 불일치 (확인 필요) |
| created_at | DATETIME | BaseTimeEntity.createdAt (LocalDateTime) | DB는 NULL 허용인데 JPA는 `nullable = false, updatable = false`로 선언 — 제약 불일치 (확인 필요) |
| updated_at | DATETIME | BaseTimeEntity.updatedAt (LocalDateTime) | DB는 NULL 허용인데 JPA는 `nullable = false`로 선언 — 제약 불일치 (확인 필요) |

### DB에 없는 JPA 필드
없음 — `LearningHistoryJpaEntity`의 모든 필드가 위 DB 컬럼 목록으로 커버된다.
