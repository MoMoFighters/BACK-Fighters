# 프로젝트 요구사항 명세서

## 1. 문서 목적

이 문서는 DDD, EventStorming, Clean Architecture 수업에서 학생들이 함께 구현할 프로젝트 요구사항을 정리한 문서다.

수업 진행 방식은 다음과 같다.

- `Lecture.md`: Clean Architecture, DDD, EventStorming 이론 설명
- `Requirement.md`: 실제로 구현할 기능과 규칙 확인
- 코드: 요구사항을 계층별로 나누어 구현

이 프로젝트는 단순 CRUD 예제가 아니다. 목표는 EventStorming 결과를 바탕으로 Command, Aggregate, Domain Event, Bounded Context, Port, Adapter가 코드에서 어떻게 드러나는지 학습하는 것이다.

## 2. 프로젝트 주제

온라인 강의 플랫폼의 핵심 흐름을 구현한다.

주요 사용자는 다음과 같다.

- 교육자: 강의를 만들고, 섹션과 모듈을 구성하고, 강의를 공개한다.
- 학습자: 공개된 강의를 수강 신청하고, 강의 모듈을 완료한다.

전체 흐름은 다음과 같다.

```text
강의 생성
-> 섹션 추가
-> 모듈 추가
-> 강의 공개
-> 수강 신청
-> 모듈 완료
```

## 3. Bounded Context

프로젝트는 세 개의 Bounded Context로 나눈다.

### 3-1. Catalog Context

강의의 정의와 공개를 담당한다.

담당 기능:

- 강의 생성
- 강의 조회
- 섹션 추가
- 모듈 추가
- 강의 공개

중심 Aggregate:

- `Course`

주요 Domain Event:

- `CoursePublishedEvent`

### 3-2. Enrollment Context

수강 신청과 수강 상태를 담당한다.

담당 기능:

- 수강 신청
- 수강 신청 조회
- 중복 활성 수강 방지
- 공개된 강의에만 수강 신청 허용

중심 Aggregate:

- `Enrollment`

Catalog Context와의 연결:

- `CourseCatalogPort`

### 3-3. Learning Context

학습 진행과 모듈 완료를 담당한다.

담당 기능:

- 학습 진행 조회
- 모듈 완료 처리
- 활성 수강 상태인 사용자만 학습 가능

중심 Aggregate:

- `LearningProgress`

Enrollment Context와의 연결:

- `EnrollmentAccessPort`
- 수업 중 일부는 스켈레톤으로 제공하고 학생 실습으로 완성한다.

## 4. 공통 아키텍처 요구사항

모든 기능은 다음 계층 구조를 따른다.

```text
presentation
application
domain
infrastructure
```

### 4-1. Presentation 계층 요구사항

역할:

- HTTP 요청을 받는다.
- Request DTO를 검증한다.
- Request DTO를 Application Command로 변환한다.
- UseCase를 호출한다.
- API 응답을 반환한다.
- Swagger 문서에 Endpoint 목적을 명확히 남긴다.

금지 사항:

- 비즈니스 규칙 판단 금지
- JPA Entity 직접 사용 금지
- Repository 직접 호출 금지
- Domain Event 직접 발행 금지

### 4-2. Application 계층 요구사항

역할:

- 하나의 UseCase 흐름을 조립한다.
- 트랜잭션 경계를 가진다.
- Aggregate를 조회하고 도메인 행위를 호출한다.
- 저장 후 Domain Event를 발행한다.
- 여러 Aggregate나 다른 Context 조회가 필요한 규칙은 Policy로 분리한다.

금지 사항:

- Aggregate 내부 규칙을 Service의 if문으로 흩어놓지 않는다.
- Controller나 JPA Entity를 의존하지 않는다.

### 4-3. Domain 계층 요구사항

역할:

- 핵심 비즈니스 규칙을 가진다.
- Aggregate는 자기 상태로 판단 가능한 규칙을 직접 검증한다.
- 중요한 비즈니스 결과는 Domain Event로 기록한다.
- Repository는 구현체가 아니라 Port로 정의한다.

금지 사항:

- Spring MVC, JPA, MySQL, Swagger, Servlet API 의존 금지
- `@Entity`, `@Table`, `JpaRepository` 사용 금지
- HTTP 응답 코드 판단 금지

### 4-4. Infrastructure 계층 요구사항

역할:

