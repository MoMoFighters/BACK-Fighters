package com.wanted.momocity.payment.infrastructure.persistence;

import com.wanted.momocity.payment.domain.model.AdminPaymentItem;
import com.wanted.momocity.payment.domain.model.MonthlySalesResult;
import com.wanted.momocity.payment.domain.model.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, Long> {

    Optional<PaymentJpaEntity> findByPaymentId(String paymentId);

    boolean existsByOriginalPaymentIdAndStatus(String paymentId, Status status);

    List<PaymentJpaEntity> findByStatusAndCreatedAtBefore(Status status, LocalDateTime threshold);

    // 총 매출 조회
    /*comment
     *  COALESCE(SUM(p.price), 0) -> 합산 결과가 널이면 0, 아니면 합산 결과 반환
     *  서브쿼리 -> 만약 한 결제건이 success도 있고 refund도 있으면 그건 매출에 합산 X */
    @Query("SELECT COALESCE(SUM(p.price), 0) FROM PaymentJpaEntity p " +
            "WHERE p.status = 'SUCCESS' " +
            "AND NOT EXISTS (SELECT 1 FROM PaymentJpaEntity r WHERE r.originalPaymentId = p.paymentId AND r.status = 'REFUND')")
    Long getTotalSales();


    // 월별 총 매출
    @Query("SELECT new com.wanted.momocity.payment.domain.model.MonthlySalesResult(MONTH(p.createdAt), COALESCE(SUM(p.price), 0)) " +
            "FROM PaymentJpaEntity p " +
            "WHERE p.status = 'SUCCESS' " +
            "AND YEAR(p.createdAt) = :year " +
            "AND NOT EXISTS (SELECT 1 FROM PaymentJpaEntity r WHERE r.originalPaymentId = p.paymentId AND r.status = 'REFUND') " +
            "GROUP BY MONTH(p.createdAt) " +
            "ORDER BY MONTH(p.createdAt)")
    List<MonthlySalesResult> getMonthlySales(@Param("year") int year);

    // 개인 결제 내역 조회
    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.userId = :userId " +
            "AND p.status <> 'PENDING' " +
            "AND (:status IS NULL OR p.status = :status) " +
            "ORDER BY p.createdAt DESC")
    List<PaymentJpaEntity> findPersonalPaymentList(
            @Param("userId") Long userId,
            @Param("status") Status status,
            Pageable pageable
    );

    // 페이지네이션용
    @Query("SELECT COUNT(p) FROM PaymentJpaEntity p WHERE p.userId = :userId " +
            "AND p.status <> 'PENDING' " +
            "AND (:status IS NULL OR p.status = :status)")
    long countPersonalPaymentList(
            @Param("userId") Long userId,
            @Param("status") Status status
    );

    @Query("SELECT new com.wanted.momocity.payment.domain.model.AdminPaymentItem(" +
            "u.name, p.price, p.plan, p.status, p.createdAt) " +
            "FROM PaymentJpaEntity p JOIN UserUser u ON p.userId = u.id " +
            "WHERE p.status IN ('SUCCESS', 'REFUND') " +
            "AND (:status IS NULL OR p.status = :status) " +
            "ORDER BY p.createdAt DESC")
    List<AdminPaymentItem> findAdminPaymentList(
            @Param("status") Status status,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM PaymentJpaEntity p WHERE p.status IN ('SUCCESS', 'REFUND') " +
            "AND (:status IS NULL OR p.status = :status)")
    long countAdminPaymentList(@Param("status") Status status);
}

