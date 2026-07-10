# user BC

## 1. 개요

user 바운디드 컨텍스트는 `user` 테이블을 실제로 소유하고 다루는 영역으로, 마이페이지(내 정보 조회/수정/알림설정/회원탈퇴), 강사 신청·승인·반려·포기, 관리자 회원관리(목록/상세/신고 처리), 신고 누적에 따른 정지 처리, 강사 증빙자료의 구글 드라이브 백업, 만료 정지 해제·하드 딜리트 스케줄러까지를 담당한다. `BC/member.md`에서 확인한 것처럼 `member` BC는 같은 `user` 테이블을 대상으로 `Member` 도메인 모델과 `MemberAdminController`(`PATCH /api/v1/admin/users/{userId}/status`)를 갖고 있지만 도메인 행위(`approveAsTeacher`, `rejectAsTeacher`, `changeStatusByAdmin`)와 리포지토리 구현이 모두 `UnsupportedOperationException`만 던지는 미구현 골격 상태다. 반면 이 user BC는 동일한 역할(강사 승인/반려, 회원 상태 변경)을 실제로 동작하는 코드로 구현하고 있어, 코드상으로는 user BC가 `user` 테이블에 대한 실질적인 구현체이고 member BC는 별도로 진행되다 미완성으로 남은 병행 구현으로 보인다(확인 필요 — 두 BC가 왜 동시에 존재하는지에 대한 설명은 코드 주석에 없음).

## 2. 패키지 구조

| 계층 | 하위 패키지 | 대표 클래스 |
|---|---|---|
| domain | `domain.model` | `User`, `TeacherApplication`, `Role`, `Status`, `BuildingInfo`, `CheckStatusResult`, `ReportInfo`, `UpdateUserInfoData` |
| domain | `domain.repository` | `UserRepository` (인터페이스) |
| domain | `domain.event` | `DriveUploadEvent`, `ReportRedisEvent`, `TeacherApplicationEvent` |
| domain | `domain.exception` | `AlreadySuspendedException`, `InvalidFileExtensionException`, `InvalidPasswordException`, `InvalidReasonException`, `MissingProofException`, `NicknameDuplicateException`, `SamePasswordException`, `UserNotFoundException`, `UserExceptionHandler`(`@RestControllerAdvice`) |
| application | `application.command` | `ApproveTeacherCommand`, `NicknameRegisterCommand`, `RejectTeacherCommand`, `TeacherApplyCommand`, `UpdateUserInfoCommand` |
| application | `application.usecase` | `UserCommandUsecase`, `UserQueryUsecase` (인터페이스, 중첩 record 다수) |
| application | `application.service` | `UserCommandService`, `UserQueryService` |
| application | `application.policy` | `UserPolicy` (닉네임/비밀번호/증빙자료 정책, 정지·활성 여부 판단) |
| application | `application.port` | `GetItemUrlPort`, `GetUserBuildingsPort`, `GoogleDriveUploadPort`, `ReportRedisPort`, `UserEmailSendPort`, `UserReportListPort` |
| application | `application.event` | `DriveUploadEventListener`, `ReportRedisEventListener`, `TeacherApprovedEventListener` |
| infrastructure | `infrastructure.persistence` | `UserJpaEntity`, `SpringDataUserRepository`, `UserRepositoryAdapter`(`UserRepository` 구현), `UserNameProjection` |
| infrastructure | `infrastructure.point` | `ChangePointAdapter`(`order` BC의 `com.wanted.momocity.global.application.point.PointChange` 포트 구현), `CheckPointAdapter`(`order` BC의 `CheckPointPort` 구현), `GetUserPointAdapter`(`store` BC의 `GetUserPointPort` 구현) |
| infrastructure | `infrastructure.redis` | `ReportRedisAdapter`(`ReportRedisPort` 구현, TTL 24시간) |
| infrastructure | `infrastructure.drive` | `GoogleDriveUploadAdapter`(`GoogleDriveUploadPort` 구현, Google Drive API 연동) |
| infrastructure | `infrastructure.email` | `UserEmailSendAdapter`(`UserEmailSendPort` 구현, 강사 승인/반려 결과 메일 발송) |
| infrastructure | `infrastructure.adminadapter` | `MemberStatsAdapter`(`admin` BC의 `MemberStatsPort` 구현 — 전체/활성/특정일 이전 회원 수, 월별 가입자 수, 승인대기 강사 수), `PendingTeacherAdapter`(`admin` BC의 `PendingTeacherPort` 구현 — 대기 강사 목록 limit개), `UserNameAdapter`(`admin` BC의 `UserNamePort` 구현 — userId→이름/역할 매핑) |
| infrastructure | `infrastructure.reportadapter` | `ReportUserNameAdapter`(`report` BC의 `ReportUserNamePort` 구현 — userId→이름 매핑) |
| infrastructure | `infrastructure.scheduler` | `BanOverScheduler`(매일 00:00, 정지 만료 유저 ACTIVE 복귀), `DriveRetryScheduler`(3분마다, 드라이브 업로드 재시도 최대 3회), `UserHardDeleteScheduler`(매월 1일 00:00, 탈퇴 3개월 경과 유저 하드 딜리트) |
| presentation | `presentation.api` | `UserController`, `TeacherApplicationController` |
| presentation | `presentation.api.request` | `NicknameRequest`, `TeacherActionRequest`, `TeacherApplyRequest`, `TeacherApproveRequest`, `TeacherRejectRequest`, `UpdateUserInfoRequest` |
| presentation | `presentation.api.response` | `AdminUserDetailResponse`, `AdminUserListResponse`(nested `Default`/`Deleted`), `AlarmSetResponse`, `NicknameRegisterResponse`, `TeacherApplicationDetailResponse`, `TeacherApplicationListResponse`(nested `Item`), `TeacherResponseCode`, `TeacherResponseMessage`, `UserInfoDetailResponse`, `UserInfoUpdateResponse`, `UserResponseCode`, `UserResponseMessage` |

