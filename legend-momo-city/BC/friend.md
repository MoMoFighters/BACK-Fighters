# friend BC

## 1. 개요

friend 바운디드 컨텍스트는 사용자 간 친구 요청·수락·거절·차단·삭제 등 친구 관계를 관리하고, 친구끼리 서로의 "도시"에 방명록(짧은 메모)을 남기는 기능을 담당한다. 또한 수강신청이 완료되면 강사와 학생을 자동으로 친구 관계(FRIEND 상태)로 맺어주는 로직도 이 BC 안에 있다.

## 2. 패키지 구조

### domain
- `domain/model` — `Friend`(친구 관계 애그리거트), `FriendStatus`(SENT/FRIEND/BLOCK enum)
- `domain/repository` — `FriendRepository`(포트 인터페이스)
- `domain/event` — 도메인에서 발행하는 이벤트 레코드 7개
  - `RequestFriendPublishedEvent`, `CancelRequestFriendPublishedEvent`, `AcceptRequestFriendPublishedEvent`, `RejectRequestFriendPublishedEvent`, `DeleteFriendPublishedEvent`, `RegisterGuestBookPublishedEvent`, `TeacherStudentAutoFriendPublishedEvent`

### application
- `application/command` — 커맨드 레코드 8개(`RequestFriendCommand`, `CancelRequestFriendCommand`, `AcceptRequestFriendCommand`, `RejectRequestFriendCommand`, `BlockFriendCommand`, `UnblockFriendCommand`, `DeleteFriendCommand`, `RegisterGuestBookCommand`)
- `application/query` — 쿼리 레코드 7개(`GetFriendQuery`, `FindUserQuery`, `SentRequestQuery`, `ReceivedRequestQuery`, `BlockedFriendQuery`, `GetStudentFriendsQuery`, `GetGuestBooksQuery`)
- `application/usecase` — `FriendCommandUseCase`, `FriendQueryUseCase`(포트, 내부 View 레코드들을 함께 선언)
- `application/service` — `FriendCommandService`(커맨드 유스케이스 구현), `FriendQueryService`(쿼리 유스케이스 구현), `FriendHandlerService`(수강신청 완료 이벤트를 받아 강사-학생 자동 친구 생성)
- `application/policy` — `FriendEligibilityPolicy`(요청/수락/거절/차단/차단해제/삭제/방명록 작성 가능 여부를 검증하는 정책 클래스, 400/403/404/409 예외를 여기서 던짐)
- `application/metric` — `FriendMetrics`(Micrometer 기반 사용자 검색·친구목록 조회 시간(Timer), 방명록 포인트 연동 실패 카운터(Counter))

### infrastructure
- `infrastructure/persistence` — `FriendJpaEntity`, `GuestBookJpaEntity`(엔티티), `SpringDataFriendRepository`, `SpringDataGuestBookRepository`(스프링 데이터 JPA 리포지토리), `FriendSideEnrollmentRepository`, `FriendSideLectureRepository`, `FriendSideUserRepository`(다른 BC의 테이블을 friend BC 관점에서 조회하기 위한 전용 리포지토리)
- `infrastructure/catalog` — `CatalogFriendAdapter`(`FriendRepository` 포트의 구현체, 실제 DB 접근 담당)
- `infrastructure/event` — `FriendLifecycleEventHandler`(다른 BC인 enrollment의 `EnrollmentCompletedEvent`를 트랜잭션 커밋 후 비동기로 받아 `FriendHandlerService` 호출)

### presentation
- `presentation/api` — `FriendController`(REST 컨트롤러)
- `presentation/api/request` — `RegisterGuestBookRequest`
- `presentation/api/response` — 응답 레코드 13개(`FriendResponse`, `FindUserResponse`, `RequestFriendResponse`, `CancelRequestFriendResponse`, `AcceptRequestFriendResponse`, `RejectRequestFriendResponse`, `SentRequestResponse`, `ReceivedRequestResponse`, `BlockFriendResponse`, `BlockedFriendResponse`, `UnblockFriendResponse`, `DeleteFriendResponse`, `GetStudentFriendsResponse`, `GetGuestBooksResponse`, `RegisterGuestBookResponse`)

