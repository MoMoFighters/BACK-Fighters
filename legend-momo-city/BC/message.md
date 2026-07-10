# message BC

## 1. 개요

message 바운디드 컨텍스트(BC, 업무 영역을 나누는 경계)는 사용자 간의 1:1 채팅과 단체(다대다) 채팅을 담당한다. 채팅방을 만들고 찾는 기능, 메시지를 보내고 읽음 처리하는 기능, 채팅방 이름을 바꾸거나 멤버를 초대하는 기능, 채팅방을 나가는 기능을 모두 포함한다.

코드를 보면 message BC 자체는 "채팅이라는 행위"(방 개설, 메시지 저장, 읽음 처리)에만 집중하고, 실제 사용자에게 보여줄 "안 읽은 알림 개수(배지)"나 "휴대폰 앱 알림"은 notification BC(`com.wanted.momocity.notification`)가 따로 담당한다. message 쪽 서비스들(`MessageCommandService`, `MessageWebsocketListener`)이 메시지 전송이 끝난 뒤 `NotificationQueryUseCase`를 호출해 배지 카운트를 갱신해달라고 요청하는 구조로 보아, message는 "채팅 데이터의 주인", notification은 "그 데이터를 근거로 사용자에게 알려주는 역할"로 나뉘어 있다.

## 2. 패키지 구조

### domain (도메인 계층 - 핵심 규칙과 데이터)
- `model/` — `ChatRoom`(단순 값 객체), `Message`(메시지 애그리거트, `create()` 정적 생성 메서드 보유), `MessageAnnounceType`(enum: `LEAVE`, `INVITE`, `RENAME`)
- `repository/` — `MessageRepository`(포트 인터페이스, 60개 이상의 메서드 선언), `ChatRoomQueryProjection`(DB 조회 결과를 담는 그릇용 record)
- `event/` — 웹소켓/알림용 이벤트 레코드 9개: `ChatMessageSentWebsocketPublishedEvent`, `ChatRoomLeaveWebsocketPublishedEvent`, `ChatRoomMemberInviteWebsocketPublishedEvent`, `ChatRoomReadWebsocketPublishedEvent`, `ChatRoomReenteredPublishedEvent`, `ChatRoomRenamedWebsocketPublishedEvent`, `LeaveChatRoomWebsocketPublishedEvent`, `SendMessagePublishedEvent`

### application (응용 계층 - 유스케이스 실행)
- `command/` — 명령 객체(record) 6개: `CreateChatRoomCommand`, `InviteRoomMemberCommand`, `LeaveChatRoomCommand`, `ModifyRoomTitleCommand`, `ReadMessageCommand`, `SendMessageCommand`
- `query/` — 조회 객체(record) 2개: `FindChatRoomQuery`, `GetMessageHistoryQuery`
- `usecase/` — 인터페이스 2개: `MessageCommandUseCase`(채팅방 개설/메시지 전송/읽음/나가기/이름 변경/초대), `MessageQueryUseCase`(채팅방 목록 조회/메시지 내역 조회)
- `service/` — 구현체 3개: `MessageCommandService`(730줄, 명령 처리 전담), `MessageQueryService`(644줄, 조회 처리 전담), `MessageHandlerService`(98줄, 회원가입/친구삭제 이벤트에 대응한 나와의 채팅방 생성·퇴장 처리)
- `policy/` — `MessageEligibilityPolicy`(채팅방 개설/전송/나가기/초대/이름변경 등 모든 비즈니스 규칙 검증을 모아둔 클래스)
- `manager/` — `ChatRoomSessionManager`(유저가 지금 어느 방에 실시간으로 머물고 있는지 메모리에서 추적하는 세션 관리자)
- `metric/` — `MessageMetrics`(Micrometer 기반 지표: 방 멤버수 분포, 메시지 내역 조회 지연시간, 채팅방 목록 조회 지연시간, 재입장 카운트, 메시지 발송 카운트)

