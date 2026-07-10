# auth BC

## 1. 개요
auth 바운디드 컨텍스트는 사용자의 회원가입, 로그인(자체/소셜), 로그아웃, 이메일 인증, 임시 비밀번호 발급, 그리고 액세스 토큰(JWT) 발급·검증·재발급을 담당한다. 즉 "사용자가 누구인지 확인하고(인증), 로그인 상태를 토큰으로 관리하는" 역할을 맡고 있다.

## 2. 패키지 구조

### domain
- `event/` — `SignupCompletedEvent` (회원가입 완료 이벤트, record)
- `exception/` — `AuthExceptionHandler`(전역 예외 처리기), `DuplicateEmailException`, `EmailNotVerifiedException`, `EmailSendException`, `InactiveUserException`, `InvalidCredentialsException`, `InvalidTokenException`, `InvalidVerificationCodeException`, `MissingTokenException`, `OAuthInvalidCodeException`, `OAuthTokenException`, `TempPasswordExpiredException`, `UserNotFoundException`
- `model/` — `User`(핵심 엔티티), `UserOauth`(소셜 로그인 연동 정보), `Category`/`Provider`/`Role`/`Status`(Enum)
- `repository/` — `UserRepository`, `UserOauthRepository` (순수 인터페이스, 프레임워크 의존 없음)

### application
- `command/` — `EmailSendCommand`, `EmailVerifyCommand`, `LoginCommand`, `LogoutCommand`, `OAuthUserInfoCommand`, `SignupCommand`, `SocialLoginCommand` (모두 record)
- `policy/` — `SignupPolicy` (이메일 중복·인증 여부 검사)
- `port/` — `BlacklistPort`, `EmailCodePort`, `EmailSendPort`, `LoadUserPort`, `OAuthClientPort`, `PasswordEncodePort`, `RedisRefreshTokenPort`, `TokenProviderPort`, `UpdatePasswordPort`
- `service/` — `AuthCommandService`(회원가입/로그인/로그아웃/소셜로그인/이메일발송/임시비번 구현), `AuthQueryService`(이메일 인증코드 검증 구현), `RefreshService`(리프레시 토큰으로 액세스 토큰 재발급)
- `usecase/` — `AuthCommandUsecase`, `AuthQueryUsecase`, `NewTokenUsecase` (인터페이스)

### infrastructure
- `email/` — `EmailCodeAdapter`(Redis에 인증코드/인증여부/임시비번여부 저장), `EmailSendAdapter`(JavaMailSender로 실제 메일 발송)
- `exception/` — `ExpiredJwtCustomException`, `InvalidJwtCustomException`, `InvalidRefreshTokenException`
- `handler/` — `CustomAccessDeniedHandler`(403 처리 + 접근로그 기록), `CustomAuthenticationEntryPoint`(401 처리)
- `jwt/` — `BlacklistAdapter`(로그아웃된 액세스 토큰 블랙리스트, Redis), `JwtAuthenticationFilter`(요청마다 토큰 검증하는 필터), `JwtTokenProvider`(JWT 생성/검증/파싱 구현체)
- `oauth/` — `GoogleOAuthClient`, `KakaoOAuthClient`, `NaverOAuthClient` (각 소셜 API 호출 어댑터)
- `persistence/` — `RedisRefreshTokenAdapter`, `SpringDataAuthUserRepository`, `SpringDataUserOauthRepository`, `UserJpaEntity`, `UserOauthJpaEntity`, `UserOauthRepositoryAdapter`, `UserRepositoryAdapter`
- `security/` — `CustomUserDetails`, `CustomUserDetailsService`, `PasswordEncodeAdapter`

### presentation
- `api/` — `AuthController`
- `api/request/` — `EmailSendRequest`, `EmailVerifyRequest`, `LoginRequest`, `SignupRequest`, `SocialLoginRequest`
- `api/response/` — `AuthResponseCode`, `AuthResponseMessage`, `EmailSendResponse`, `EmailVerifyResponse`, `LoginResponse`, `NewTokenResponse`

## 3. 진행 상태