- JPA Entity와 Spring Data Repository를 둔다.
- Domain Repository Port를 구현한다.
- Domain Model과 JPA Entity를 변환한다.
- 외부 Context 조회 Adapter를 구현한다.
- JPA 프록시와 Lazy Loading 문제를 Adapter 내부에서 처리한다.

금지 사항:

- 비즈니스 규칙을 Infrastructure에 숨기지 않는다.
- Controller로 JPA Entity를 직접 반환하지 않는다.

## 5. 기능 요구사항

## 5-1. 강의 생성

### 설명

교육자는 새로운 강의 초안을 생성할 수 있다.

### Endpoint

```http
POST /api/courses
```

### Request

```json
{
  "authorId": 100,
  "title": "Clean Architecture 101",
  "description": "DDD와 Clean Architecture를 함께 배우는 강의"
}
```

### Response

```json
{
  "code": "COURSE_CREATED",
  "message": "Course created.",
  "data": {
    "courseId": 1
  }
}
```

### 비즈니스 규칙

- `authorId`는 필수다.
- `title`은 필수이며 빈 문자열일 수 없다.
- 생성된 강의의 상태는 `DRAFT`다.

### 구현 위치

- Request DTO: `catalog.presentation.api.request.CreateCourseRequest`
- Command: `catalog.application.command.CreateCourseCommand`
- UseCase: `catalog.application.usecase.CourseCommandUseCase`
- Application Service: `catalog.application.service.CourseCommandService`
- Aggregate: `catalog.domain.model.Course`
- Repository Port: `catalog.domain.repository.CourseRepository`
- JPA Adapter: `catalog.infrastructure.persistence.CourseRepositoryAdapter`

## 5-2. 강의 조회

### 설명

강의 ID로 강의 요약 정보를 조회한다.

### Endpoint

```http
GET /api/courses/{courseId}
```

### Path Variable

- `courseId`: 조회할 강의 ID

### Response

```json
{
  "code": "SUCCESS",
  "message": "Success.",
  "data": {
    "courseId": 1,
    "authorId": 100,
    "title": "Clean Architecture 101",
    "description": "DDD와 Clean Architecture를 함께 배우는 강의",
    "status": "DRAFT",
    "sectionCount": 0,
    "moduleCount": 0
  }
}
```

### 비즈니스 규칙

- 존재하지 않는 강의 ID로 조회하면 예외가 발생한다.

## 5-3. 섹션 추가

### 설명

초안 상태의 강의에 섹션을 추가한다.

### Endpoint

```http
POST /api/courses/{courseId}/sections
```

### Request

```json
{
  "title": "Introduction",
  "sectionOrder": 1
}
```

### 비즈니스 규칙

- 강의는 `DRAFT` 상태여야 한다.
- 섹션 제목은 필수다.
- `sectionOrder`는 1 이상이어야 한다.
- 같은 강의 안에서 `sectionOrder`는 중복될 수 없다.

### 구현 기준

- 중복 순서 검증은 `Course.addSection()`에서 수행한다.
- Controller나 Application Service에서 직접 중복 검증하지 않는다.

## 5-4. 모듈 추가

### 설명

특정 섹션에 학습 모듈을 추가한다.

### Endpoint

```http
POST /api/courses/{courseId}/sections/{sectionOrder}/modules
```

### Request

```json
{
  "title": "Why Boundaries Matter",
  "contentType": "TEXT",
  "moduleOrder": 1
}
```

### 비즈니스 규칙

- 강의는 `DRAFT` 상태여야 한다.
- 요청한 `sectionOrder`에 해당하는 섹션이 존재해야 한다.
- 모듈 제목은 필수다.
- `contentType`은 필수다.
- `moduleOrder`는 1 이상이어야 한다.
- 같은 섹션 안에서 `moduleOrder`는 중복될 수 없다.

### 구현 기준

- 섹션 존재 여부는 `Course.addModule()`에서 검증한다.
- 모듈 순서 중복은 `CourseSection.addModule()`에서 검증한다.

## 5-5. 강의 공개

### 설명

초안 상태의 강의를 공개한다.

### Endpoint

```http
POST /api/courses/{courseId}/publication
```

### 비즈니스 규칙

- 강의는 `DRAFT` 상태여야 한다.
- 강의에는 최소 하나 이상의 섹션이 있어야 한다.
- 강의에는 최소 하나 이상의 모듈이 있어야 한다.
- 공개에 성공하면 강의 상태는 `PUBLISHED`가 된다.
- 공개에 성공하면 `CoursePublishedEvent`가 기록된다.