### infrastructure (인프라 계층 - DB, 이벤트, 외부 연동)
- `persistence/` — JPA 엔티티 4개(`ChatRoomJpaEntity`, `ChatRoomMemberJpaEntity`, `MessageJpaEntity`, `MessageAnnounceJpaEntity`, `MessageReadJpaEntity` 총 5개), Spring Data 리포지토리 5개(`SpringDataChatRoomRepository`, `SpringDataChatRoomMemberRepository`, `SpringDataMessageRepository`, `SpringDataMessageReadRepository`, `SpringDataMessageAnnounceRepository`), 다른 BC 테이블을 조회하기 위한 "사이드" 리포지토리 3개(`MessageSideUserRepository`, `MessageSideEnrollmentRepository`, `MessageSideFriendRepository`)
- `catalog/` — `CatalogMessageAdapter`(`MessageRepository` 포트의 실제 구현체, 위 리포지토리들을 조합해서 도메인이 원하는 데이터를 만들어줌)
- `adapter/` — `ChatContentAdapter`(report BC가 정의한 `ChatContentPort`를 구현, 신고 기능에서 특정 메시지 내용을 조회할 때 사용)
- `event/` — `MessageLifecycleEventHandler`(회원가입 완료·친구 삭제 이벤트를 받아 나와의 채팅방 생성/퇴장 처리), `MessageWebsocketListener`(방 개설/메시지 전송/읽음/나가기/이름변경/초대 등 7가지 이벤트를 받아 웹소켓으로 실시간 화면 갱신 및 notification BC 호출)

### presentation (표현 계층 - API)
- `api/MessageController.java` — 컨트롤러 1개(모든 채팅 API를 이 클래스가 담당)
- `api/request/` — 요청 DTO 4개: `CreateChatRoomRequest`, `InviteRoomMemberRequest`, `ModifyRoomTitleRequest`, `SendMessageRequest`
- `api/response/` — 응답 DTO 8개: `CreateChatRoomResponse`, `FindChatRoomResponse`, `GetMessageHistoryResponse`, `InviteRoomMemberResponse`, `LeaveChatRoomResponse`, `ModifyRoomTitleResponse`, `ReadMessageResponse`, `SendMessageResponse`

## 3. 진행 상태

### 구현되어 있는 기능
- 채팅방 조회 및 개설(1:1, 다대다 모두 지원, 기존 방 재사용/과거 방 복구 로직 포함)
- 메시지 전송(친구 상태·활성 상태·방 멤버 여부 검증 포함)
- 메시지 읽음 처리(벌크 업데이트 쿼리로 처리)
- 메시지 내역 조회(커서 기반 페이지네이션, 20개씩)
- 채팅방 나가기(마지막 남은 사람이면 방 전체 삭제, 아니면 본인만 삭제)
- 채팅방 이름 변경(다대다 채팅방 전용)
- 다대다 채팅방 멤버 초대
- 회원가입 완료 시 "나와의 채팅방" 자동 생성, 친구 삭제 시 1:1 채팅방 자동 퇴장 처리
- 웹소켓을 통한 실시간 화면 갱신(방 개설/메시지 전송/읽음/나가기/이름변경/초대/재입장 각각에 대응하는 이벤트-리스너 존재)
- Micrometer 지표 수집(방 멤버 수 분포, 조회 지연시간, TPS 등)

### 비어있거나 미완성으로 보이는 부분
- `Message` 도메인 모델에 `isRead` 필드가 주석으로 남아 있음(현재는 읽음 여부를 별도 테이블 `MessageReadJpaEntity`로 관리하는 구조로 옮겨간 것으로 보임)
- `MessageJpaEntity`에도 `changeIsRead(boolean isRead)` 메서드가 주석 처리되어 있고, "message_read 테이블로 연계"라는 주석이 달려 있어 과거 구조에서 리팩토링된 흔적으로 보임
- `MessageQueryUseCase.ChatRoomView`, `MessageHistoryView` 레코드에 `isNotActive`, `shouldMasked`, `isRead` 필드가 주석 처리되어 있음(개별 멤버 단위로 이동한 것으로 추정)
- `MessageCommandService.sendMessageCommandHandle()`, `MessageWebsocketListener` 관련 로직에 대량의 주석 처리된 옛 코드(웹소켓 발송을 위한 반복문 방식)가 그대로 남아 있음 — 비동기 이벤트 방식으로 리팩토링되면서 이전 코드를 지우지 않고 주석으로만 남긴 것으로 보임
- `CatalogMessageAdapter`에 `import javax.swing.text.html.Option;`, `import static org.apache.logging.log4j.ThreadContext.isEmpty;` 처럼 실제로 쓰이지 않는 것으로 보이는 임포트가 남아 있음(확인 필요)
- `MessageSideUserRepository.findUserById(Long senderId)` 메서드가 선언되어 있으나 코드 내에서 사용되는 곳을 찾지 못함(확인 필요)
- `CreateChatRoomRequest`에 `@NotBlank`, `@NotEmpty` 임포트가 있으나 필드에 실제로 붙어있지 않음(확인 필요)

