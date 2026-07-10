# notification BC

## 1. 개요

이 바운디드 컨텍스트(도메인 경계 단위, BC)는 로그인한 사용자에게 쌓이는 "알림(notification)" 데이터를 만들고, 조회하고, 읽음 처리하고, 삭제하는 책임을 담당한다. 친구 요청, 친구 요청 수락/거절, 메시지 전송, 강사-학생 자동 친구, 방명록 작성, 게시글 좋아요/댓글/대댓글, 캘린더 투두/메모, 강의 승인/거절 등 다른 바운디드 컨텍스트에서 발생한 이벤트를 구독(`@TransactionalEventListener`)해서 알림 행을 만들고, 실시간 웹소켓 갱신(`SimpMessagingTemplate`)까지 담당한다.

message BC와의 차이: message BC는 채팅방·메시지·읽음여부(`MessageReadJpaEntity`) 자체의 도메인을 다루고, notification BC는 그 메시지 전송 "사건"을 알림 한 줄(`type = MESSAGE`)로 변환해서 보여주는 쪽이다. 실제로 notification BC의 인프라 계층에는 `message.infrastructure.persistence.MessageReadJpaEntity`, `ChatRoomJpaEntity`를 그대로 참조하는 보조 리포지토리(`NotificationSideMessageReadRepository`, `NotificationSideChatRoomRepository`)가 있어, 메시지 알림 개수·방 제목·읽음 여부는 message BC 테이블에서 직접 읽어오고 notification 테이블에는 메시지 알림 자체의 읽음 상태(`isRead`)를 아예 저장하지 않는다(`NotificationJpaEntity.toEntity`에서 `MESSAGE` 타입이면 `isRead`를 강제로 `null` 처리).

## 2. 패키지 구조

### domain
- `domain/model/Notification.java` — 알림 도메인 모델(불변 값 객체 형태), 타입별 정적 팩토리 메서드 모음
- `domain/model/NotificationType.java` — 알림 타입 Enum (도메인 모델이나 JPA 엔티티에서는 사용되지 않고 `String type`으로 저장됨, 확인 필요: 실제로 이 Enum이 사용되는 곳이 없음)
- `domain/repository/NotificationRepository.java` — 알림 저장/조회/삭제를 위한 포트(인터페이스)

### application
- `application/command/` — `ReadNotificationCommand`, `RemoveNotificationCommand` (커맨드 레코드)
- `application/query/` — `GetNotificationQuery`, `GetMainTotalCountsQuery`, `GetPhoneAppCountsQuery` (쿼리 레코드)
- `application/usecase/` — `NotificationCommandUseCase`, `NotificationQueryUseCase` (유스케이스 인터페이스, 응답용 내부 record인 `NotiView`/`MainTotalCountsView`/`PhoneAppCountsView` 포함)
- `application/service/` — `NotificationCommandService`(읽음/삭제 처리), `NotificationQueryService`(목록/개수 조회+웹소켓 발송), `NotificationHandlerService`(다른 BC 이벤트를 받아 알림 도메인 객체를 생성·저장하는 비즈니스 로직 모음)
- `application/policy/NotificationEligibilityPolicy.java` — 읽기/삭제 요청의 빈 값 검증, 권한(본인 알림인지) 검증 정책
- `application/manager/NotificationSessionManager.java` — 유저별 알림 채널 웹소켓 구독 세션 관리(`ConcurrentHashMap` 기반 인메모리)
- `application/metric/NotificationMetrics.java` — Micrometer `Timer`로 알림 목록 조회 지연시간 측정

### infrastructure
- `infrastructure/catalog/CatalogNotificationRepositoryAdapter.java` — `NotificationRepository` 포트의 구현체(어댑터)
- `infrastructure/event/` — `NotificationCreatedPublishedEvent`(알림 갱신 알림용 이벤트), `NotificationLifecycleEventHandler`(친구/메시지/커뮤니티/캘린더/강의 등 타 BC 이벤트 구독→`NotificationHandlerService` 호출), `NotificationWebsocketListener`(`NotificationCreatedPublishedEvent` 구독→온라인 유저에게만 목록/개수 재조회 후 웹소켓 발송)
- `infrastructure/persistence/` — `NotificationJpaEntity`(테이블 매핑), `SpringDataNotificationRepository`(notification 테이블 JPA 리포지토리), `NotificationSideChatRoomRepository`/`NotificationSideMessageReadRepository`/`NotificationSideUserRepository`(다른 BC 테이블을 읽기 위한 보조 리포지토리)

