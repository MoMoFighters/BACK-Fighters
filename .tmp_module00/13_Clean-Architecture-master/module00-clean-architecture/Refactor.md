# Refactor 정리

## 목적
- `learning` 컨텍스트가 `enrollment`의 Repository를 직접 참조하던 구조를, 요구사항의 Port/Adapter 구조로 정리.
- 학습 컨텍스트는 "활성 수강 여부(boolean)"만 의존하도록 경계 축소.

## 수정된 위치

### 1) Learning Policy 의존 방향 변경
- 파일: `src/main/java/com/wanted/cleanarchitecture/learning/application/policy/LearningAccessPolicy.java`
- 변경 전:
  - `EnrollmentRepository`를 직접 주입받아 `findActiveEnrollment(...)` 호출.
- 변경 후:
  - `EnrollmentAccessPort`를 주입받아 `hasActiveEnrollment(...)` 호출.
  - 결과가 `false`면 `DomainRuleViolationException` 발생.

핵심 변경 코드 개념:
- Before: `learning -> enrollment.domain.repository`
- After: `learning -> learning.application.port -> learning.infrastructure.adapter -> enrollment.domain.repository`

### 2) Learning Port 스켈레톤 확정
- 파일: `src/main/java/com/wanted/cleanarchitecture/learning/application/port/EnrollmentAccessPort.java`
- 조치:
  - TODO/실습 주석 상태였던 포트를 정답 인터페이스로 확정.
  - 메서드:
    - `boolean hasActiveEnrollment(Long userId, Long courseId);`

### 3) Enrollment Access Adapter 신규 추가
- 파일: `src/main/java/com/wanted/cleanarchitecture/learning/infrastructure/enrollment/EnrollmentAccessAdapter.java`
- 역할:
  - `EnrollmentAccessPort` 구현체.
  - 내부에서 `EnrollmentRepository.existsActiveEnrollment(userId, courseId)` 호출해 boolean 반환.
- 효과:
  - Learning Application 계층이 Enrollment 영속성 세부사항(JPA/Repository 메서드)에 직접 결합되지 않음.

## 리팩터링 결과
- Bounded Context 연결이 Port/Adapter 규칙에 맞게 정리됨.
- Application Policy가 필요한 사실(활성 수강 여부)만 사용하도록 단순화됨.
- 이후 Enrollment 조회 방식이 바뀌어도 Learning Policy 변경 없이 Adapter만 교체 가능.

