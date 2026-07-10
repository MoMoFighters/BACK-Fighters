# global (공유 커널)

## 1. 개요

`global` 패키지는 특정 도메인(BC)의 업무 규칙을 담당하지 않는다. 실제 코드를 확인한 결과, 다른 BC(admin, auth, community 등)가 공통으로 가져다 쓰는 기술적 기반과 최소한의 공용 타입만 모아둔 "공유 커널(shared kernel)"이 맞다.

구체적으로 이 패키지가 제공하는 것은 다음과 같다.
- 공통 API 응답/예외 포맷(`ApiResponse`, `ApiErrorResponse`, `ApiExceptionHandler` 등) — presentation 계층 전역 규약
- S3 업로드/다운로드/Presigned URL 발급을 위한 포트(인터페이스)와 어댑터(구현체)
- 여러 BC가 공유하는 JPA 공통 상위 클래스(`BaseTimeEntity`), 도메인 이벤트 공통 인터페이스, 도메인 규칙 위반 공통 예외
- Spring Security, Redis, WebSocket, 비동기 실행, Swagger 문서화 등 애플리케이션 전역 인프라 설정
- 포인트 적립/사용, 강의 카테고리(enum) 등 여러 BC에서 함께 쓰는 최소 공용 계약

## 2. 패키지 구조

### domain
- `domain/common/event` — `DomainEvent` (인터페이스): 모든 도메인 이벤트가 구현해야 하는 공통 계약. `occurredAt(): Instant` 메서드 하나만 가짐.
- `domain/common/exception` — `DomainRuleViolationException` (RuntimeException 상속): 도메인 규칙 위반을 표현하는 공통 예외.
- `domain/model` — `Category` (enum): FITNESS, STUDY, COOK, BEAUTY, ART. 각 값은 S3에 저장된 프로필 이미지 URL을 상수로 가짐.

### application
- `application/point` — `AddOrderHistory`(포인트 내역 저장 포트), `PointChange`(포인트 사용/획득 포트). 둘 다 인터페이스만 존재, 구현체는 global 밖(다른 BC의 infrastructure)에 있을 것으로 보임(확인 필요).
- `application/s3` — `S3DownloadPort`, `S3PresignedUrlPort`, `S3UploadPort`. 모두 인터페이스.

### infrastructure
- `infrastructure/aop` — `GlobalFlowLoggingAspect`(요청 흐름을 traceId 기준으로 계층별 로깅), `MdcTaskDecorator`(@Async 스레드로 MDC traceId 전파)
- `infrastructure/cloudfront` — `CloudFrontProperties`, `CloudFrontUrlConverter`(S3 key를 CloudFront URL로 변환)
- `infrastructure/config` — `AsyncConfig`/`AsyncProperties`(도메인 이벤트용 비동기 실행기), `JpaAuditingConfig`(@EnableJpaAuditing), `OpenApiConfig`(Swagger), `RedisConfig`(캐시 매니저·RedisTemplate), `S3Config`(S3Client/S3Presigner Bean), `SecurityConfig`(시큐리티 필터체인·CORS), `TopicSubscriptionInterceptor`(웹소켓 STOMP 인증/구독 관리), `WebClientConfig`, `WebMvcConfig`(현재 내용 없음), `WebSocketConfig`(STOMP 엔드포인트/브로커 설정)
- `infrastructure/metrics` — `MetricsAop`(여러 BC의 서비스 메서드 실행시간을 AOP로 계측), `MomoMetrics`(Micrometer Timer/Counter 등록·기록)
- `infrastructure/persistence` — `BaseTimeEntity`(createdAt/updatedAt 공통 매핑)
- `infrastructure/s3` — `S3DownloadAdapter`, `S3PresignedUrlAdapter`(viewing BC의 `S3Port`도 함께 구현), `S3UploadAdapter`
- `infrastructure/util` — `ClientIpResolver`(X-Forwarded-For 기반 클라이언트 IP 조회 유틸)

### presentation
- `presentation/api/common` — `ApiResponse`(성공 응답 표준 포맷), `ApiErrorResponse`(에러 응답 표준 포맷), `ApiResponseCode`(공통 응답 코드 상수), `ApiResponseMessage`(공통 응답 메시지 상수), `ApiExceptionHandler`(`@RestControllerAdvice`, 전역 예외를 표준 에러 응답으로 변환)

## 3. 진행 상태