### presentation
- `presentation/api/NotificationController.java` — REST 컨트롤러
- `presentation/api/request/` — `ReadNotificationRequest`, `RemoveNotificationRequest`
- `presentation/api/response/` — `GetMainTotalCountsResponse`, `GetNotificationResponse`, `GetPhoneAppCountsResponse`

## 3. 진행 상태

### 구현되어 있는 기능
- 알림 목록 조회(메시지 알림은 채팅방 단위로 묶어서 최신 발신자 닉네임까지 조합해 표시)
- 메인 페이지 종 아이콘용 총 안 읽은 알림 개수 조회
- 휴대폰 앱별(친구+메시지 / 캘린더 / 커뮤니티) 안 읽은 알림 개수 조회
- 알림 읽음 처리(일반 알림 + 메시지 알림 방 단위 벌크 처리, 본인 알림인지 권한 검증 포함)
- 알림 삭제 처리(일반 알림은 하드 삭제, 메시지 알림은 `message_read` 쪽 소프트 삭제)
- 아래 이벤트들을 구독해서 알림 자동 생성 + 웹소켓 실시간 반영: 친구 요청/철회/수락/거절, 메시지 전송, 강사-학생 자동 친구, 방명록 작성, 게시글 좋아요/댓글/대댓글, 캘린더 투두/메모(도메인 모델에는 팩토리 메서드가 있으나 이벤트 핸들러에서 호출하는 코드는 확인되지 않음, 확인 필요), 강의 승인/거절
- 오프라인 유저는 `SimpUserRegistry`로 판별해서 DB 재조회/웹소켓 발송을 생략하는 최적화(Short-Circuit)

### 비어있거나 미완성으로 보이는 부분
- `domain/model/NotificationType.java` Enum이 선언되어 있지만, `Notification` 도메인 모델과 `NotificationJpaEntity`는 모두 `String type`을 그대로 사용하고 있어 이 Enum이 실제로 참조되는 코드를 찾지 못했다(확인 필요).
- `Notification.createMessageNotification`, `Notification.todoCalendar`, `Notification.memoCalendar` 정적 팩토리 메서드는 있지만, `NotificationHandlerService`에서 `todoCalendar`/`memoCalendar`를 호출하는 `createTodoNotification`/`createMemoNotification` 메서드 자체는 존재하나, 이를 호출하는 이벤트 리스너(`NotificationLifecycleEventHandler`)에는 대응하는 캘린더 관련 `@TransactionalEventListener` 메서드가 없다. 즉 캘린더 투두/메모 알림 생성 서비스 메서드는 구현되어 있지만 실제로 이벤트로 트리거되는 경로가 코드상 확인되지 않는다(확인 필요).
- `NotificationRepository` 포트에 `saveAll(List<NotificationJpaEntity>)` 메서드가 선언되어 있고 어댑터에도 구현되어 있으나, 서비스 계층에서 이를 호출하는 코드는 찾지 못했다(미사용으로 보임, 확인 필요).
- `countByUserIdAndType` 포트 메서드(어댑터에도 구현됨)도 서비스 계층에서 호출하는 코드를 찾지 못했다. 실제 휴대폰 앱별 개수 조회는 `countUnreadGroupByType` 한 방 쿼리로 대체된 것으로 보인다(주석에도 "개선 - 앱별 알림 통합해서 가져오기"라고 적혀 있음).
- `findRoomTitleById` 포트 메서드(단건 조회)도 어댑터에는 있으나 서비스단 호출부는 찾지 못했다. `findRoomTitlesByIdsIn`(IN절 일괄 조회)로 대체된 것으로 보인다.

### 코드 내 TODO/FIXME 주석
- 별도의 `TODO`/`FIXME` 문자열 표기는 발견되지 않았다. 다만 `Notification.java` 11~18번째 줄에 다음과 같은 주석이 있다.
  ```
  //추후
  //v2
  private final Boolean isRead;
  ```
  `NotificationJpaEntity.java` 35~38번째 줄에도 동일하게 `//추후` `//v2` 주석이 붙어 있다.