`infrastructure.adminadapter`와 `infrastructure.reportadapter`는 다른 BC(admin, report)가 정의한 포트를 user BC가 구현해서 꽂아주는 지점이다. admin/report 쪽 애플리케이션 계층은 인터페이스(`MemberStatsPort`, `PendingTeacherPort`, `UserNamePort`, `ReportUserNamePort`)만 알고, 실제 `user` 테이블 조회는 이 어댑터들이 `SpringDataUserRepository`를 통해 수행한다(포트-어댑터 패턴으로 BC 경계를 지킴). `infrastructure.point` 역시 같은 방식으로 `order`/`store` BC의 포인트 관련 포트를 구현한다.

`presentation/api/request/TeacherActionRequest.java`는 존재하지만(주석상 승인/반려 통합 요청 DTO로 보임) 실제 컨트롤러(`TeacherApplicationController`)는 이를 사용하지 않고 `TeacherApproveRequest`/`TeacherRejectRequest`로 분리해 사용하고 있다 — 즉 `TeacherActionRequest`는 사용되지 않는 잔존 클래스로 보인다(확인 필요).

## 3. 진행 상태

**구현되어 있는 기능**
- 마이페이지 조회(`/detail`), 닉네임 등록/중복확인, 정보 수정(닉네임/비밀번호/프로필이미지), 알림(do_not_disturb) 토글, 회원탈퇴(소프트 딜리트)
- 강사 신청(증빙자료 S3 업로드 + Google Drive 비동기 백업), 강사 신청 목록/상세 조회(presigned URL 발급), 강사 일괄 승인(카테고리별 벌크 업데이트 + 이메일 발송 이벤트), 강사 반려(반려 사유 10자 이상 검증), 강사 포기
- 관리자 회원관리: 역할/상태별 목록 조회(캐시 적용, `@Cacheable("adminUserList")`, 전체조회일 때만), 회원 1명 상세 조회(신고내역 포함), 신고 횟수 +/- 처리(1회=1주 정지, 2회=1개월 정지, 3회 이상=영구정지 BLACK, Redis에 24시간 TTL로 신고시각 저장해 복구 가능 여부 판단)
- 비밀번호 변경 시 기존 액세스 토큰 블랙리스트 처리 + 리프레시 토큰 삭제(로그아웃과 동일 처리)
- 스케줄러 3종: 정지 만료 자동 해제, 드라이브 업로드 실패 재시도(최대 3회), 탈퇴 3개월 경과 유저 하드 딜리트(FK cascade로 연관 데이터 자동 삭제, 주석에 명시)
- 다른 BC(admin, report, order, store)에 대한 포트 구현체 다수(회원 통계, 대기 강사 목록, 이름 조회, 포인트 증감/조회)