### TODO/FIXME 주석
코드 전체를 grep한 결과 `TODO`, `FIXME` 문자열은 발견되지 않았다. 다만 `// v2 ->` 형태의 주석이 여러 곳에 있어 "버전 2로 확장하면서 바뀐 부분"을 표시하는 용도로 쓰이고 있다.

## 4. API 목록

`MessageController`에는 클래스 레벨 `@RequestMapping`이 없고, 각 메서드에 전체 경로가 직접 지정되어 있다.

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| GET | /api/v2/messages/chatrooms | MessageController.getChatRooms | 로그인 유저가 존재하는 모든 채팅방을 조회한다. (Javadoc/`@Operation` 명시) |
| POST | /api/v2/messages/chatrooms/create | MessageController.findAndNewChatRoom | 채팅방 개설 시 기존 채팅방 존재 여부 확인 후 있으면 기존 채팅방으로 보내고 없으면 개설한다. (`@Operation` 명시) |
| POST | /api/v1/messages/send/{roomId} | MessageController.sendMessage | 채팅방을 선택하고 메시지를 전송한다. (`@Operation` 명시) |
| PATCH | /api/v1/messages/read/{roomId} | MessageController.readMessages | 채팅방 진입 시 읽음과 내역 조회 두 개의 API 호출 및 웹소켓으로 채팅방 머무르는 여부 확인 (`@Operation` 명시) |
| GET | /api/v2/messages/history/{roomId} | MessageController.getMessageHistory | 메시지 내역을 최신 20개씩 보내고 최상단 스크롤하면 마지막 메시지 아이디 기준으로 최신순 보여준다. (`@Operation` 명시) |
| DELETE | /api/v1/messages/chatRooms/leave/{roomId} | MessageController.leaveChatRoom | 혼자 남으면 전체 폭파하고 누군가 남아있으면 멤버에서만 삭제한다. (`@Operation` 명시) |
| PATCH | /api/v2/message/chatrooms/modify/{roomId} | MessageController.modifyRoomTitle | 다대다인 경우에만 채팅방 이름 수정 가능 (`@Operation` 명시) |
| POST | /api/v2/message/chatrooms/invite/{roomId} | MessageController.inviteRoomMember | 다대다 채팅방에 친구 상태인 활성 학생을 초대한다. (`@Operation` 명시) |

참고: URL 앞부분이 `/api/v2/message/...`(단수, 이름변경/초대)와 `/api/v2/messages/...`(복수, 목록/개설/내역)로 일관되지 않게 섞여 있음(확인 필요, 오타로 추정되나 실제 배포된 API 계약일 수 있어 임의로 통일하지 않음).

## 5. 도메인 모델

### ChatRoom (`domain/model/ChatRoom.java`)
- 필드: `id`(Long), `title`(String)
- 단순 값 객체로 `@AllArgsConstructor`, `@Getter`만 있고 비즈니스 메서드는 없음. 실제 채팅방 로직은 인프라 계층의 `ChatRoomJpaEntity`가 담당하고 있어, 이 도메인 클래스는 실제로 폭넓게 쓰이지 않는 것으로 보임(확인 필요).

### Message (`domain/model/Message.java`)
- 필드: `id`, `roomId`, `senderId`, `content`, `createdAt` (`isRead` 필드는 주석 처리되어 현재 미사용)
- 비즈니스 메서드: `static Message create(Long roomId, Long senderId, String content)` — 새 메시지를 발송할 때 도메인 생성 규칙을 담당(id는 null, 생성 시각은 현재 시각으로 자동 세팅)
- 주석에 "도메인 애그리거트"라고 명시되어 있음