### 최상위 특이 패키지 (friend 바로 아래)
- `friend/enrollment` — `EnrollmentWithFMJpaEntity`. `@Entity(name = "FMEnrollment")`로 별도 엔티티명을 붙여 실제 enrollment BC의 `enrollment` 테이블을 friend 쪽 시점에서 읽기 위한 전용 매핑 클래스.
- `friend/lecture` — `LectureWithFMJpaEntity`. `@Entity(name = "FMLecture")`로 `lecture` 테이블을 friend 쪽에서 읽기 위한 매핑 클래스(강사 ID, 강의명만 보유).
- `friend/user` — `UserWithFMJpaEntity`. `@Entity(name = "FMUser")`로 `user` 테이블을 friend 쪽에서 읽기 위한 매핑 클래스. 생성자를 `PROTECTED`로 막아 friend BC에서 새 유저를 직접 만들 수 없게 방어함.
- `friend/fmexception` — `FMBusinessRuleViolationException`(400), `FMResourceAccessDeniedException`(403), `FMResourceConflictException`(409), `FMResourceNotFoundException`(404), `FMExceptionHandler`(`@RestControllerAdvice`, 위 4개 예외를 실제 HTTP 응답으로 변환)

관찰: enrollment/lecture/user 패키지 3개는 각각 `@Entity(name = "FM...")`로 원래 엔티티명과 다른 이름을 붙여, JPA 엔티티명 충돌 없이 다른 BC(수강, 강의, 사용자)의 테이블을 friend BC 관점에서만 읽기 위한 "타 BC 참조용 얕은 복제 엔티티" 모음으로 보인다. 즉 friend BC는 다른 BC의 도메인 모델을 직접 참조하지 않고, 자신만의 읽기 전용 JPA 매핑을 따로 두어 BC 간 결합을 낮추는 구조다. fmexception 패키지도 이름 앞에 FM(Friend Management로 추정, 확인 필요)을 붙여 다른 BC의 예외 클래스와 이름이 겹치지 않게 분리해둔 것으로 보인다.

## 3. 진행 상태

### 구현되어 있는 기능
- 친구 요청 보내기/철회하기
- 받은 요청 수락/거절
- 친구 목록 조회(강사인 친구의 수강 중인 강의명 포함)
- 사용자 닉네임 검색(친구 상태, 관리자/차단 유저 제외 처리 포함)
- 보낸 요청 목록 / 받은 요청 목록 조회
- 친구 차단 / 차단 해제 / 차단 목록 조회
- 친구 삭제
- 강사·비활성 유저 제외 친구 목록 조회(다대다 채팅, 친구 도시 놀러가기용)
- 방명록 작성(1일 1회 제한, 친구 상태에서만 가능, 작성 시 포인트 +10 지급)과 방명록 목록 조회
- 수강신청 완료 시 강사-학생 자동 친구 관계 생성(비동기 이벤트 기반)
- 친구 관련 이벤트 발행을 통한 알림(notification) 도메인과의 느슨한 결합
- 성능 메트릭 수집(Micrometer Timer/Counter)

