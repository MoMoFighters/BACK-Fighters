# order BC

## 1. 개요

order 바운디드 컨텍스트는 사용자가 포인트를 사용해서 상품(예: 프로필 이미지)을 구매하는 기능과, 그 구매(포인트 사용/획득) 내역을 조회하는 기능을 담당한다. 구매 시점에 이미 보유한 상품인지, 포인트가 충분한지를 확인하고, 통과하면 포인트를 차감하면서 구매 내역을 저장한다.

## 2. 패키지 구조

- `application/`
  - `command/` — `MakeOrderCommand` (구매 요청을 표현하는 커맨드 객체)
  - `policy/` — `OrderPolicy` (구매 가능 여부를 검증하는 정책 클래스)
  - `port/` — `CheckPointPort`, `GetAllProductPort`, `LoadItemPort` (다른 바운디드 컨텍스트에 의존하기 위한 출력 포트 인터페이스)
  - `service/` — `OrderCommandService`, `OrderQueryService` (유스케이스 구현체)
  - `usecase/` — `OrderCommandUsecase`, `OrderQueryUsecase` (유스케이스 인터페이스)
- `domain/`
  - `exception/` — `AlreadyOwnedException`, `InsufficientPointException`, `ItemNotFoundException`, `OrderExceptionHandler`
  - `model/` — `CheckItem`, `ListResult`, `OrderHistory`, `OrderHistoryList`, `ProfileItemResult`, `Reason`(enum), `StoreItemResult`, `Type`(enum)
  - `repositroy/` — `OrderRepository`
    - 패키지명이 `repositroy`로 되어 있다. `repository`의 오타로 보인다(확인 필요: 의도적 명명이 아니라면 수정 대상).
- `infrastructure/`
  - `adapter/` — `AddOrderHistoryAdapter`, `CheckIsOrderedAdapter`, `GetUserOwnedItemIdAdapter` (다른 BC가 정의한 포트를 order 쪽 데이터로 구현하는 어댑터)
  - `persistence/` — `OrderJpaEntity`, `OrderRepositoryAdapter`, `SpringDataOrderRepository`
- `presentation/`
  - `api/` — `OrderController`
  - `api/common/` — `OrderResponseCode`, `OrderResponseMessage`
  - `api/request/` — `MakeOrderRequest`
  - `api/response/` — `OrderHistoryResponse`, `ProfileItemListResponse`

## 3. 진행 상태

### 구현되어 있는 기능
- 상품 구매(`POST /api/v1/order/new`): 상품명으로 아이템 조회 → 이미 보유 중인지/포인트가 충분한지 검증 → 포인트 차감 → 구매 내역 저장.
- 포인트 사용 내역 조회(`GET /api/v1/order/list`): 페이지네이션 포함.
- 사용 가능한 프로필 이미지 목록 조회(`GET /api/v1/order/profile/list`): 전체 상품 목록과 사용자가 보유한 상품을 대조해서 소유/미소유로 구분.
- 다른 BC(store로 추정)가 사용하는 포트인 `CheckIsOrderedPort`, `GetUserOwnedItemIdPort`를 order의 인프라 계층에서 구현해서 제공.

### 비어있거나 미완성으로 보이는 부분
- `domain/model/OrderHistory.java` — 클래스 본문이 비어 있다(필드/메서드 없음). 실제로는 `ListResult`, `OrderHistoryList`가 이력 데이터를 대신 표현하고 있어, `OrderHistory` 클래스는 미사용 상태로 보인다(확인 필요).
- `OrderRepository.findOwnedItemIdsByUserIdAndReason`의 반환형이 `List<Long>`인데, 인터페이스와 구현체(`OrderRepositoryAdapter`) 양쪽에 `Set<Long>` 버전이 주석으로 남아 있다. `import java.util.Set;`도 사용되지 않은 채 남아 있음(확인 필요: 자료구조를 Set으로 바꾸려다 중단된 것으로 보임).
- `OrderQueryService`에도 동일하게 `Set<Long> ownedItemIds` 선언이 주석으로 남아 있음.

### 코드 내 TODO/FIXME 주석
- 별도의 `TODO`/`FIXME` 표시 주석은 발견되지 않았다.

## 4. API 목록

컨트롤러 클래스 레벨 매핑: `@RequestMapping("/api/v1/order")` (`OrderController`)

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| POST | /api/v1/order/new | OrderController.makeOrder | 상품 구매 시 user 테이블에 point 차감 + order_history에 행 내역 추가 (Javadoc `@Operation` 설명 인용) |
| GET | /api/v1/order/list | OrderController.getOrderHistoryList | 포인트 사용 내역 조회 (`@Operation` summary 인용) |
| GET | /api/v1/order/profile/list | OrderController.getAvailableProfile | 전체 프사 목록 중 사용 가능한 것과 아닌 것을 구분하여 출력 (`@Operation` 설명 인용) |

## 5. 도메인 모델