### 구현 기준

- 공개 가능 여부는 `Course.publish()`에서 검증한다.
- `Course.publish()`는 `CoursePublishedEvent`를 기록한다.
- `CourseCommandService`는 저장 후 `pullDomainEvents()`로 이벤트를 꺼낸다.
- `CourseCommandService`는 `publishAll()`로 이벤트를 발행한다.

### 호출 흐름

```text
CourseController
-> PublishCourseCommand
-> CourseCommandService
-> CourseRepository.findById
-> Course.publish()
-> CourseRepository.save
-> course.pullDomainEvents()
-> publishAll(events)
-> CourseLifecycleEventHandler
```

## 5-6. 수강 신청

### 설명

학습자는 공개된 강의에 수강 신청할 수 있다.

### Endpoint

```http
POST /api/enrollments
```

### Request

```json
{
  "userId": 200,
  "courseId": 1
}
```

### Response

```json
{
  "code": "ENROLLMENT_CREATED",
  "message": "Enrollment created.",
  "data": {
    "enrollmentId": 1
  }
}
```

### 비즈니스 규칙

- `userId`는 필수다.
- `courseId`는 필수다.
- 공개된 강의에만 수강 신청할 수 있다.
- 같은 사용자는 같은 강의에 대해 활성 수강을 중복으로 가질 수 없다.
- 수강 신청이 생성되면 상태는 `ACTIVE`다.

### Bounded Context 연결 기준

`enrollment`는 `catalog.domain.model.Course`를 직접 사용하지 않는다.

대신 다음 Port를 사용한다.

```java
CourseCatalogPort
```

호출 흐름:

```text
EnrollmentCommandService
-> EnrollmentEligibilityPolicy
-> CourseCatalogPort
-> CatalogCourseAdapter
-> CourseRepository
```

### 구현 기준

- 공개 여부 확인은 `CourseCatalogPort`를 통해 수행한다.
- 중복 활성 수강 확인은 `EnrollmentRepository`를 통해 수행한다.
- 두 조건을 조합하는 규칙은 `EnrollmentEligibilityPolicy`가 담당한다.

## 5-7. 수강 신청 조회

### 설명

수강 신청 ID로 수강 상태를 조회한다.

### Endpoint

```http
GET /api/enrollments/{enrollmentId}
```

### Path Variable

- `enrollmentId`: 조회할 수강 신청 ID

### 비즈니스 규칙

- 존재하지 않는 수강 신청 ID로 조회하면 예외가 발생한다.

## 5-8. 모듈 완료 처리

### 설명

학습자는 자신이 수강 중인 강의의 모듈을 완료 처리할 수 있다.

### Endpoint

```http
PUT /api/learning/module-completions/{moduleId}
```

### Request

```json
{
  "userId": 200,
  "courseId": 1
}
```

### 비즈니스 규칙

- `userId`는 필수다.
- `courseId`는 필수다.
- `moduleId`는 필수다.
- 사용자는 해당 강의를 활성 수강 중이어야 한다.
- 진행 기록이 없으면 새로 생성한 뒤 완료 처리한다.
- 이미 완료된 모듈은 다시 완료할 수 없다.
- 완료 시 상태는 `COMPLETED`가 된다.

### 구현 기준

- 현재 수업에서는 `LearningAccessPolicy`에 직접 의존 구조가 남아 있다.
- 학생 실습으로 `EnrollmentAccessPort`를 사용하도록 변경한다.

실습 목표:

```text
LearningAccessPolicy
-> EnrollmentAccessPort
-> EnrollmentAccessAdapter
-> EnrollmentRepository
```

## 5-9. 학습 진행 조회

### 설명

학습 진행 ID로 진행 상태를 조회한다.

### Endpoint

```http
GET /api/learning/progresses/{progressId}
```

### Path Variable

- `progressId`: 조회할 학습 진행 ID

### 비즈니스 규칙

- 존재하지 않는 학습 진행 ID로 조회하면 예외가 발생한다.

## 6. Domain Event 요구사항

### 6-1. 기본 규칙

Domain Event는 이미 발생한 중요한 비즈니스 사실을 표현한다.

예:

```text
CoursePublishedEvent
```

Domain Event는 다음 기준을 따른다.

- 이름은 과거형으로 작성한다.
- Command와 구분한다.
- 후속 처리에 필요한 최소 정보만 담는다.
- 발생 시각 `occurredAt`을 가진다.

