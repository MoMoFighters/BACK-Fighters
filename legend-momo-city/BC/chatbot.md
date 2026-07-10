# chatbot BC

## 1. 개요
chatbot BC는 강의 수강생이 질문을 하면 FAQ 매칭 또는 Gemini(생성형 AI) 응답을 통해 답변을 제공하고, 사용자별 일일 사용량(호출 횟수·토큰 사용량)을 관리하려는 목적의 바운디드 컨텍스트다. 다만 커밋 로그(`bbe8b8c [FEAT] 챗봇 관련 domain 구조 설계`, `f747777 [FEAT] 챗봇 AI 구조 설계`)가 보여주듯, 현재는 클린 아키텍처 계층별 "패키지/클래스 뼈대"만 설계된 상태이며 실제 로직은 전혀 구현되어 있지 않다.

## 2. 패키지 구조

### domain
- `domain.model` — `ChatbotDailyUsage`, `ChatbotQuestionLog`, `LectureSummary`
- `domain.exception` — `ChatbotDailyLimitExceededException`, `ChatbotLectureNotFoundException`
- `domain.repository` — `ChatbotDailyUsageRepository`, `ChatbotQuestionLogRepository` (도메인 리포지토리 인터페이스로 추정되나 현재는 `class`로 선언되어 있음, 확인 필요)

### application
- `application.port` — `FaqPolicyPort`, `GeminiClientPort`, `LectureInfoPort`, `ReviewInfoPort`
- `application.service` — `ChatbotQuestionService`, `ChatbotUsageService`
- `application.support` — `ChatbotPromptBuilder`, `SimilarQuestionMatcher`
- `application.usecase` — `ChatbotQuestionUseCase`, `ChatbotUsageUseCase`

### infrastructure
- `infrastructure.adapter` — `ChatbotQuestionLogRepositoryAdapter`, `GeminiClientAdapter`, `LectureInfoAdapter`, `ReviewInfoAdapter`
- `infrastructure.faq` — `FaqPolicyLoader`
- `infrastructure.persistence` — `ChatbotDailyUsageJpaRepository`, `ChatbotDailyUseageJpaEntity`, `ChatbotQuestionLogJpaEntity`, `ChatbotQuestionLogJpaRepository`

### presentation
- `presentation.api` — `ChatbotController`
- `presentation.api.common` — `ChatbotExceptionHandler`, `ChatbotResponseCode`
- `presentation.api.request` — `ChatbotQuestionRequest`
- `presentation.api.response` — `ChatbotQuestionResponse`, `ChatbotUsageResponse`

총 클래스 수: 32개 (domain 7 + application 10 + infrastructure 9 + presentation 6)

## 3. 진행 상태

### 실제로 확인한 사실
`chatbot` 패키지 하위 `.java` 파일 32개를 전부 Read로 직접 확인했다. **예외 없이 모든 클래스가 다음과 같은 형태의 빈 껍데기(스켈레톤)였다.**

```java
package com.wanted.momocity.chatbot.domain.model;

public class ChatbotQuestionLog {
}
```

- 필드 없음, 생성자 없음, 메서드 없음
- `@Entity`, `@RestController`, `@RequestMapping`, `@Repository`, `@Service` 등 스프링/JPA 어노테이션이 단 하나도 없음
- `interface`로 선언되어야 할 것으로 보이는 포트/리포지토리(`FaqPolicyPort`, `GeminiClientPort`, `ChatbotDailyUsageRepository` 등)도 전부 `public class`로만 선언됨 (확인 필요 — 추후 interface로 변경될 가능성)
- 파일 32개 중 32개 전부가 "패키지 선언 + 빈 클래스" 한 줄짜리 파일

### 구현되어 있는 기능
- 없음. 패키지·클래스 이름을 통한 설계 골격만 존재한다.

### 비어있거나 미완성으로 보이는 부분
- domain/application/infrastructure/presentation 4개 계층 전체가 미구현 상태
- `ChatbotController`에 `@RequestMapping`/`@GetMapping`/`@PostMapping` 등 매핑 자체가 없어 API 목록을 추출할 수 없음 (4번 항목 참고)
- JPA 엔티티(`ChatbotDailyUseageJpaEntity`, `ChatbotQuestionLogJpaEntity`)에 필드가 전혀 없어 ERD와 대조할 코드 필드가 존재하지 않음 (5번 항목 ERD 대조 참고)
- `ChatbotResponseCode`, `ChatbotExceptionHandler`도 내용 없이 클래스 이름만 존재

### TODO/FIXME 주석
- 코드 내 TODO/FIXME 주석 없음 (grep 결과 매치 0건)