- `NotificationHandlerService.sendMessageNotification` 138번째 줄: `//추후 isRead 생기면 주석 해제` 라는 주석이 있고, `Notification.createMessageNotification` 54번째 줄에도 `//isRead 생기면 주석 해제`라는 동일 취지의 주석이 있다.

## 4. API 목록

컨트롤러 `NotificationController`에는 클래스 레벨 `@RequestMapping`이 없고, 각 메서드에 전체 경로가 직접 붙어 있다.

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| GET | /api/v2/notice/notificationlist | NotificationController.getNotification | 로그인 유저에게 온 모든 알림 내역을 조회한다. (Javadoc `@Operation` 주석) |
| GET | /api/v2/notice/total-counts | NotificationController.getMainTotalCounts | 로그인 후 메인 페이지의 상단 종에 띄워질 총 알림 개수 (Javadoc `@Operation` 주석) |
| GET | /api/v2/notice/app-counts | NotificationController.getPhoneAppCounts | 휴대폰 속 친구+메시지/캘린더/커뮤니티 앱에 띄워질 알림 개수 (Javadoc `@Operation` 주석) |
| PATCH | /api/v2/notice/read | NotificationController.readNotification | 알림 목록 하나 클릭 또는 일괄 읽음 처리를 한다. (Javadoc `@Operation` 주석) |
| DELETE | /api/v2/notice/remove | NotificationController.removeNotification | 알림 목록 하나 클릭 또는 일괄 삭제 처리를 한다. (Javadoc `@Operation` 주석) |

## 5. 도메인 모델

### Notification (domain/model/Notification.java)
`@AllArgsConstructor` + `@Getter`가 붙은, 모든 필드가 `final`인 불변 클래스. 순수 자바 객체이며 JPA 애노테이션은 없다.

주요 필드:
- `id` (Long) — 알림 PK, 생성 시점에는 `null`
- `userId` (Long) — 알림을 받는(또는 저장 대상) 유저 ID
- `type` (String) — 알림 종류를 나타내는 문자열. 코드에서 등장하는 값: `"FRIEND_REQUEST"`, `"MESSAGE"`, `"GUESTBOOK"`, `"POST"`, `"CALENDAR"`, `"APPROVAL"`
- `refId` (Long) — 알림이 참조하는 대상의 ID (요청자 ID, 방 ID, 게시글 ID, 방명록 ID, 투두/메모 ID, 강의 ID 등 타입에 따라 의미가 다름)
- `message` (String) — 알림 문구
- `isRead` (Boolean) — 읽음 여부. 메시지 알림 생성 시에는 `null`로 채워짐(메시지의 읽음 여부는 message BC의 `message_read` 테이블이 권위를 가지기 때문)
- `createdAt` (LocalDateTime) — 생성 시각

생성자는 없고, 오직 아래와 같은 정적 팩토리(비즈니스) 메서드로만 인스턴스를 만들 수 있다.
- `createFriendRequest(Long userId, String message, Long refId)` — 친구 요청 알림, type=`FRIEND_REQUEST`
- `createFriendAccept(Long acceptorUserId, String message, Long fromUserId)` — 친구 요청 수락 알림, type=`FRIEND_REQUEST`
- `createMessageNotification(Long senderId, String type, Long roomId, String message)` — 메시지 전송 알림, `isRead`는 `null`
- `createAutoFriend(Long fromUserId, String message, Long toUserId)` — 강사-학생 자동 친구 알림, type=`FRIEND_REQUEST`
- `createGuestBook(Long ownerId, String message, Long bookId, LocalDateTime now)` — 방명록 알림, type=`GUESTBOOK`
- `likePost(Long postOwnerId, String message, Long postId)` — 게시글 좋아요 알림, type=`POST`
- `commentPost(Long postOwnerId, String message, Long postId)` — 게시글 댓글 알림, type=`POST`
- `replyComment(Long parentCommentOwnerId, String replyMessage, Long postId)` — 대댓글(부모 댓글 주인 대상) 알림, type=`POST`
- `postComment(Long postOwnerId, String postMessage, Long postId)` — 대댓글(게시글 주인 대상) 알림, type=`POST`
- `todoCalendar(Long userId, String message, Long todoId)` — 캘린더 투두 알림, type=`CALENDAR`
- `memoCalendar(Long userId, String message, Long memoId)` — 캘린더 메모 알림, type=`CALENDAR`
- `lectureApproval(Long teacherId, String message, Long lectureId, LocalDateTime occurredAt)` — 강의 승인/거절 알림, type=`APPROVAL`