### 구현되어 있는 기능
- 자체 회원가입(이메일 인증 필수), 로그인, 로그아웃
- 카카오/구글/네이버 소셜 로그인 (인가코드 → 액세스토큰 → 유저정보 조회 2단계 호출)
- 이메일 인증코드 발송/검증 (Redis TTL 기반)
- 임시 비밀번호 발급 및 이메일 발송 (3분 만료)
- JWT 액세스/리프레시 토큰 발급, 검증, 재발급, 블랙리스트 처리
- 로그인/로그아웃/403 발생 시 admin BC의 `AccessLogRepository`를 호출해 접근로그 기록 (코드 주석상 "auth BC 담당자 승인, 예외적 BC 간 참조/수정"이라고 명시되어 있음 — BC 경계를 넘는 의도적 예외로 보임)

### 비어있거나 미완성으로 보이는 부분
- `UserRepository`에는 `register`, `existsByEmail`만 있고 조회 관련 기능은 `LoadUserPort`가 분리해서 담당함. 이는 포트 분리 설계로 보이며 미완성이라기보다 의도된 구조로 판단됨.
- `User` 도메인 모델은 필드 대부분이 `final`이고 상태 변경(예: status 변경, point 변경)을 위한 비즈니스 메서드가 보이지 않음 — 정지/차단 등 상태 변경은 다른 BC(admin/user 쪽)에서 처리하는 것으로 추정됨(확인 필요).
- `InactiveUserException` 처리 로직에서 `Status.REJECTED`인 경우도 로그인을 허용하도록 되어 있고, 관련 주석(`//case REJECTED -> ...`)이 주석 처리되어 있음 — REJECTED 상태의 안내 메시지 처리가 임시로 막혀 있는 것으로 보임(확인 필요).

### 코드 내 주석 (TODO/FIXME는 없었으나, 특이 주석 인용)
- `LoginCommand.java`, `LogoutCommand.java`: `// [MS-4 접근로그] admin BC 접근로그 저장을 위해 ip 필드 추가 (auth BC 담당자 승인, 예외적 BC 간 수정)`
- `AuthController.java`(login, logout): `// [MS-4 접근로그] 리버스 프록시 뒤에서 getRemoteAddr()가 루프백 주소를 반환하는 문제 수정 (auth BC 담당자 협의됨)`
- `CustomAccessDeniedHandler.java`: `// [MS-4 접근로그] 리버스 프록시 뒤에서 getRemoteAddr () 가 루프백 주소를 반환하는 문제 수정 (auth BC 담당자 수영님과 협의됨)`
- `AuthCommandService.java`(login): `// case REJECTED -> "강사 신청이 반려되었습니다. 증빙자료를 다시 제출해주세요.";` (주석 처리되어 사용되지 않는 상태)
- `JwtTokenProvider.java` 파일 맨 아래 블록 주석: `/* - 토큰 재사용 방지를 위해 Redis에 RefreshToken 저장 및 블랙리스트 처리 전략 필요 - key는 @PostConstruct에서 디코딩/변환 → @Value만 사용할 경우 Spring Context 순서에 따라 NullPointer 발생 가능 */`
- 실제 TODO/FIXME 키워드 주석은 발견되지 않음.

## 4. API 목록