**비어있거나 미완성으로 보이는 부분**
- `domain/model/CheckStatusResult.java`, `ReportInfo.java`, `BuildingInfo.java`, `UpdateUserInfoData.java`는 필드만 있는 단순 record로 비즈니스 메서드가 없음
- `presentation/api/request/TeacherActionRequest.java`는 상세 주석(승인/반려 통합 처리 의도)이 있지만 실제 컨트롤러 어디에서도 사용되지 않는 것으로 보임(대신 `TeacherApproveRequest`/`TeacherRejectRequest`로 분리 사용) — 사용되지 않는 잔존 코드인지 확인 필요
- `UserQueryUsecase.getAdminUserList()`의 반환 타입이 `List<?>`(제네릭 와일드카드)로, `AdminUserListResponse.Default`/`Deleted` 중 하나가 섞여 담기는 방식 — 타입 안전성이 약한 설계로 보임(설계상 의도인지 확인 필요)
- `member` BC 쪽에 있는 `MemberAdminController`(`PATCH /api/v1/admin/users/{userId}/status`, 임의 상태 변경 API)는 미구현 상태인데, user BC에는 이를 대체하는 "임의 상태 변경" 범용 API가 보이지 않음(신고 횟수 +/- 를 통한 상태 변경만 존재) — 두 BC 간 책임 이관 여부 확인 필요

**TODO/FIXME 주석**
- 이 경로(`user/` 전체) 내에서 `TODO`/`FIXME` 문자열을 grep 했으나 발견되지 않음

## 4. API 목록

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| GET | /api/v1/user/detail | UserController.getUserDetail | 마이페이지에서 사용자에게 제시될 정보 조회 (Swagger `@Operation` 인용) |
| PATCH | /api/v1/user/register/nickname | UserController.registerNickname | 사용자의 닉네임 등록 (Swagger `@Operation` 인용) |
| PATCH | /api/v1/user/update | UserController.updateUserInfo | 사용자 정보 수정, 프로필 이미지(모듈4부터)·닉네임·비밀번호 변경 가능 (Swagger `@Operation` 인용) |
| POST | /api/v1/user/nickname/check | UserController.checkNickname | 닉네임 중복 확인 (Swagger `@Operation` 인용) |
| PATCH | /api/v1/user/settings/alarm | UserController.setAlarm | do_not_disturb 칼럼 활용해서 기존에 true면 false로, false면 true로 변경 (Swagger `@Operation` 인용) |
| GET | /api/v1/user/list | UserController.getUserList | 관리자 회원 목록 조회, 파라미터 없으면 탈퇴회원 제외 전체 조회, role/status로 필터링 (Swagger `@Operation` 인용, ADMIN 권한) |
| GET | /api/v1/user/list/detail/{userId} | UserController.getUserDetail | 관리자 회원 1명의 정보 조회 (Swagger `@Operation` 인용, ADMIN 권한) |
| PATCH | /api/v1/user/delete | UserController.deleteUser | 회원탈퇴, status/nickname만 변경 후 3개월 뒤 하드 딜리트 (Swagger `@Operation` 인용) |
| PATCH | /api/v1/user/plus/report-count/{userId} | UserController.plusReportCount | 관리자의 신고 처리, 신고 누적 횟수에 따라 status/suspensionCount/suspendedUntil 변경 (Swagger `@Operation` 인용, ADMIN 권한) |
| PATCH | /api/v1/user/minus/report-count/{userId} | UserController.minusReportCount | 관리자가 실수로 +누른 경우 사용자 복구 (Swagger `@Operation` 인용, ADMIN 권한) |
| POST | /api/v1/teacherApply | TeacherApplicationController.teacherApply | 강사 증빙자료를 제출하고 강사 신청을 진행 (Swagger `@Operation` 인용, STUDENT 권한) |
| PATCH | /api/v1/application-giveup | TeacherApplicationController.teacherGiveup | 반려된 강사 신청자(REJECTED+STUDENT)가 강사 재신청을 포기하고 ACTIVE+STUDENT로 복귀 (Swagger `@Operation` description 인용) |
| GET | /api/v1/teacher-applications | TeacherApplicationController.getApplicationList | 강사 신청자 목록 조회 (MS-3) (Swagger `@Operation` 인용, ADMIN 권한) |
| GET | /api/v1/teacher-application-detail/{userId} | TeacherApplicationController.getApplicationDetail | 강사 신청자 상세 조회 (MS-4) (Swagger `@Operation` 인용, ADMIN 권한) |
| PATCH | /api/v1/application-approve | TeacherApplicationController.teacherApprove | 강사 승인 처리, 다건(userId 리스트) 일괄 승인 (Swagger `@Operation` 인용, ADMIN 권한) |
| PATCH | /api/v1/application-reject/{userId} | TeacherApplicationController.teacherReject | 강사 반려, 반려 사유 최소 10자 (Swagger `@Operation` 인용, ADMIN 권한) |

