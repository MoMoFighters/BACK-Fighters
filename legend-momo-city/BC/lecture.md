# lecture BC

## 1. 개요

lecture 바운디드 컨텍스트는 강의(온라인 수업)와 강의 안에 들어가는 챕터(동영상 단위 강의 회차)를 등록하고 관리하는 책임을 담당한다. 강사는 강의와 챕터를 등록하고 동영상을 업로드하며, 관리자는 강의를 승인(공개)하거나 거절할 수 있다. 학생과 비회원은 공개된 강의 목록과 상세 정보를 조회할 수 있다.

## 2. 패키지 구조

### domain
- `domain/model` — `LectureAggregate`(강의 애그리게잇), `LectureChapter`(챕터), `LectureCategory`(카테고리 enum), `LectureStatus`(강의 상태 enum), `VideoStatus`(동영상 처리 상태 enum), `LecturePage`(페이징 결과를 담는 record)
- `domain/repository` — `LectureRepository`, `ChapterRepository` (도메인이 필요로 하는 저장소 인터페이스)
- `domain/event` — `LectureCreatedEvent`(강의 등록 완료 이벤트), `LectureStatusChangedEvent`(관리자 승인/거절 이벤트)
- `domain/exception` — `LectureNotFoundException`, `ChapterNotFoundException`, `ChapterLimitExceededException`, `ChapterVideoAlreadyExistsException`, `DuplicateChapterOrderException`

### application
- `application/command` — `LectureCommand` (내부에 `AdminChangeLectureStatusCommand`, `ChangeLectureStatusCommand`, `CreateChapterCommand`, `CreateLectureCommand`, `RegisterChapterVideoCommand` record 포함)
- `application/query` — `LectureQuery` (내부에 `GetAdminLectureDetailQuery`, `GetAdminLecturesQuery`, `GetLecturesQuery`, `GetStudentLectureDetailQuery`, `GetTeacherLectureDetailQuery`, `GetTeacherLecturesQuery` record 포함)
- `application/port` — `LectureEnrollmentQueryPort`(수강 신청 정보 조회), `LectureReviewQueryPort`(리뷰 통계 조회), `TeacherAccountPort`(인증된 강사 ID 조회)
- `application/service` — `LectureCommandService`(강의/챕터 등록·상태변경 처리), `LectureQueryService`(강의 조회 처리), `LectureS3UrlResolver`(S3 key를 전체 URL로 변환)
- `application/usecase` — `LectureCommandUseCases`(내부에 `LectureCommandUseCase`, `ChapterCommandUseCase`, `AdminLectureCommandUseCase` 인터페이스), `LectureQueryUseCases`(내부에 `LectureQueryUseCase`, `AdminLectureQueryUseCase` 인터페이스)

### infrastructure
- `infrastructure/adapter` — `AuthTeacherAccountAdapter`(TeacherAccountPort 구현, auth BC 연동), `ChapterParentAdapter`(report BC의 `ChapterParentPort` 구현), `ChapterRepositoryAdapter`(ChapterRepository 구현), `LectureStatsAdapter`(admin BC의 `LectureStatsPort` 구현, 대시보드 통계 제공)
- `infrastructure/persistence` — `LectureJpaEntity`, `ChapterJpaEntity`(JPA 엔티티), `LectureRepositoryAdapter`(LectureRepository 구현), `SpringDataLectureRepository`, `SpringDataChapterRepository`(Spring Data JPA 인터페이스)

### presentation
- `presentation/api` — `LectureController`(REST 컨트롤러), `LectureExceptionHandler`(lecture 패키지 전용 예외 처리)
- `presentation/api/request` — `LectureRequest` (내부에 `AdminChangeLectureStatusRequest`, `ChangeLectureStatusRequest`, `CreateChapterRequest`, `CreateLectureRequest`, `RegisterChapterVideoRequest` record 포함)
- `presentation/api/response` — `AdminLectureResponse`, `LectureResponse`, `StudentLectureResponse`, `TeacherLectureResponse` (역할별 응답 DTO 모음 클래스)

## 3. 진행 상태

### 구현되어 있는 기능
- 강의 등록(썸네일 S3 업로드 포함), 강의 상태 변경(강사가 WAITING으로 검수 요청, 관리자가 ACTIVE/HOLD로 승인·거절)
- 챕터 등록(최대 10개 제한, 순서 중복 검증, 썸네일 S3 업로드), 챕터 동영상 등록(500MB 크기 제한, 중복 등록 방지)
- 학생/강사/관리자 역할별 강의 목록·상세 조회 (같은 URL을 Authentication의 ROLE로 분기)
- 강의 등록 완료 이벤트(`LectureCreatedEvent`), 관리자 상태 변경 이벤트(`LectureStatusChangedEvent`) 발행
- 관리자 대시보드용 강의 통계 제공(`LectureStatsAdapter`가 admin BC의 `LectureStatsPort` 구현)
- report BC가 챕터의 상위 강의 ID를 조회할 수 있게 해주는 `ChapterParentAdapter`