### 6-2. CoursePublishedEvent

발생 시점:

- `Course.publish()`가 성공했을 때

포함 데이터:

- `courseId`
- `occurredAt`

후속 처리 예:

- 로그 기록
- 검색 색인 갱신
- 공개 알림 발송
- 통계 적재

## 7. JPA 및 DB 요구사항

### 7-1. JPA Entity와 Domain Model 분리

다음은 반드시 분리한다.

| Domain Model | JPA Entity |
| --- | --- |
| `Course` | `CourseJpaEntity` |
| `CourseSection` | `CourseSectionJpaEntity` |
| `ContentModule` | `ContentModuleJpaEntity` |
| `Enrollment` | `EnrollmentJpaEntity` |
| `LearningProgress` | `LearningProgressJpaEntity` |

### 7-2. 메서드 역할 차이

Domain Model의 메서드:

- 업무 행위를 표현한다.
- 비즈니스 규칙을 검증한다.
- 도메인 이벤트를 기록한다.

예:

```java
course.publish(now);
course.addSection(title, sectionOrder);
course.addModule(sectionOrder, title, contentType, moduleOrder);
```

JPA Entity의 메서드:

- DB 컬럼 값을 변경한다.
- FK 연관관계를 맞춘다.
- JPA 컬렉션을 동기화한다.

예:

```java
entity.changeStatus(status);
entity.replaceSections(sections);
section.assignCourse(course);
```

### 7-3. Unique 제약

다음 중복은 DB에서도 방어한다.

- `course_sections`: `(course_id, section_order)`
- `content_modules`: `(section_id, module_order)`
- `enrollments`: `(user_id, course_id, status)`
- `user_progress`: `(user_id, module_id)`

## 8. Swagger 요구사항

모든 Controller Endpoint에는 Swagger 설명을 작성한다.

필수 항목:

- API 그룹 `@Tag`
- Endpoint 목적 `@Operation(summary, description)`
- Path Variable 설명 `@Parameter`
- Request/Response 필드 설명 `@Schema`

Swagger는 Presentation 계층에만 둔다.

Domain, Application 계층은 Swagger를 몰라야 한다.

## 9. 테스트 요구사항

### 9-1. Domain 단위 테스트

Spring 없이 순수 Java 객체로 테스트한다.

필수 테스트:

- 강의 제목이 없으면 생성할 수 없다.
- 섹션 순서는 중복될 수 없다.
- 모듈 순서는 같은 섹션 안에서 중복될 수 없다.
- 모듈 없는 강의는 공개할 수 없다.
- 공개 성공 시 `CoursePublishedEvent`가 기록된다.
- `pullDomainEvents()` 후 이벤트 목록은 비워진다.

### 9-2. Integration 테스트

전체 흐름을 검증한다.

필수 흐름:

```text
강의 생성
-> 섹션 추가
-> 모듈 추가
-> 강의 공개
-> 수강 신청
-> 모듈 완료
```

테스트는 H2 인메모리 DB를 사용한다.

## 10. 학생 실습 과제

수업 중 강사가 함께 구현하는 범위:

- `Course.publish()`
- `CoursePublishedEvent`
- `pullDomainEvents()`
- `publishAll()`
- `CourseCatalogPort`
- `CatalogCourseAdapter`
- Swagger 적용
- Request/Response DTO 분리

학생이 직접 완성할 범위:

- `EnrollmentCreatedEvent`
- `ModuleCompletedEvent`
- `EnrollmentAccessPort`
- `EnrollmentAccessAdapter`
- `LearningAccessPolicy` 리팩토링
- `Enrollment` 도메인 테스트
- `LearningProgress` 도메인 테스트
- 실패 시나리오 Application Service 테스트

## 11. 최종 완료 기준

프로젝트가 완료되었다고 판단하는 기준은 다음과 같다.

- Controller는 Request/Response와 UseCase 호출만 담당한다.
- Application Service는 UseCase 흐름과 Transaction 경계를 담당한다.
- Domain Model은 비즈니스 규칙을 직접 가진다.
- JPA Entity는 저장 모델로만 사용된다.
- 다른 Bounded Context는 Port를 통해 연결된다.
- Domain Event는 Aggregate가 기록하고 Application Service가 발행한다.
- Swagger에서 모든 Endpoint 목적을 확인할 수 있다.
- 테스트가 통과한다.

검증 명령:

```bash
.\gradlew.bat test
```