### 비어있거나 미완성으로 보이는 부분
- `domain/model`에는 `Friend`, `FriendStatus` 두 클래스만 존재하고, 방명록(GuestBook)에 대응하는 순수 도메인 모델(예: `GuestBook` 도메인 클래스)은 없다. 방명록은 `infrastructure/persistence`의 `GuestBookJpaEntity`만 존재하며 도메인 계층에 대응 모델이 없다.
- `Friend` 도메인 애그리거트는 생성 로직(`createRequest`, `createTeacherStudentRelation`)만 있고, 상태 변경(수락/차단 등) 비즈니스 메서드는 도메인 모델이 아니라 `FriendJpaEntity`(인프라 계층)에 구현되어 있다(`changeStatus`, `swapDirectionAndBlock`). 즉 상태 전이 로직이 도메인 모델이 아니라 JPA 엔티티에 위치해 있다.
- `application/policy/FriendEligibilityPolicy.ensureDeletable` 메서드 내부에 주석 처리된 코드가 남아있다(아래 TODO/주석 인용 참고).
- `CatalogFriendAdapter`에서 사용하지 않는 `import javax.swing.text.html.Option;` 임포트가 남아 있다(미사용, 정리 필요로 보임. 확인 필요).

### 코드 내 주석 인용
- `FriendEligibilityPolicy.ensureDeletable` 내부:
```
//관계 행 자체가 없는지 확인(404)
//        if (relation.getStatus().isEmpty()) {
//            log.warn("[FriendEligibilityPolicy] 친구 삭제 검증 실패 - 관계 내역이 존재하지 않음");
//            throw new FMResourceNotFoundException("삭제할 친구 내역이 존재하지 않습니다.");
//        }
```
- `FriendHandlerService.createAndSaveTeacherFriendRelation` 내부:
```
//        UserWithFMJpaEntity teacher = lecture.getTeacherId();
```
(둘 다 명시적인 TODO/FIXME 키워드는 없고, 주석으로 비활성화된 코드만 남아 있는 형태)

## 4. API 목록

`FriendController`는 클래스 레벨 `@RequestMapping`이 없고, 각 메서드에 전체 경로가 직접 지정되어 있다.

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| GET | /api/v1/friends | FriendController.getFriends | 로그인한 사용자의 내 친구 목록을 불러온다. |
| GET | /api/v1/friends/find | FriendController.findUser | 닉네임(포함)을 통해 사용자를 검색한다. (차단 유저 제외) |
| POST | /api/v1/friends/request/{userId} | FriendController.sentFriend | 상대방에게 친구 요청을 보내고 알림을 생성한다. |
| DELETE | /api/v1/friends/request/{userId} | FriendController.cancelRequestFriend | 보낸 친구 요청을 철회한다. |
| GET | /api/v1/friends/sent | FriendController.sentList | 로그인한 사용자가 타인에게 보낸 친구 요청 중 SENT 상태인 목록을 조회한다. |
| PATCH | /api/v1/friends/received/{userId}/accept | FriendController.acceptRequestFriend | 받은 친구 요청을 수락하고 관계를 맺는다. |
| DELETE | /api/v1/friends/received/{userId}/reject | FriendController.rejectRequestFriend | 친구 요청을 거절하고 friend 테이블에서 행 삭제 |
| GET | /api/v1/friends/received | FriendController.receivedList | 로그인한 사용자가 toUserId이면서 SENT 상태인 목록을 조회한다. |
| PATCH | /api/v1/friends/block/{userId} | FriendController.blockFriend | 선택한 친구를 차단한다. |
| GET | /api/v1/friends/blocked | FriendController.blockedList | 로그인 유저가 차단한 목록을 조회한다. |
| PATCH | /api/v1/friends/unblock/{userId} | FriendController.unblockFriend | 친구 차단을 해제하여 다시 친구 상태로 만든다. |
| DELETE | /api/v1/friends/delete/{userId} | FriendController.deleteFriend | 친구 상태이면서 강사가 아닌 친구를 삭제한다. |
| GET | /api/v2/studentfriends | FriendController.getStudentFriends | 다대다 채팅 개설/초대, 친구 도시 놀러가기 시 사용한다(강사, 비활성 유저 제외) |
| GET | /api/v2/friends/guest | FriendController.getGuestBooks | 로그인 유저의 방명록 목록을 조회하고 읽는다. |
| POST | /api/v2/friends/guests/register/{ownerId} | FriendController.registerGuestBook | userId는 도시 주인 아이디이며 친구 상태에서만 작성 가능하고 1일 1회 제한된다. |