### MessageAnnounceType (`domain/model/MessageAnnounceType.java`)
- enum 값: `LEAVE`(나가기), `INVITE`(초대/재입장), `RENAME`(이름변경) — 채팅방 안내 문구(공지성 메시지)의 유형을 나타냄. 다만 실제 구현(`MessageAnnounceJpaEntity.createAnnounce()`)에서는 이 enum이 아니라 `"LEAVE"`, `"INVITE"`, `"RENAME"` 문자열 리터럴을 그대로 넘기고 있어서, enum이 선언은 되어 있지만 실제로 타입으로 사용되지는 않는 것으로 보임(확인 필요).

### 인프라 계층의 실질적인 데이터 모델(JPA 엔티티)
문서 성격상 참고로 같이 적는다. 도메인 모델보다 실제 로직에서 훨씬 많이 쓰인다.
- `ChatRoomJpaEntity`: `id`, `roomTitle`, `createdAt`, `updatedAt`. 비즈니스 메서드로 `changeCreatedAt`, `changeUpdatedAt`, `registRoomTitle`(최초 등록), `updateRoomTitle`(수정 시각도 같이 갱신) 보유.
- `ChatRoomMemberJpaEntity`: `id`, `roomId`(방), `userId`(멤버), `joinedAt`. 정적 생성 메서드로 `createMembership`(신규/복구 멤버십), `createInviteMembership`(다대다 초대 멤버십) 보유.
- `MessageJpaEntity`: `id`, `roomId`, `senderId`, `content`, `createdAt`, `updatedAt`. 정적 생성 메서드 `createNewMessage`, 그리고 채팅 목록에서 "마지막 메시지가 없을 때" 노출용 가짜 메시지를 만들기 위한 `changeContent`/`changeCreatedAt` 보유.
- `MessageAnnounceJpaEntity`: `id`, `roomId`, `targetId`(안내 문구의 대상 유저), `content`, `type`(문자열: LEAVE/INVITE/RENAME), `createdAt`. 정적 생성 메서드 `createAnnounce` 보유.
- `MessageReadJpaEntity`: `id`, `roomId`, `messageId`, `userId`, `isMsgRead`(메시지 읽음 여부), `isNotiRead`(알림 읽음 여부), `isDeleted`. 정적 생성 메서드 `createNewUnreadMessage` 보유.

## 6. ERD 스키마 대조

ERD Cloud에서 뽑은 5개 테이블 생성 SQL과 `infrastructure/persistence` 밑 JPA 엔티티 5개를 컬럼 단위로 대조한 결과다.

### `message` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | `MessageJpaEntity.id` | |
| room_id | BIGINT | `MessageJpaEntity.roomId` (`@ManyToOne` FK, `ChatRoomJpaEntity` 참조) | |
| sender_id | BIGINT | `MessageJpaEntity.senderId` (`@ManyToOne` FK, `UserWithFMJpaEntity` 참조) | |
| content | TEXT | `MessageJpaEntity.content` | |
| created_at | DATETIME | `MessageJpaEntity.createdAt` | |
| updated_at | DATETIME | `MessageJpaEntity.updatedAt` | |

DB에는 `is_read` 컬럼이 아예 없다. `MessageJpaEntity`에도 `isRead` 필드 자체는 존재하지 않고, 61~63번째 줄에 `changeIsRead(boolean isRead)` 메서드만 주석으로 남아 있으며 바로 아래에 "message_read 테이블로 연계"라는 주석이 달려 있다. 즉 읽음 여부는 `message` 테이블이 아니라 `message_read` 테이블에서 관리하는 구조로 완전히 옮겨간 상태이고, DB 스키마도 이와 일치한다(불일치 없음).

### `message_read` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | `MessageReadJpaEntity.id` | |
| room_id | BIGINT | `MessageReadJpaEntity.roomId` (`@ManyToOne` FK) | |
| message_id | BIGINT | `MessageReadJpaEntity.messageId` (`@ManyToOne` FK) | |
| user_id | BIGINT | `MessageReadJpaEntity.userId` (`@ManyToOne` FK) | |
| is_msg_read | BOOLEAN | `MessageReadJpaEntity.isMsgRead` | |
| is_noti_read | BOOLEAN | `MessageReadJpaEntity.isNotiRead` | |
| is_deleted | BOOLEAN | `MessageReadJpaEntity.isDeleted` | |