참고: `UserController`는 클래스 레벨 `@RequestMapping("/api/v1/user")`, `TeacherApplicationController`는 클래스 레벨 `@RequestMapping("/api/v1")`이며 컨트롤러 주석에 "MS-3/MS-4/MS-5"로 표기되어 있으나 실제 경로는 `/api/v1/admin/...`이 아니라 `/api/v1/teacher-applications` 등으로 admin prefix가 빠진 형태다(코드에 있는 그대로 기재, admin prefix 제거 작업 반영으로 보임 — 확인 필요).

## 5. 도메인 모델

### User (도메인 엔티티, 클래스)
필드: `id`, `email`, `password`, `name`, `nickname`, `profileImageUrl`, `role`(`Role`), `status`(`Status`), `category`(`Category`, `global` BC 소유), `proof`, `point`, `doNotDisturb`, `suspensionCount`(신고 받은 횟수), `suspendedUntil`(임시 정지 만료 시점), `createdAt`, `updatedAt`, `deletedAt`, `isTempPwd`(임시 비밀번호 여부). 모든 필드가 `final`이며 생성자는 `@Builder`만 노출.

정적 팩토리 메서드(단순 getter/setter 아닌 비즈니스 성격의 생성 로직):
- `restore(...)` — 마이페이지에서 필요한 일부 필드만으로 빌더를 채워 복원(전체 컬럼을 다 가져오면 null이 너무 많아서라는 주석 있음)
- `restoreForAdmin(...)` — 관리자 대시보드용으로 다른 필드 조합(`id`, `deletedAt`, `suspendedUntil`, `proof` 등)으로 복원

### TeacherApplication (record, 도메인 값객체)
필드(9개+2개): `userId`, `nickname`, `name`, `email`, `profileImageUrl`, `category`, `proof`, `status`, `role`, `suspensionCount`, `suspendedUntil`, `createdAt`. 컴팩트 생성자에서 `userId == null`이면 `DomainRuleViolationException`을 던져 불변식을 보장한다.
비즈니스 메서드:
- `fileType()` — `proof` URL에서 쿼리스트링을 제거한 뒤 확장자를 추출해 소문자로 반환
- `withPresignedUrl(String url)` — `proof` 필드만 presigned URL로 교체한 새 인스턴스를 반환(불변 객체이므로 복제 후 교체하는 패턴)

### Role (enum)
값: `STUDENT`, `TEACHER`, `ADMIN`.

### Status (enum)
값: `ACTIVE`, `PENDING`, `REJECTED`, `BANNED`, `BLACK`, `DELETED`. (member BC의 `MemberStatus`와 값 목록이 동일함)

### BuildingInfo / CheckStatusResult / ReportInfo / UpdateUserInfoData (record, 값객체)
- `BuildingInfo(category, position, level, buildingUrl)` — 사용자가 보유한 건물 정보. 비즈니스 메서드 없음
- `CheckStatusResult(status, suspendedUntil)` — 신고 횟수에 따라 계산된 상태/정지기간 결과. 비즈니스 메서드 없음
- `ReportInfo(targetType, content, createdAt, isResolved)` — `targetType`은 `report` BC의 `ReportTargetType`을 참조. 비즈니스 메서드 없음
- `UpdateUserInfoData(userId, profileImageUrl, nickname, password)` — 정보수정 시 리포지토리에 전달하는 데이터 묶음. 비즈니스 메서드 없음

## 6. ERD 스키마 대조

대조 대상: `src/main/java/com/wanted/momocity/user/infrastructure/persistence/UserJpaEntity.java`(user 테이블), `src/main/java/com/wanted/momocity/auth/infrastructure/persistence/UserOauthJpaEntity.java`(user_oauth 테이블).

### `user` 테이블

| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT NOT NULL | id (Long) | |
| email | VARCHAR(100) NULL | email (String) | JPA에 `unique = true` 제약이 추가로 걸려 있음(DB 스키마에는 UNIQUE 명시 없음, 확인 필요) |
| password | VARCHAR(255) NULL | password (String) | DB는 NULL 허용인데 JPA는 `@Column(nullable = false)` — 제약 불일치 |
| name | VARCHAR(50) NULL | name (String) | DB는 NULL 허용인데 JPA는 `@Column(nullable = false)` — 제약 불일치 |
| nickname | VARCHAR(30) NULL | nickname (String) | |
| profile_image_url | VARCHAR(500) NULL | profileImageUrl (String) | |
| role | ENUM('STUDENT','TEACHER','ADMIN') NULL | role (Role, `@Enumerated(STRING)`) | DB는 NULL 허용인데 JPA는 `@Column(nullable = false)` — 제약 불일치. enum 값 자체는 `STUDENT,TEACHER,ADMIN`으로 코드와 일치 |
| status | ENUM('ACTIVE','PENDING','REJECTED','BANNED','BLACK','DELETED') NULL | status (Status, `@Enumerated(STRING)`) | DB는 NULL 허용인데 JPA는 `@Column(nullable = false)` — 제약 불일치. enum 값은 6개 모두 코드와 일치 |
| category | ENUM('FITNESS','STUDY','COOK','BEAUTY','ART') NULL | category (Category, `@Enumerated(STRING)`) | enum 값 5개 모두 코드(`global.domain.model.Category`)와 일치 |
| proof | VARCHAR(500) NULL | proof (String) | |
| point | INT NULL | point (Long) | DB는 INT, JPA 필드 타입은 Long(BIGINT 매핑) — 타입 불일치(확인 필요) |
| do_not_disturb | BOOLEAN NULL | doNotDisturb (boolean, primitive) | DB는 NULL 허용이지만 JPA는 primitive boolean이라 NULL을 표현할 수 없음(항상 true/false) — 제약 불일치 |
| suspension_count | INT NULL | suspensionCount (Long) | DB는 INT, JPA 필드 타입은 Long(BIGINT 매핑) — 타입 불일치(확인 필요) |
| suspended_until | DATETIME NULL | suspendedUntil (LocalDateTime) | |
| created_at | DATETIME NULL | createdAt (LocalDateTime) | DB는 NULL 허용인데 JPA는 `@Column(nullable = false, updatable = false)` — 제약 불일치 |
| updated_at | DATETIME NULL | updatedAt (LocalDateTime) | |
| deleted_at | DATETIME NULL | deletedAt (LocalDateTime) | |
| is_tempPWD | BOOLEAN NULL | isTempPwd (boolean, primitive, `@Column(name = "is_tempPWD")`) | DB는 NULL 허용이지만 JPA는 primitive boolean이라 NULL을 표현할 수 없음 — 제약 불일치 |

DB에 없는 JPA 필드: 없음.

### `user_oauth` 테이블

실제 구현 위치는 **auth BC**다. `src/main/java/com/wanted/momocity/auth/infrastructure/persistence/UserOauthJpaEntity.java`가 `@Table(name = "user_oauth")`로 직접 매핑하며, user BC의 `infrastructure/persistence` 패키지에는 `user_oauth`를 다루는 엔티티가 없다(user BC 쪽엔 `UserJpaEntity`/`SpringDataUserRepository`/`UserRepositoryAdapter`/`UserNameProjection`만 존재).

| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT NOT NULL | id (Long) | |
| user_id | BIGINT NULL | user (`UserJpaEntity`, `@ManyToOne` + `@JoinColumn(name = "user_id")`) | DB는 NULL 허용인데 JPA는 `@JoinColumn(nullable = false)` — 제약 불일치. 참고로 이 `@ManyToOne`이 auth BC 엔티티에서 user BC의 `UserJpaEntity`를 직접 참조하는 구조라 BC 경계상 결합이 있음(확인 필요) |
| provider | ENUM('KAKAO','GOOGLE','NAVER') NULL | provider (`Provider`) | **`@Enumerated(EnumType.STRING)` 애노테이션이 없음** — JPA 기본값은 `ORDINAL`(정수) 저장이라, DB 컬럼이 ENUM 문자열 타입인 것과 맞지 않을 가능성이 높음(실제 배포 스키마와 다를 수도 있어 확인 필요). 또한 코드 enum 순서는 `NAVER, KAKAO, GOOGLE`(`auth.domain.model.Provider`)로 DB ENUM 정의 순서(`KAKAO,GOOGLE,NAVER`)와 다름 — ORDINAL 저장 시 값이 어긋날 위험. 값 집합 자체(`KAKAO/GOOGLE/NAVER`)는 코드와 일치. DB는 NULL 허용인데 JPA는 `@Column(nullable = false)` — 제약 불일치 |
| provider_id | VARCHAR(100) NULL | providerId (String) | DB는 NULL 허용인데 JPA는 `@Column(nullable = false)` — 제약 불일치 |
| created_at | DATETIME NULL | createdAt (LocalDateTime) | DB는 NULL 허용인데 JPA는 `@Column(nullable = false)` — 제약 불일치 |

DB에 없는 JPA 필드: 없음.