### 비어있거나 미완성으로 보이는 부분
- `VideoStatus` enum(UPLOADING, ENCODING, READY, FAILED)이 domain/model에 정의되어 있지만, `ChapterJpaEntity`와 `LectureChapter`에는 이 상태를 저장하는 필드가 없다. `ChapterJpaEntity`에서 `VideoStatus`를 import하지만 실제 컬럼 매핑에는 사용되지 않는다(확인 필요 — 동영상 인코딩 상태 관리 기능이 아직 구현되지 않은 것으로 보임).
- `LectureResponse` 안의 `LectureListItemResponse`, `LecturePageResponse` record는 정의되어 있으나, 코드베이스 내 다른 곳에서 사용되는지는 이번 조사 범위에서 확인되지 않음(확인 필요).
- `AdminLectureListItemResponse`, `TeacherLectureListItemResponse` 주석에 "averageRating, reviewCount는 추후 review 패키지와 port 연결 후 실제 값으로 교체한다"는 문구가 있으나, 실제 서비스 코드(`LectureQueryService`)에서는 이미 `LectureReviewQueryPort`를 통해 실제 값을 채우고 있음(주석이 코드보다 오래된 상태로 보임).

### TODO/FIXME 주석
코드 내에서 TODO/FIXME 주석은 발견되지 않았다.

## 4. API 목록

컨트롤러 클래스 레벨 매핑: `@RequestMapping("/api/v1/lectures")` (`LectureController`)

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| POST | /api/v1/lectures | LectureController.createLecture | 강사가 강의를 등록한다. 썸네일 파일을 포함하므로 multipart/form-data로 요청한다. (Javadoc 주석 기반) |
| POST | /api/v1/lectures/{lectureId}/chapters | LectureController.createChapter | 강사가 본인 강의에 챕터를 등록한다. 프론트 통합 등록 흐름에 맞춰 multipart/form-data로 요청한다. (Javadoc 주석 기반) |
| PATCH | /api/v1/lectures/{lectureId}/chapters/{chapterId}/video | LectureController.registerChapterVideo | 강사가 본인 강의의 챕터에 동영상을 등록한다. 영상 파일을 포함하므로 multipart/form-data로 요청한다. (Javadoc 주석 기반) |
| GET | /api/v1/lectures | LectureController.getLectures | 로그인 사용자의 권한(ROLE_ADMIN/ROLE_TEACHER/그 외)에 따라 학생, 강사, 관리자 기준 강의 목록을 조회한다. (Javadoc 주석 기반) |
| GET | /api/v1/lectures/{lectureId} | LectureController.getLectureDetail | 로그인 사용자의 권한에 따라 학생, 강사, 관리자 기준 강의 상세 정보를 조회한다. (Javadoc 주석 기반) |
| PATCH | /api/v1/lectures/{lectureId}/status | LectureController.changeLectureStatus | 강사는 본인 강의를 검수 요청(WAITING) 상태로 변경하고, 관리자는 강의를 승인(ACTIVE) 또는 거절(HOLD) 상태로 변경한다. (Javadoc 주석 기반) |

권한 제약(`@PreAuthorize`): createLecture, createChapter, registerChapterVideo는 `ROLE_TEACHER`만 가능. changeLectureStatus는 `ROLE_TEACHER` 또는 `ROLE_ADMIN`만 가능. getLectures, getLectureDetail은 별도 `@PreAuthorize` 없이 컨트롤러 내부에서 Authentication의 역할을 확인해 응답을 분기한다.

## 5. 도메인 모델

### LectureAggregate (강의)
주요 필드: `id`, `teacherId`, `title`, `description`, `thumbnailUrl`, `category`(LectureCategory), `status`(LectureStatus), `completedUserCount`, `createdAt`, `updatedAt`

비즈니스 메서드:
- `create(teacherId, title, description, thumbnailUrl, category)` — 신규 강의 생성. 상태는 항상 WAITING, 수강 완료 인원은 0으로 시작. teacherId/title/description/category 필수값 검증.
- `restore(...)` — DB에서 조회한 값으로 도메인 모델 복원.
- `update(title, description, thumbnailUrl, category)` — id/teacherId/status/createdAt은 유지하고 나머지 필드만 교체.
- `changeStatus(newStatus)` — 상태만 변경한 새 인스턴스 반환.
- `isOwnedBy(teacherId)` — 요청한 teacherId가 이 강의의 소유자인지 확인(수정/삭제 권한 검증용).

### LectureChapter (챕터)
주요 필드: `id`, `lectureId`, `title`, `orderNo`, `videoUrl`, `videoSizeBytes`, `durationSec`, `originalFilename`, `chapterThumbnailUrl`, `createdAt`, `updatedAt`

