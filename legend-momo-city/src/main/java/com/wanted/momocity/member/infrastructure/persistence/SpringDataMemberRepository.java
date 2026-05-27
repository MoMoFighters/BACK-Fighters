package com.wanted.momocity.member.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/* comment.
    SpringDataMemberRepository 정리
    1. Spring Data JPA 가 런타임에 자동으로 구현 클래스를 만들어주는 저장소 인터페이스
    2. 위치 : member/infrastructure/persistence (인프라 계층)
    3. 누가 호출하는가? : MemberRepositoryAdapter 가 주입받아서 호출한다. (하지만 직접 외부 노출하지 않는다)
    4. 핵심 기능 : 메소드 이름만 규칙대로 적으면 Spring Data 가 SQL 을 자동 생성
    5. 도메인 MemberRepository 와의 차이점
        - MemberRepository : 우리 비즈니스 약속, 도메인 모델 Member 를 다룸
        - SpringDataMemberRepository : Spring 기술 인터페이스, JPA Entity MemberJpaEntity 를 다룸
        - 둘을 잇는 다리가 위에서 말한 MemberRepositoryAdapter 이다.
 */

// import 가 Spring 인 이유 : 이 파일은 인프라 계층인데, Spring 의존 OK이다. 도메인 계층의 MemberRepository 와 다른 작업을 한다.
public interface SpringDataMemberRepository extends JpaRepository<MemberJpaEntity, Long> {

    // Member 가 아닌 MemberJpaEntity 를 반환하는 이유
    // 이 인터페이스는 인프라 계층이기 때문에 도메인 모델을 모른다.
    // Adapter 가 받아서 MemberJpaEntity -> Member 변환하는 책임을 가지고있다.
    Page<MemberJpaEntity> findByRoleAndStatusAndDeletedAtIsNull(String role, String status, Pageable pageable);

    long countByRoleAndStatusAndDeletedAtIsNull(String role, String status);
}
