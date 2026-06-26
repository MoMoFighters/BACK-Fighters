package com.wanted.momocity.order.infrastructure.persistence;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity,Long> {

    // 이미 구매한 상품인지 확인
    // 실제로 구매한 상품이 맞는지 확인
    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    // 한 사용자의 포인트 변동 내역 조회
    List<OrderJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 페이지네이션
    long countByUserId(Long userId);
}