### 값 객체(record)
- `CheckItem(Long itemId, Long price)` — 구매 대상 상품의 id와 가격을 담는 조회 결과. 비즈니스 메서드 없음.
- `ListResult(Type type, Reason reason, LocalDateTime createdAt, Long amount)` — 포인트 변동 내역 한 건. 비즈니스 메서드 없음.
- `OrderHistoryList(List<ListResult> list, int page, int size, long totalElements, int totalPages)` — 포인트 내역 페이지네이션 결과. 비즈니스 메서드 없음.
- `ProfileItemResult(Long itemId, String itemName, String imageUrl, boolean owned)` — 프로필 이미지 상품 목록 조회 시 소유 여부를 포함한 결과. 비즈니스 메서드 없음.
- `StoreItemResult(Long itemId, String itemName, String imageUrl)` — 상점(store BC로 추정)의 전체 상품 정보. 비즈니스 메서드 없음.
- `MakeOrderCommand(Long userId, Reason reason, String itemName)` (application/command) — 구매 요청 커맨드. `type()` 메서드가 항상 `Type.USED`를 반환하는 비즈니스 메서드를 가짐(현재 구매는 포인트를 "사용"하는 경우만 지원한다는 뜻으로 보임, 확인 필요).

### 클래스
- `OrderHistory` (domain/model) — 필드/메서드가 전혀 없는 빈 클래스.

### Enum
- `Reason` — `COMPLETE, REVIEW, PROFILE, BUS, GUESTBOOK`
- `Type` — `GAINED, USED`

### 예외
- `AlreadyOwnedException` — 이미 보유한 상품을 다시 구매하려 할 때(409 Conflict로 매핑).
- `InsufficientPointException` — 포인트 부족(400 Bad Request로 매핑).
- `ItemNotFoundException` — 상품을 찾지 못했을 때(404 Not Found로 매핑). 단, 실제로 이 예외를 던지는 코드는 도메인/애플리케이션 계층에서 확인되지 않았다(확인 필요: 정의만 있고 사용처가 없을 가능성).

### 정책
- `OrderPolicy.orderPolicy(MakeOrderCommand command, Long itemId, Long price)` — 이미 보유한 상품이면 `AlreadyOwnedException`, 포인트가 부족하면 `InsufficientPointException`을 던지는 검증 로직. 순수 도메인 모델의 메서드는 아니고 `@Component`로 등록된 애플리케이션 계층 정책 클래스.

## 6. ERD 스키마 대조

`order_history` 테이블에 실제로 매핑되는 엔티티는 `domain/model/OrderHistory.java`(빈 클래스, 3장에서 확인된 대로 미사용)가 아니라 `infrastructure/persistence/OrderJpaEntity.java`다. `@Table(name = "order_history")`로 직접 매핑되어 있다.

### `order_history` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT NOT NULL (PK) | `OrderJpaEntity.id` (`@Id @GeneratedValue(IDENTITY)`) | |
| user_id | BIGINT NOT NULL | `OrderJpaEntity.userId` (`@Column(nullable = false)`) | |
| reason | ENUM('COMPLETED','REVIEW','PROFILE','BUY','GUESTBOOK') NULL | `OrderJpaEntity.reason` (`@Enumerated(STRING)`, `@Column(nullable = false)`) | DB는 NULL 허용인데 JPA는 `nullable = false`로 선언되어 있음 - 확인 필요. 또한 enum 값 자체도 코드와 불일치(아래 별도 서술) |
| type | ENUM("GAINED","USED") NULL | `OrderJpaEntity.type` (`@Enumerated(STRING)`, `@Column(nullable = false)`) | DB는 NULL 허용인데 JPA는 `nullable = false`로 선언되어 있음 - 확인 필요. enum 값 자체는 코드와 일치 |
| amount | BIGINT NULL | `OrderJpaEntity.amount` (`@Column(nullable = false)`) | DB는 NULL 허용인데 JPA는 `nullable = false` - 확인 필요 |
| item_id | BIGINT NOT NULL | `OrderJpaEntity.itemId` (`@Column(nullable = true)`) | DB는 NOT NULL인데 JPA는 `nullable = true`로 선언되어 있음(주석: "포인트 + 이면 itemId가 없음") - DB 제약과 반대 방향으로 불일치, 확인 필요 |
| created_at | DATETIME NULL | `OrderJpaEntity.createdAt` (`@Column(name = "created_at", nullable = false, updatable = false)`) | DB는 NULL 허용인데 JPA는 `nullable = false` - 확인 필요 |

### DB에 없는 JPA 필드
- 없음(추가로 `@Table`에 `uniqueConstraints = {user_id, item_id}` 제약이 JPA 쪽에만 선언되어 있고, 주어진 SQL에는 해당 UNIQUE 제약이 없음 - 확인 필요).

### enum 값 일치 여부
- `Type`(코드: `GAINED, USED`) — DB의 `type ENUM("GAINED","USED")`와 일치.
- `Reason`(코드: `COMPLETE, REVIEW, PROFILE, BUS, GUESTBOOK`) — DB의 `reason ENUM('COMPLETED','REVIEW','PROFILE','BUY','GUESTBOOK')`와 불일치. `COMPLETE`(코드) vs `COMPLETED`(DB), `BUS`(코드) vs `BUY`(DB). `@Enumerated(EnumType.STRING)`으로 매핑되므로 이 값 차이는 실제 저장/조회 시 오류로 이어질 수 있는 문제로 보인다 - 확인 필요.