## 5. 도메인 모델

### Friend (domain/model/Friend.java)
`@AllArgsConstructor`, `@Getter`가 붙은 불변(필드가 모두 `final`) 클래스.

필드
- `id` (Long)
- `fromUserId` (Long)
- `toUserId` (Long)
- `status` (FriendStatus)

비즈니스 메서드(정적 팩토리, 단순 getter 아님)
- `createRequest(Long fromUserId, Long toUserId)` — 새 친구 요청을 생성하며 상태를 `SENT`로 초기화한다.
- `createTeacherStudentRelation(Long studentId, Long teacherId)` — 수강신청 자동 친구 맺기용으로, 상태를 곧바로 `FRIEND`로 초기화한다.

### FriendStatus (domain/model/FriendStatus.java)
enum 값: `SENT`, `FRIEND`, `BLOCK`

### 참고: 상태 전이는 도메인이 아니라 인프라 엔티티(FriendJpaEntity)에 구현됨
- `changeStatus(String newStatus)` — 상태 문자열을 변경하고 `updatedAt`을 갱신한다.
- `swapDirectionAndBlock()` — `fromUserId`/`toUserId`를 서로 바꾸고 상태를 `BLOCK`으로 변경한다(차단 시 로그인 유저를 항상 from으로 맞추기 위한 로직).

### GuestBookJpaEntity (infrastructure/persistence, 도메인 모델 대응 클래스 없음)
필드: `id`, `writerId`(작성자), `ownerId`(도시 주인), `content`, `createdAt`
비즈니스 메서드: `create(UserWithFMJpaEntity loginUser, UserWithFMJpaEntity ownerUser, String content, LocalDateTime now)` — 방명록 신규 생성 정적 팩토리.

## 6. ERD 스키마 대조

### `friend` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT (PK) | id (Long) | |
| from_user_id | BIGINT | fromUserId (`@ManyToOne` UserWithFMJpaEntity, `@JoinColumn(name = "from_user_id")`) | |
| to_user_id | BIGINT | toUserId (`@ManyToOne` UserWithFMJpaEntity, `@JoinColumn(name = "to_user_id")`) | |
| status | ENUM('SENT','FRIEND','BLOCK') | status (String) | DB는 ENUM인데 JPA 필드는 `@Enumerated` 없이 순수 String으로 매핑되어 있음. `changeStatus`/`createRequest`/`createTeacherStudentRelation`/`swapDirectionAndBlock` 모두 `"SENT"`, `"BLOCK"` 같은 문자열 리터럴을 직접 대입하며, 도메인의 `FriendStatus` enum(SENT/FRIEND/BLOCK)을 사용하지 않음. 값 집합 자체는 `FriendStatus` enum과 일치하지만, 코드 상 타입 안전성 없이 문자열로 다뤄지고 있어 오타 위험이 있음 |
| created_at | DATETIME | createdAt (LocalDateTime) | |
| updated_at | DATETIME | updatedAt (LocalDateTime) | |

### `guestbook` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT (PK) | id (Long) | |
| writer_id | BIGINT | writerId (`@ManyToOne` UserWithFMJpaEntity, `@JoinColumn(name = "writer_id")`) | |
| owner_id | BIGINT | ownerId (`@ManyToOne` UserWithFMJpaEntity, `@JoinColumn(name = "owner_id")`) | |
| content | VARCHAR(1000) | content (String) | JPA `@Column`에 길이(length=1000) 제약이 명시되어 있지 않음(확인 필요) |
| created_at | DATETIME | createdAt (LocalDateTime) | |

### DB에 없는 JPA 필드
- 없음(두 엔티티 모두 DB 컬럼과 필드 개수·이름이 1:1로 대응됨)
