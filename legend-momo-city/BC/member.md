# member BC

## 1. 개요

member 바운디드 컨텍스트는 `user` 테이블 한 행을 표현하는 회원 도메인 모델(`Member`)을 단독으로 소유하고, 회원의 역할(role)·상태(status)·관심사 카테고리(category)를 관리하는 영역이다. 다른 영역(강사, 신고, 관리자 등)이 회원 정보가 필요할 때는 이 BC가 제공하는 `MemberQueryService`(조회 전용 공개 창구)를 거쳐야 하며, `MemberRepository`나 `MemberJpaEntity`를 직접 참조할 수 없다.

`domain/package-info.java` 주석과 `Member.java` 상단 주석에 따르면, member BC와 user BC의 구분은 코드만으로는 명확히 드러나지 않는다. member BC는 `user` 테이블의 소유자로서 회원 상태 변경(관리자에 의한 상태 전이, 강사 승인/반려)이라는 "회원 계정 관리" 책임을 담당하는 것으로 보이며, user라는 이름의 별도 BC 코드는 이 경로 안에서는 발견되지 않았다(확인 필요).

## 2. 패키지 구조

| 계층 | 하위 패키지 | 대표 클래스 |
|---|---|---|
| domain | `domain.model` | `Member`, `MemberRole`, `MemberStatus`, `MemberCategory` |
| domain | `domain.repository` | `MemberRepository` (인터페이스) |
| application | `application.command` | `ChangeMemberStatusCommand` |
| application | `application.usecase` | `MemberCommandUseCase` (+ 중첩 record `MemberStatusChangeResult`) |
| application | `application.service` | `MemberCommandService`, `MemberQueryService` |
| infrastructure | `infrastructure.persistence` | `MemberJpaEntity`, `MemberRepositoryAdapter`, `SpringDataMemberRepository` |
| presentation | `presentation.api` | `MemberAdminController` |
| presentation | `presentation.api.request` | `ChangeMemberStatusRequest` |
| presentation | `presentation.api.response` | `ChangeMemberStatusResponse` |

각 계층 최상단에는 `package-info.java`가 있으며 계층 책임을 한 줄로 명시한다.
- domain: "회원 - 도메인 계층 (순수 비즈니스 규칙. 데이터베이스/HTTP 의존성 없음)."
- application: "회원 - 응용 계층 (유스케이스 조립, 트랜잭션 경계)."
- infrastructure: "회원 - 인프라 계층 (데이터베이스 어댑터, 외부 연동). user 테이블 주인."
- presentation 계층에는 `package-info.java`가 확인되지 않음.

## 3. 진행 상태

**구현되어 있는 기능**
- `Member` 도메인 모델의 생성자 검증(이메일/역할/상태 필수) 및 `restore()`를 통한 복원, 단순 getter 전체
- `MemberRole`(STUDENT/TEACHER/ADMIN), `MemberStatus`(ACTIVE/PENDING/REJECTED/BANNED/BLACK/DELETED), `MemberCategory`(HEALTH/STUDY/COOK/BEAUTY/ART) 3개 enum
- `MemberRepository` 인터페이스 시그니처(도메인 계약) 정의
- `MemberQueryService`의 조회 3개 메서드(`findById`, `findByRoleAndStatus`, `countByRoleAndStatus`)는 Repository 위임 형태로 실제 구현되어 있음
- `MemberJpaEntity`(user 테이블 매핑), `SpringDataMemberRepository`(Spring Data JPA 쿼리 메서드) 정의
- `MemberAdminController`의 API 골격, 요청/응답 DTO(`ChangeMemberStatusRequest`, `ChangeMemberStatusResponse`), 커맨드(`ChangeMemberStatusCommand`) 검증 로직

**비어있거나 미완성으로 보이는 부분**
- `Member.approveAsTeacher()`, `Member.rejectAsTeacher()`, `Member.changeStatusByAdmin(MemberStatus)` — 모두 본문이 `UnsupportedOperationException`만 던짐
- `MemberCommandService.changeStatus()` — 구현 없이 예외만 던짐
- `MemberRepositoryAdapter`의 `findById`, `findByRoleAndStatus`, `countByRoleAndStatus`, `save` 4개 메서드 전부 `UnsupportedOperationException`
- `MemberAdminController.changeStatus()` 컨트롤러 메서드 본문도 `UnsupportedOperationException`
- 즉 "회원 상태 변경(MS-6)" 흐름은 표현/응용/인프라 전 계층에서 시그니처만 잡혀 있고 실제 로직은 비어 있음