**구현되어 있는 기능**
- 공통 API 성공/에러 응답 포맷과 전역 예외 처리(`ApiExceptionHandler`)는 비즈니스 규칙 위반, 검증 실패, 인증/인가 실패, 리소스 미존재, 예상치 못한 오류(500) 순으로 구체적 예외부터 처리하도록 구현 완료.
- S3 업로드/다운로드/Presigned URL 발급 어댑터 3종 모두 구현 완료, AWS SDK `S3Client`/`S3Presigner` 사용.
- Redis 캐시(게시글/캘린더/스트릭/관리자 회원목록별 TTL 개별 설정), Spring Security(JWT 필터 연동, CORS), 웹소켓(STOMP 인증·채팅/알림 채널 구독 관리) 설정 모두 구현되어 있고 실사용 중으로 보임.
- 여러 BC 서비스 메서드(S3 업로드, 블랙리스트 조회, 메시지 내역, 채팅방 목록, 친구 목록, 강의 등록/목록, 수강신청, 수강평 등록/삭제/목록, 진척도 조회, 챕터 등록, 관리자 회원 목록, 강사 승인)에 대한 실행시간 계측(`MetricsAop`+`MomoMetrics`)이 촘촘히 구현되어 있음.
- traceId 기반 전역 흐름 로깅(`GlobalFlowLoggingAspect`) 및 비동기 스레드로 MDC 전파(`MdcTaskDecorator`) 구현.

**비어있거나 미완성으로 보이는 부분**
- `WebMvcConfig` 클래스는 본문이 전부 주석으로 처리되어 있어 실질적으로 빈 클래스임(`WebMvcConfigurer`만 구현, Bean 없음). CORS 설정은 현재 `SecurityConfig`의 `corsConfigurationSource()`가 담당하는 것으로 보임(확인 필요: 중복/이전 흔적일 가능성).
- `application/point`의 `AddOrderHistory`, `PointChange`는 인터페이스만 있고, global 패키지 내에는 구현체(Adapter)가 없음. 구현체가 어느 BC(order 등)에 있는지는 이 범위 밖이라 확인 필요.
- `SecurityConfig` 주석에 "JWT 인증 필터(JwtAuthenticationFilter)는 아직 없다"는 문구가 있으나, 실제 코드에는 이미 `.addFilterBefore(new JwtAuthenticationFilter(...), ...)`가 적용되어 있어 주석이 과거 상태를 그대로 남긴 것으로 보임(확인 필요: 주석 최신화 필요).

**코드 내 TODO/FIXME 원문 인용**
- `WebMvcConfig.java` (주석 처리된 코드 내부): `// TODO 인프라 / 프론트 도메인 결정 후 운영 Origin 추가`
- `SecurityConfig.java` (클래스 상단 주석, 실제 TODO 태그는 아니지만 작업 지시 성격): `아래 TODO 위치에 .addFilterBefore(...) 로 끼워 넣고, 보호 경로 정책을 강화한다.`

## 4. API 목록

`global/presentation/api` 하위를 grep(`@RestController`, `@Controller`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`)으로 전부 확인한 결과, 실제 HTTP 엔드포인트를 여는 컨트롤러는 존재하지 않는다.

`common` 패키지에 있는 것은 컨트롤러가 아니라:
- `ApiExceptionHandler` — `@RestControllerAdvice`(전역 예외 어드바이스, URL 매핑 없음)
- `ApiResponse`, `ApiErrorResponse`, `ApiResponseCode`, `ApiResponseMessage` — 응답 포맷/상수 클래스(컨트롤러 아님)

따라서 이 BC는 자체 API 엔드포인트를 제공하지 않으며, 다른 BC의 컨트롤러들이 응답을 만들 때 가져다 쓰는 "규약"만 제공한다. (표는 해당 없음 — 엔드포인트 없음)

## 5. 도메인 모델

`domain/model`, `domain/common` 밑에는 풀full 엔티티가 아니라 공통 값/타입만 존재한다.

- **`DomainEvent`** (`domain/common/event`, 인터페이스)
  - 필드: 없음(인터페이스)
  - 메서드: `Instant occurredAt()` — 이벤트가 발생한 시각. 각 BC의 구체 이벤트 클래스(예: PaymentCompletedEvent 등, global 밖)가 이 인터페이스를 구현하는 구조로 보임(확인 필요, global 안에는 구현체 없음).

- **`DomainRuleViolationException`** (`domain/common/exception`, RuntimeException 상속)
  - 생성자 2개: `(String message)`, `(String message, Throwable cause)`
  - 도메인/애플리케이션 계층에서 던지고, `ApiExceptionHandler.handleDomainRuleViolation()`이 받아 HTTP 400 + `ApiResponseCode.DOMAIN_RULE_VIOLATION`으로 변환.

- **`Category`** (`domain/model`, enum)
  - 값: `FITNESS`, `STUDY`, `COOK`, `BEAUTY`, `ART`
  - 필드: `categoryProfileImage`(String, 각 값의 S3 프로필 이미지 URL)
  - 메서드: `getCategoryProfileImage()`