비즈니스 메서드:
- `create(lectureId, title, orderNo, chapterThumbnailUrl)` — 동영상 없이 챕터 생성(동영상은 별도 API에서 등록).
- `createWithoutThumbnail(lectureId, title, orderNo)` — 썸네일 URL 없이 챕터 기본 정보만 생성(썸네일은 chapterId 생성 후 S3 업로드로 채움).
- `restore(...)` — DB 조회 값으로 도메인 모델 복원.
- `registerVideo(videoUrl, videoSizeBytes, durationSec, originalFilename)` — 기존 챕터 정보를 유지하며 동영상 관련 값만 채운 새 인스턴스 반환. videoUrl/videoSizeBytes(1 이상)/durationSec(1 이상)/originalFilename 검증.
- `hasVideo()` — videoUrl이 존재하는지 확인(중복 등록 방지용).
- `belongsTo(lectureId)` — 이 챕터가 특정 강의에 속하는지 확인(다른 강의 챕터에 영상 등록하는 것을 방지).
- `changedChapterThumbnailUrl(chapterThumbnailUrl)` — 챕터 썸네일 URL만 교체한 새 인스턴스 반환. 필수값 검증 포함.
- 검증 규칙: lectureId 필수, title 필수, orderNo는 1 이상.

### LecturePage (record)
필드: `content`(List<LectureAggregate>), `totalElements`(long), `totalPages`(int). Spring Data의 Page를 application 계층 밖으로 노출하지 않기 위한 도메인 전용 페이징 모델.

### LectureCategory (enum)
값: `FITNESS`, `STUDY`, `COOK`, `BEAUTY`, `ART`

### LectureStatus (enum)
값: `WAITING`(강사가 검수 요청한 상태), `ACTIVE`(관리자 승인, 공개 상태), `HOLD`(관리자 거절), `DELETED`
비고: 강의는 신청 즉시 ACTIVE가 아니라 WAITING 상태로 시작한다(주석 확인).

### VideoStatus (enum)
값: `UPLOADING`(챕터 생성 직후/업로드 전 기본 상태), `ENCODING`(인코딩 중), `READY`(재생 가능), `FAILED`(업로드/인코딩 실패)
비고: enum은 정의되어 있으나 현재 `LectureChapter`/`ChapterJpaEntity`에 이 상태를 저장하는 필드가 없다(3번 항목 참고, 확인 필요).

## 6. ERD 스키마 대조

### `lecture` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | id (Long) | |
| teacher_id | BIGINT | teacherId (Long) | |
| title | VARCHAR(200) | title (String, length=200) | |
| description | TEXT | description (String, columnDefinition="TEXT") | |
| thumbnail_url | VARCHAR(500) | thumbnailUrl (String, length=500) | |
| category | ENUM('FITNESS','STUDY','COOK','BEAUTY','ART') | category (LectureCategory, EnumType.STRING) | enum 값 5개 일치 확인 |
| status | ENUM('WAITING','ACTIVE','HOLD','DELETED') | status (LectureStatus, EnumType.STRING) | enum 값 4개 일치 확인 |
| completed_user_count | INT | completedUserCount (int) | |
| created_at | DATETIME | createdAt (BaseTimeEntity 상속) | |
| updated_at | DATETIME | updatedAt (BaseTimeEntity 상속) | |
| deleted_at | DATETIME | JPA에 없음 - 확인 필요 | `LectureJpaEntity`는 `BaseTimeEntity`만 상속하며, 별도 soft delete 필드가 없다. `LectureStatus`에 `DELETED` 값이 있어 상태값으로 삭제를 표현하는 것으로 보이나, DB에 `deleted_at` 컬럼이 별도로 존재하는 이유는 확인 필요. |

DB에 없는 JPA 필드: 없음.

### `chapter` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | id (Long) | |
| lecture_id | BIGINT | lectureId (Long) | |
| title | VARCHAR(200) | title (String, length=200) | |
| order_no | INT | orderNo (int) | |
| video_url | VARCHAR(500) | videoUrl (String, length=500) | |
| video_size_bytes | BIGINT | videoSizeBytes (Long) | |
| duration_sec | INT | durationSec (Integer) | |
| original_filename | VARCHAR(255) | originalFilename (String, length=255) | |
| thumbnail_url | VARCHAR(500) | chapterThumbnailUrl (String, length=500, `@Column(name="thumbnail_url")`) | 필드명은 chapterThumbnailUrl이지만 `@Column(name)`으로 실제 컬럼명은 thumbnail_url과 일치 |
| created_at | DATETIME | createdAt (BaseTimeEntity 상속) | |
| updated_at | DATETIME | updatedAt (BaseTimeEntity 상속) | |
| deleted_at | DATETIME | JPA에 없음 - 확인 필요 | lecture 테이블과 동일하게 soft delete 필드가 매핑되어 있지 않음. |

DB에 없는 JPA 필드: 없음.

비고: `chapter` 테이블에는 동영상 인코딩 상태를 저장하는 컬럼이 없다. 이는 3번 항목·`VideoStatus` enum 설명에서 확인한 "`VideoStatus`가 domain에 정의되어 있으나 `ChapterJpaEntity`에 매핑되지 않는다"는 사실과 일치한다. 즉 DB 스키마 자체에 저장 공간이 없으므로, 코드에서 `VideoStatus`를 컬럼으로 매핑하지 않은 것이 스키마 설계와 일관된 상태로 보인다(동영상 인코딩 상태 관리 기능 자체가 아직 구현되지 않은 것으로 판단됨, 확인 필요).
