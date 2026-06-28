package com.wanted.momocity.admin.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/* comment.
    Spring Data JPA 자동 쿼리 인터페이스.
    메서드 이름으로 WHERE 조건을 표현한다.
 */
public interface SpringDataAccessLogRepository extends JpaRepository<AccessLogJpaEntity, Long> {

    // WHERE action = ? (비로그인 포함)
    Page<AccessLogJpaEntity> findByAction(String action, Pageable pageable);
}
