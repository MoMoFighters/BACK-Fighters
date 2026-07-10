# store BC

## 1. 개요

store(상점) 바운디드 컨텍스트는 사용자가 자신의 포인트로 살 수 있는 상품(예: 프로필 꾸미기 아이템) 목록을 관리하는 역할을 맡고 있다. 현재 코드상으로는 "상품 목록을 페이지 단위로 조회하는 기능"만 실제로 구현되어 있고, 상품을 실제로 구매(주문)하는 처리는 order(주문) BC 쪽에서 이 BC가 제공하는 포트(연결 창구)를 통해 이루어진다.

## 2. 패키지 구조

### domain (도메인 계층 - 핵심 규칙과 데이터 모양)
- `domain.model`
  - `Store` : 상점에 등록된 상품 하나를 표현하는 엔티티(불변 객체)
  - `StoreListResult` : 상품 목록 조회 결과를 담는 값객체(record)
  - `CheckIsOrderedResult` : 상품 이름으로 id/url을 조회한 결과를 담는 값객체(record)
  - `Type` : 상품 종류를 나타내는 enum
- `domain.repository`
  - `StoreRepository` : 상품 조회를 위한 저장소 인터페이스(포트)
- `domain.exception`
  - `ItemNotFoundException` : 상품을 찾지 못했을 때 던지는 예외
  - `ItemNotOwnedException` : 사용자가 보유하지 않은 상품을 사용하려 할 때 던지는 예외
  - `StoreExceptionHandler` : 위 두 예외를 HTTP 응답으로 변환하는 전역 예외 처리기(`@RestControllerAdvice`)

### application (응용 계층 - 유스케이스와 포트)
- `application.usecase`
  - `StoreQueryUsecase` : 상품 목록 조회 기능의 인터페이스. `getProductList(Long userId, int page, int size)` 메서드 하나만 있음
  - `StoreCommandUsecase` : 상품에 대한 변경(구매 등) 기능의 인터페이스인데, **현재 메서드가 하나도 없는 빈 인터페이스**
- `application.service`
  - `StoreQueryService` : `StoreQueryUsecase`의 구현체. 상품 목록 + 사용자 포인트 + 보유 여부까지 함께 조립해서 반환
  - `StoreCommandService` : `StoreCommandUsecase`의 구현체인데, **본문이 완전히 비어 있음**
- `application.port`
  - `GetUserPointPort` : 사용자 포인트를 가져오기 위한 포트 (user BC 쪽 구현을 기대하는 것으로 보임, 확인 필요)
  - `GetUserOwnedItemIdPort` : 사용자가 보유한 상품 id 목록을 가져오기 위한 포트
  - `CheckIsOrderedPort` : 특정 상품을 특정 사용자가 구매했는지 확인하는 포트 (구현체는 store 패키지 안에 보이지 않음, 확인 필요 — order BC 쪽 구현 추정)

### infrastructure (인프라 계층 - DB 연동, 다른 BC와의 실제 연결)
- `infrastructure.persistence`
  - `StoreJpaEntity` : `store` 테이블에 매핑되는 JPA 엔티티 (id, price, url, type, name, createdAt)
  - `SpringDataStoreRepository` : Spring Data JPA 리포지토리. 이름으로 찾기, 타입으로 전체 찾기, 이름으로 id/url 찾기 쿼리 제공
  - `StoreRepositoryAdapter` : `domain.repository.StoreRepository`를 구현하는 어댑터. 실제로는 `SpringDataStoreRepository`를 감싸서 사용
- `infrastructure.adapter`
  - `GetAllProductAdapter` : **order BC의 `GetAllProductPort`를 구현**. store 테이블에서 PROFILE 타입 상품 전체를 조회해 order BC에 넘겨줌
  - `GetItemUrlPortAdapter` : **user BC의 `GetItemUrlPort`를 구현**. 상품 이름으로 존재 여부/구매 여부를 확인한 뒤 url을 돌려줌
  - `LoadItemAdapter` : **order BC의 `LoadItemPort`를 구현**. 상품 이름으로 id/price를 조회해 order BC에 넘겨줌

### presentation (표현 계층 - HTTP API)
- `presentation.api`
  - `StoreController` : 상품 목록 조회 API 하나만 존재
- `presentation.api.common`
  - `StoreResponseCode` : 응답 코드 상수 (`PURCHASE_SUCCESS`, `LIST_FOUND_SUCCESS` — 이 중 구매 관련 코드는 실제로 쓰이는 곳이 없음, 확인 필요)
  - `StoreResponseMessage` : 응답 메시지 상수 (`PRODUCT_LIST_FETCHED`, `PURCHASE_SUCCESS` — 마찬가지로 구매 메시지는 미사용으로 보임)
- `presentation.api.response`
  - `StoreListResponse` : 상품 목록 조회 API의 응답 DTO. 내부에 `Product`라는 중첩 record 있음

## 3. 진행 상태

**구현되어 있는 기능**
- 상점 상품 목록을 페이지 단위(page, size)로 조회하는 기능 (`GET /api/v1/store/product/list`)
  - 조회 시 사용자의 현재 포인트, 전체 개수/페이지 수, 사용자가 이미 보유한 상품 id 집합까지 함께 계산해서 응답에 담아줌
