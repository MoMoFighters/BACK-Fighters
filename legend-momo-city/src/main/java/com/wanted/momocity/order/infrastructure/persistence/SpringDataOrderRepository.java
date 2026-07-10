package com.wanted.momocity.order.infrastructure.persistence;


import com.wanted.momocity.order.domain.model.Reason;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity,Long> {

    // 이미 구매한 상품인지 확인
    // 실제로 구매한 상품이 맞는지 확인
    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    // 한 사용자의 포인트 변동 내역 조회
    List<OrderJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 페이지네이션
    long countByUserId(Long userId);

    // 사용 가능한 프사 목록
    @Query("SELECT o.itemId FROM OrderJpaEntity o WHERE o.userId = :userId AND o.reason = :reason")
    List<Long> findOwnedItemIdsByUserIdAndReason(@Param("userId") Long userId, @Param("reason") Reason reason);

    // 사용자가 전체 상품 목록 중 보유중인 상품의 id
    @Query("SELECT o.itemId FROM OrderJpaEntity o WHERE o.userId = :userId ")
    List<Long> getUserOwnedItemId(@Param("userId") Long userId);
}