### NotificationType (domain/model/NotificationType.java)
```java
public enum NotificationType {
    APPROVAL, FRIEND_REQUEST, MESSAGE, GUESTBOOK, POST, CALENDAR
}
```
Enum 값 목록: `APPROVAL`, `FRIEND_REQUEST`, `MESSAGE`, `GUESTBOOK`, `POST`, `CALENDAR`. 위에서 언급했듯 실제 `Notification`/`NotificationJpaEntity`는 `String type`을 그대로 쓰기 때문에, 이 Enum이 어디서 사용되는지는 확인하지 못했다(확인 필요).

### NotificationJpaEntity (infrastructure/persistence/NotificationJpaEntity.java)
`notification` 테이블에 매핑된 JPA 엔티티. 필드는 `Notification` 도메인 모델과 거의 동일하되 `userId`가 `UserWithFMJpaEntity`(연관관계, LAZY)로 매핑되어 있다는 점이 다르다. 단순 getter 외의 비즈니스 메서드:
- `toEntity(Notification domain, UserWithFMJpaEntity userJpaEntity)` (정적) — 도메인 모델을 JPA 엔티티로 변환. `type`이 `"MESSAGE"`면 `isRead`를 강제로 `null`로 세팅
- `updateMessageNotification(String message, LocalDateTime createdAt)` — 기존 메시지 알림 행의 문구/시각만 갱신(읽음 여부는 손대지 않음)
- `toDomain()` — JPA 엔티티를 순수 도메인 모델(`Notification`)로 복원
- `markAsRead()` — `isRead`를 `true`로 변경(단, 실제 읽음 처리 흐름에서는 이 메서드 대신 `SpringDataNotificationRepository`의 벌크 UPDATE 쿼리를 사용하는 것으로 보이며, 이 메서드를 직접 호출하는 코드는 찾지 못함, 확인 필요)

## 6. ERD 스키마 대조

### `notification` 테이블

| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT (PK) | `id` (Long) | |
| user_id | BIGINT | `userId` (`UserWithFMJpaEntity`, `@ManyToOne` LAZY) | |
| type | ENUM('APPROVAL','FRIEND_REQUEST','MESSAGE','GUESTBOOK','POST','CALENDAR','PAYMENT') | `type` (String) | DB는 ENUM, JPA는 `String`으로 매핑(별도 컨버터 없음). ENUM 값 중 `PAYMENT`는 코드의 `NotificationType`(APPROVAL/FRIEND_REQUEST/MESSAGE/GUESTBOOK/POST/CALENDAR 6개)에 없음 — 폐기된 결제 기능의 흔적으로, DB ENUM 정의에만 남은 유물로 보임(확인 필요) |
| ref_id | BIGINT | `refId` (Long) | |
| message | VARCHAR(500) | `message` (String) | |
| is_read | BOOLEAN | `isRead` (Boolean) | |
| created_at | DATETIME | `createdAt` (LocalDateTime) | |

DB에 없는 JPA 필드: 없음.

컬럼 자체는 모두 일치한다. 유일한 불일치는 `type` ENUM에 `PAYMENT` 값이 남아있는데, 도메인의 `NotificationType` enum(6개 값)과 실제 코드(`Notification`/`NotificationJpaEntity`) 어디에서도 `PAYMENT` 문자열을 만들거나 다루는 곳이 없다는 점이다(`grep` 결과 notification 패키지 내 `PAYMENT` 사용 0건). 결제 기능 폐기(2026-06-17 확정) 이전에 만들어진 DB ENUM 정의가 그대로 남아있는 것으로 추정되며, 실제 DB 마이그레이션으로 제거할지 여부는 확인 필요.
