package com.wanted.momocity.payment.infrastructure.persistence;

import com.wanted.momocity.payment.domain.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, Long> {

    Optional<PaymentJpaEntity> findByPaymentId(String paymentId);

    boolean existsByOriginalPaymentIdAndStatus(String paymentId, Status status);

    // 총 매출 조회
    /*comment
    *  COALESCE(SUM(p.price), 0) -> 합산 결과가 널이면 0, 아니면 합산 결과 반환
    *  서브쿼리 -> 만약 한 결제건이 success도 있고 refund도 있으면 그건 매출에 합산 X */
    @Query("SELECT COALESCE(SUM(p.price), 0) FROM PaymentJpaEntity p " +
            "WHERE p.status = 'SUCCESS' " +
            "AND NOT EXISTS (SELECT 1 FROM PaymentJpaEntity r WHERE r.originalPaymentId = p.paymentId AND r.status = 'REFUND')")
    Long getTotalSales();
}