컬럼과 필드가 1:1로 정확히 일치한다. `changeIsMsgRead`, `changeIsNotiRead` 두 변경 메서드와 `createNewUnreadMessage` 정적 생성 메서드가 실제로 이 세 boolean 필드를 다루고 있어, "메시지를 읽음 여부"는 이 테이블에서 정상적으로 관리되고 있는 것으로 확인된다.

### `message_announce` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | `MessageAnnounceJpaEntity.id` | |
| room_id | BIGINT | `MessageAnnounceJpaEntity.roomId` (`@ManyToOne` FK) | |
| target_id | BIGINT | `MessageAnnounceJpaEntity.targetId` (`@ManyToOne` FK) | |
| content | TEXT | `MessageAnnounceJpaEntity.content` | |
| type | ENUM("LEAVE", "INVITE", "RENAME") | `MessageAnnounceJpaEntity.type` | DB는 ENUM 타입인데 JPA 필드는 `String`으로 선언되어 있음(`@Enumerated` 없이 순수 문자열). `createAnnounce()`가 문자열 그대로 저장하고 있어 값 자체는 "LEAVE"/"INVITE"/"RENAME"로 DB ENUM 정의와 일치하지만, 타입 안전성이 코드 레벨에서 보장되지 않음(확인 필요) |
| created_at | DATETIME | `MessageAnnounceJpaEntity.createdAt` | |

별도로 `domain/model/MessageAnnounceType.java`에 `LEAVE`, `INVITE`, `RENAME` 값을 가진 enum이 선언되어 있으나(5번 섹션 참고), `MessageAnnounceJpaEntity`는 이 enum을 참조하지 않고 `String type` 필드를 그대로 쓴다. 즉 도메인 enum과 DB ENUM 값 목록 자체는 일치하지만, JPA 엔티티가 그 enum 타입을 실제로 사용하지 않는 불일치가 있다.

### `chat_room` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | `ChatRoomJpaEntity.id` | |
| title | VARCHAR(100) | `ChatRoomJpaEntity.roomTitle` | `@Column(name = "title")`로 매핑되어 있어 자바 필드명(`roomTitle`)과 DB 컬럼명(`title`)이 다를 뿐 매핑 자체는 일치 |
| created_at | DATETIME | `ChatRoomJpaEntity.createdAt` | |
| updated_at | DATETIME | `ChatRoomJpaEntity.updatedAt` | |

### `chat_room_member` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | `ChatRoomMemberJpaEntity.id` | |
| room_id | BIGINT | `ChatRoomMemberJpaEntity.roomId` (`@ManyToOne` FK) | |
| user_id | BIGINT | `ChatRoomMemberJpaEntity.userId` (`@ManyToOne` FK) | |
| joined_at | DATETIME | `ChatRoomMemberJpaEntity.joinedAt` | |

### DB에 없는 JPA 필드
5개 엔티티 전체를 확인한 결과, DB 스키마에 없는데 JPA 엔티티에만 존재하는 필드는 발견되지 않았다. 5개 테이블 모두 컬럼과 필드가 1:1로 대응된다.

### 종합
- 컬럼 단위 불일치: 없음(모든 컬럼이 JPA 필드로 존재).
- 타입/설계 관점 불일치 1건: `message_announce.type`이 DB에서는 ENUM("LEAVE","INVITE","RENAME")으로 정의되어 있지만, `MessageAnnounceJpaEntity.type`은 `String`으로 선언되어 있어 `@Enumerated`를 통한 타입 매핑이 아니다. 값 목록 자체(LEAVE/INVITE/RENAME)는 도메인 enum `MessageAnnounceType`과도 일치하지만, 코드에서 이 enum이 실제로 연결되어 쓰이지는 않는다.
- `message`/`message_read` 테이블의 읽음 여부 관련 컬럼은 DB 스키마상으로도 `message` 테이블에는 없고 `message_read` 테이블에만 존재해서, 5번 섹션에서 서술한 "isRead가 message_read로 이관됐다"는 코드 관찰과 DB 스키마가 서로 일치한다.