컨트롤러 클래스 레벨 매핑: `@RequestMapping("/api/v1/auth")` (`AuthController`)

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| POST | /api/v1/auth/signup | AuthController.signup | 학생/강사 자체 회원가입. 가입 시 role은 STUDENT로 고정, 이후 강사 신청 과정에서 TEACHER로 변경 (Javadoc/@Operation 인용) |
| POST | /api/v1/auth/login | AuthController.login | 자체 로그인, 성공 시 토큰 발급 (@Operation 인용) |
| POST | /api/v1/auth/email/send | AuthController.emailSend | 회원가입 시 이메일 중복 확인 및 본인 인증을 위한 인증코드 발송 (@Operation 인용) |
| POST | /api/v1/auth/email/verify | AuthController.emailVerify | 서버가 보낸 인증코드와 사용자가 입력한 값 일치 여부 확인 (@Operation 인용) |
| POST | /api/v1/auth/password/temp | AuthController.tempPasswordSend | 랜덤 8자리 임시 비밀번호를 생성해 이메일로 전송하고 DB 비밀번호를 갱신, 3분 내 마이페이지에서 변경 필요 (@Operation 인용) |
| POST | /api/v1/auth/kakaologin | AuthController.kakaoLogin | 카카오 로그인을 위한 api (@Operation 인용) |
| POST | /api/v1/auth/googlelogin | AuthController.googleLogin | 구글로그인을 위한 api (@Operation 인용) |
| POST | /api/v1/auth/naverlogin | AuthController.naverLogin | 네이버 로그인을 위한 api (@Operation 인용) |
| POST | /api/v1/auth/logout | AuthController.logout | 로그아웃을 위한 api, redis에서 refresh 토큰 값 제거를 통해 로그아웃 진행 (@Operation 인용) |
| POST | /api/v1/auth/newtoken | AuthController.newToken | 새로운 액세스 토큰 발급해주는 api, 사용자가 연장 버튼 누르면 새로운 토큰 발급 (@Operation 인용) |

## 5. 도메인 모델

### User (domain/model/User.java)
- 주요 필드: `id`, `email`, `password`, `name`, `nickname`, `profileImageUrl`, `role`(Role), `status`(Status), `category`(Category), `proof`, `point`, `doNotDisturb`, `suspensionCount`, `suspendedUntil`, `createdAt`, `updatedAt`, `deletedAt`, `isTempPwd`
- 모든 필드 `final`이며 setter 없음. 팩토리 메서드로만 생성/복원.
- 비즈니스(팩토리) 메서드:
  - `signup(email, password, name)`: 자체 회원가입 시 STUDENT/ACTIVE 상태, 기본 프로필 이미지, point 0, isTempPwd false로 생성
  - `oAuthRegister(email, name)`: 소셜 회원가입 시 password는 null, STUDENT/ACTIVE 상태로 생성 (카카오는 email이 null일 수 있음이 주석에 명시)
  - `restore(...)`: DB에서 조회한 값으로 객체 복원

### UserOauth (domain/model/UserOauth.java)
- 필드: `id`, `user`(User), `provider`(Provider), `providerId`, `createdAt`
- `create(user, provider, providerId)`: 신규 소셜 연동 생성
- `restore(...)`: DB 복원

### Enum
- `Category`: FITNESS, STUDY, COOK, BEAUTY, ART (강사가 선택한 가르칠 카테고리)
- `Provider`: NAVER, KAKAO, GOOGLE
- `Role`: STUDENT, TEACHER, ADMIN
- `Status`: ACTIVE, PENDING, REJECTED, BANNED, BLACK, DELETED (사용자 서비스 이용 권한 상태)

### infrastructure 하위 핵심 클래스 (jwt/oauth 등)
- `JwtTokenProvider` (infrastructure/jwt): `TokenProviderPort` 구현체. 액세스/리프레시 토큰 생성(`createAccessToken`, `createRefreshToken`, `createTempAccessToken`), 검증(`validateToken`), 인증객체 추출(`getAuthentication`), 토큰에서 id 추출(`getIdFromToken`), 잔여 유효시간 계산(`getRemainingMillis`)
- `JwtAuthenticationFilter` (infrastructure/jwt): 매 요청마다 1회 실행되는 `OncePerRequestFilter`. 블랙리스트 확인 → 토큰 검증 → 만료 시 리프레시 토큰으로 재발급 시도 → 실패 시 401 응답. `/ws-chat` 경로는 필터 제외
- `BlacklistAdapter` (infrastructure/jwt): 로그아웃 시 액세스 토큰을 Redis에 블랙리스트로 등록/조회
- `GoogleOAuthClient`, `KakaoOAuthClient`, `NaverOAuthClient` (infrastructure/oauth): `OAuthClientPort` 구현체. 인가코드로 액세스토큰 요청 → 그 토큰으로 유저정보(providerId, email, name) 조회하는 2단계 흐름 공통