**TODO/FIXME 주석 원문 인용**
- `Member.java`: `throw new UnsupportedOperationException("TODO: 강사 승인 도메인 행위 (m03 우선순위 1)");`
- `Member.java`: `throw new UnsupportedOperationException("TODO: 강사 반려 도메인 행위 (m03 우선순위 1)");`
- `Member.java`: `throw new UnsupportedOperationException("TODO: 회원 상태 변경 도메인 행위 (m03 우선순위 3)");`
- `MemberCommandService.java`: `throw new UnsupportedOperationException("TODO: m03 우선순위 3 - 회원 상태 변경 구현");`
- `MemberRepositoryAdapter.java`: `throw new UnsupportedOperationException("TODO: m03 구현 - Member 단건 조회");`
- `MemberRepositoryAdapter.java`: `throw new UnsupportedOperationException("TODO: m03 구현 - role+status 페이징 조회");`
- `MemberRepositoryAdapter.java`: `throw new UnsupportedOperationException("TODO: m03 구현 - role+status 개수 조회");`
- `MemberRepositoryAdapter.java`: `throw new UnsupportedOperationException("TODO: m03 구현 - Member 저장");`
- `MemberAdminController.java`: `throw new UnsupportedOperationException("TODO: m03 우선순위 3 - MS-6 회원 상태 변경 컨트롤러 구현");`
- `MemberStatus.java` 주석: "module03 미구현 항목 : BANNED/BLACK 값은 enum 에 있지만 자동 정지 로직은 module04 에서 진행 예정."

## 4. API 목록

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| PATCH | /api/v1/admin/users/{userId}/status | MemberAdminController.changeStatus | 관리자가 회원 상태를 ACTIVE / BANNED / BLACK / DELETED 등으로 변경한다. (Swagger `@Operation` summary·description 인용, MS-6) |

클래스 레벨 `@RequestMapping("/api/v1/admin")` + 메서드 레벨 `@PatchMapping("/users/{userId}/status")` 조합. `@PreAuthorize("hasRole('ADMIN')")`가 클래스 전체에 걸려 있어 ADMIN 권한만 접근 가능. 단, 컨트롤러 메서드 본문은 `UnsupportedOperationException`을 던지므로 실제로는 동작하지 않는 상태(3번 항목 참조).

## 5. 도메인 모델

### Member (도메인 엔티티)
필드: `id`, `email`, `name`, `nickname`, `birth`, `profileImageUrl`, `role`(가변), `status`(가변), `category`, `proof`, `createdAt`, `updatedAt`. `id`~`updatedAt` 중 `role`과 `status`만 가변(`private`, final 아님)이고 나머지는 `final`이다.

생성자는 `private`이며 `email`, `role`, `status`가 null/blank이면 `DomainRuleViolationException`을 던진다(항상 유효한 객체 보장). 외부 생성 진입점은 정적 팩토리 `restore(...)`뿐이다(DB에서 읽어온 값으로 복원).

비즈니스 메서드(단순 getter/setter 아님, 모두 미구현 상태):
- `approveAsTeacher()` — PENDING → ACTIVE 전이 검증 + 상태 변경(role=TEACHER 유지) 의도, 현재는 예외만 던짐
- `rejectAsTeacher()` — PENDING → REJECTED 전이 검증 + 상태 변경(로그인 차단) 의도, 현재는 예외만 던짐
- `changeStatusByAdmin(MemberStatus newStatus)` — 관리자가 수동으로 임의 상태로 전이시키는 의도, 현재는 예외만 던짐

### MemberRole (enum)
값: `STUDENT`, `TEACHER`, `ADMIN`. `user.role` 컬럼과 매핑.

### MemberStatus (enum)
값: `ACTIVE`, `PENDING`, `REJECTED`, `BANNED`, `BLACK`, `DELETED`. `user.status` 컬럼과 매핑. 주석에 따르면 과거 `SUSPENDED` 값이 있었으나 폐지되어 `BANNED`(기간정지)와 `BLACK`(영구정지)으로 분리됐다고 기술되어 있다(확인 필요 — 폐지 이력은 주석 서술로만 확인, 실제 마이그레이션 코드는 이 경로에 없음).

### MemberCategory (enum)
값: `HEALTH`, `STUDY`, `COOK`, `BEAUTY`, `ART`. `user.category` 컬럼과 매핑, nullable(회원의 관심사 분류이므로 필수 아님).

### MemberJpaEntity (인프라 계층 저장 모델, 참고)
`Member`와는 별개 클래스로 `BaseTimeEntity`를 상속하며 `@Getter` 없이 직접 getter를 작성. `role`/`status`/`category`는 String 컬럼으로 저장하고 Adapter가 enum ↔ String 변환을 책임진다(변환 로직 자체는 미구현). `password`, `point`, `isPaid`, `doNotDisturb`, `deletedAt`, `isTempPWD` 등 도메인 모델 `Member`에는 없는 필드도 존재한다. `changeRole(String)`, `changeStatus(String)` 두 메서드가 있으나 주석상 "JPA 저장 모델 메소드(도메인 행위 아님)"로 명시되어 있다.
