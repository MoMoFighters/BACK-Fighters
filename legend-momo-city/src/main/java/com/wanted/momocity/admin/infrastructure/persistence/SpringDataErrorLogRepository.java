package com.wanted.momocity.admin.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/* comment.
    SpringDataErrorLogRepository 정리
    1. 역할 : Spring Data Jpa 가 런타임에 자동으로 구현체를 만들어주는 저장소 인터페이스이다.
    2. 위치 : 인프라 계층
    3. 누가 호출하는가 : ErrorLogRepositoryAdapter 가 주입받아 호출하게 된다. (외부에 직접적으로 노출 X)
    4. WHY 도메인 ErrorLogRepository 와 별도로 두는가
       → ErrorLogRepository (도메인) : 비즈니스 약속, ErrorLog 도메인 모델을 다룬다.
       → SpringDataErrorLogRepository (인프라) : Spring 기술 인터페이스, ErrorLogJpaEntity 를 다룬다.
    5. 메서드 2개 시그니처 의미
       - findAllByOrderByOccurredAtDesc : findAll + OrderBy + OccurredAt + Desc  정렬만 진행하고 필터는 존재하지 않는다.
       - findAllByLevelOrderByOccurredAtDesc : 반대로 level 필터 + 정렬이 있다.
 */
public interface SpringDataErrorLogRepository extends JpaRepository<ErrorLogJpaEntity, Long> {

    // 최근 N개 (occurredAt DESC) - Adapter 에서 PageRequest.of(0, limit) 으로 호출
    List<ErrorLogJpaEntity> findAllByOrderByOccurredAtDesc(Pageable pageable);

    // 특정 레벨의 최근 N개 (occurredAt DESC)
    List<ErrorLogJpaEntity> findAllByLevelOrderByOccurredAtDesc(String level, Pageable pageable);
}