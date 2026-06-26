package com.wanted.momocity.order.infrastructure.persistence;


import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity,Long> {

    // 이미 구매한 상품인지 확인
    // 실제로 구매한 상품이 맞는지 확인
    boolean existsByUserIdAndItemId(Long userId, Long itemId);

}