## 4. API 목록
`ChatbotController.java` 파일 전체 내용은 다음과 같다.

```java
package com.wanted.momocity.chatbot.presentation.api;

public class ChatbotController {
}
```

`@RequestMapping`, `@GetMapping`, `@PostMapping` 등 매핑 어노테이션이 전혀 없으므로 추출 가능한 HTTP API가 없다.

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| (확인 필요) | (확인 필요) | (확인 필요) | 컨트롤러 본문이 비어 있어 API 없음 |

## 5. 도메인 모델

### ChatbotDailyUsage (`domain/model/ChatbotDailyUsage.java`)
- 필드: 없음
- 비즈니스 메서드: 없음
- 파일 전체가 `public class ChatbotDailyUsage { }` 뿐이다.

### ChatbotQuestionLog (`domain/model/ChatbotQuestionLog.java`)
- 필드: 없음
- 비즈니스 메서드: 없음
- 파일 전체가 `public class ChatbotQuestionLog { }` 뿐이다.

(참고로 `LectureSummary` 도메인 모델도 동일하게 빈 클래스다.)

### ERD 스키마 대조

두 JPA 엔티티(`ChatbotDailyUseageJpaEntity.java`, `ChatbotQuestionLogJpaEntity.java`) 역시 Read로 직접 확인한 결과, 필드가 전혀 없는 빈 클래스였다.

```java
package com.wanted.momocity.chatbot.infrastructure.persistence;

public class ChatbotDailyUseageJpaEntity {
}
```

```java
package com.wanted.momocity.chatbot.infrastructure.persistence;

public class ChatbotQuestionLogJpaEntity {
}
```

따라서 ERD의 모든 컬럼이 "엔티티에 없음" 상태다.

**chatbot_question_log ↔ ChatbotQuestionLogJpaEntity**

| DB 컬럼 | 타입 | 코드 필드명 | 일치 여부 | 비고 |
|---|---|---|---|---|
| id | BIGINT NOT NULL | 없음 | 불일치 | 엔티티에 필드 자체가 없음 |
| user_id | BIGINT NOT NULL | 없음 | 불일치 | 엔티티에 필드 자체가 없음 |
| lecture_id | BIGINT NOT NULL | 없음 | 불일치 | 엔티티에 필드 자체가 없음 |
| question | VARCHAR(100) NULL | 없음 | 불일치 | 엔티티에 필드 자체가 없음 |
| is_faq_matched | BOOLEAN NULL | 없음 | 불일치 | 엔티티에 필드 자체가 없음 |
| created_at | DATETIME NULL | 없음 | 불일치 | 엔티티에 필드 자체가 없음 |

**chatbot_daliy_usage ↔ ChatbotDailyUseageJpaEntity**

| DB 컬럼 | 타입 | 코드 필드명 | 일치 여부 | 비고 |
|---|---|---|---|---|
| id | BIGINT NOT NULL | 없음 | 불일치 | 엔티티에 필드 자체가 없음 |
| user_id | BIGINT NOT NULL | 없음 | 불일치 | 엔티티에 필드 자체가 없음 |
| usage_date | DATE NULL | 없음 | 불일치 | 엔티티에 필드 자체가 없음 |
| call_count | INT NULL | 없음 | 불일치 | 엔티티에 필드 자체가 없음 |
| token_used | INT NULL | 없음 | 불일치 | 엔티티에 필드 자체가 없음 |
| created_at | DATETIME NULL | 없음 | 불일치 | 엔티티에 필드 자체가 없음 |
| updated_at | DATETIME NULL | 없음 | 불일치 | 엔티티에 필드 자체가 없음 |

**이름 관련 이슈**
- DB 테이블명 자체가 `chatbot_daliy_usage`로 되어 있어 "daily"의 오타(daliy)로 보인다. 이는 ERD 원본의 문제이며, 코드 쪽 클래스명 `ChatbotDailyUseageJpaEntity` 또한 `Useage`(정확히는 `Usage`)로 오타가 있어, DB 오타와는 별개로 코드에도 자체적인 오타가 존재한다. 즉:
  - DB 테이블명: `chatbot_daliy_usage` (daliy 오타)
  - 코드 클래스명: `ChatbotDailyUseageJpaEntity` (Useage 오타, Usage가 맞음)
  - 두 오타의 위치가 다르므로 향후 어노테이션으로 실제 테이블을 매핑할 때 혼동 소지가 있다. (확인 필요 — 실제 `@Table(name=...)` 지정 시 어느 쪽 철자를 따를지 팀 논의 필요)