- 다른 BC(order, user)가 store 데이터를 필요로 할 때 쓰는 어댑터 3개(상품 전체 조회, 아이템 로드, 아이템 url 조회)는 모두 구현되어 있음

**비어있거나 미완성으로 보이는 부분**
- `StoreCommandUsecase` : 메서드가 하나도 없는 빈 인터페이스
- `StoreCommandService` : 클래스 본문이 완전히 비어 있음 (필드, 메서드 전혀 없음). 상품 구매(주문) 같은 명령형 기능이 store BC 자체에는 아직 구현되어 있지 않은 것으로 보임
- `StoreResponseCode.PURCHASE_SUCCESS`, `StoreResponseMessage.PURCHASE_SUCCESS` : 코드/메시지 상수는 미리 만들어 놓았지만 실제로 참조하는 컨트롤러나 서비스 코드가 없음 (확인 필요 — 구매 API가 order BC 쪽에 있는지, 혹은 앞으로 store BC에 추가될 예정인지)
- `CheckIsOrderedPort`의 실제 구현체가 store 패키지 내에서 발견되지 않음 (확인 필요)
- `StoreRepository`의 `countProductList()`는 있지만 `Pageable` import(`domain.repository.StoreRepository`의 5번째 줄)가 실제로는 쓰이지 않고 있음 — 사용되지 않는 import로 보임

**TODO/FIXME 주석**
- 코드 안에서 TODO/FIXME 형태의 주석은 발견되지 않음
- 다만 `SpringDataStoreRepository.java` 25~27번째 줄에 주석으로 막아둔(사용하지 않는) 쿼리 메서드가 남아 있음:
  ```
  // 상품 이름으로 url 찾기
  //    @Query("SELECT s.url FROM StoreJpaEntity s WHERE s.name = :name")
  //    String findUrlByName(@Param("name") String name);
  ```

## 4. API 목록

컨트롤러 클래스 레벨 매핑: `@RequestMapping("/api/v1/store")` (StoreController)

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| GET | /api/v1/store/product/list | StoreController.getStoreProductList | 상점에 있는 모든 항목(상품) 목록을 페이지 단위로 조회 (Swagger `@Operation` summary 기준: "상점에 있는 모든 항목 조회") |

store BC 컨트롤러는 위 1개 뿐이며, 응답 코드로 200(성공), 401(인증 실패)이 Swagger 문서에 명시되어 있음.

## 5. 도메인 모델

### Store (엔티티)
- 필드: `id`(Long), `price`(Long), `url`(String), `type`(Type), `name`(String), `createdAt`(LocalDateTime)
- 모두 `final` 필드 + `@Getter`만 있는 불변 객체. 생성자 하나만 있고, 비즈니스 메서드는 없음 (단순 데이터 보관 역할)

### Type (enum)
- 값: `PROFILE`
- 현재는 프로필 관련 상품 하나만 존재. `SpringDataStoreRepository`의 주석("추후에 profile말고 다른 type이 생기게 될 경우를 고려한 작업")을 보면 앞으로 다른 타입이 추가될 것을 염두에 두고 설계된 것으로 보임

### StoreListResult (값객체, record)
- 필드: `stores`(List<Store>), `point`(Long, 사용자 포인트), `page`(int), `size`(int), `totalElements`(long), `totalPages`(int), `ownedItemIds`(Set<Long>, 사용자가 보유한 상품 id 집합)
- 별도 비즈니스 메서드 없음. 조회 결과를 한 번에 묶어 전달하는 용도

### CheckIsOrderedResult (값객체, record)
- 필드: `id`(Long), `url`(String)
- 상품 이름으로 id와 url을 함께 조회할 때 쓰는 결과 객체. 비즈니스 메서드 없음

### 예외 클래스
- `ItemNotFoundException` : 존재하지 않는 상품을 조회했을 때 (HTTP 404로 변환, 코드 `STORE_ITEM_NOT_FOUND`)
- `ItemNotOwnedException` : 보유하지 않은 상품을 사용하려 했을 때 (HTTP 403으로 변환, 코드 `STORE_ITEM_NOT_OWNED`)

## 6. ERD 스키마 대조

`StoreJpaEntity`(store/infrastructure/persistence/StoreJpaEntity.java) 기준으로 대조함.

### `store` 테이블
| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT | id (Long) | |
| price | BIGINT, NULL 허용 | price (Long) | DB는 NULL 허용인데 JPA는 `@Column(nullable = false)`로 되어 있음 - 확인 필요 |
| url | VARCHAR(500), NULL 허용 | url (String) | |
| name | VARCHAR(500), NULL 허용 | name (String) | DB는 NULL 허용인데 JPA는 `@Column(nullable = false, unique = true)`로 되어 있음 - 확인 필요 |
| created_at | DATETIME, NULL 허용 | createdAt (LocalDateTime) | DB는 NULL 허용인데 JPA는 `@Column(nullable = false, updatable = false)`로 되어 있음 - 확인 필요 |

### DB에 없는 JPA 필드
- `type` (Type enum, `@Enumerated(EnumType.STRING)`으로 매핑됨) : 주어진 SQL의 `store` 테이블 정의에는 `type` 컬럼이 없음. 문서 2장에서 언급한 `Type`(PROFILE 등) enum이 실제로 어느 컬럼에 저장되는지 확인 필요
