package com.wanted.momocity.payment.domain.repository;

import com.wanted.momocity.payment.domain.model.*;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    // 결제 정보 저장 - prepare
    Payment save(Payment payment);

    // 실제 결제 정보 조회
    Optional<Payment> findByPaymentId(String paymentId);

    // 이미 환불 했는지 확인
    boolean existsByPaymentIdAndStatus(String paymentId, Status status);

    // 총 매출 조회
    long getTotalSales();

    // 월별 매출 조회
    List<MonthlySalesResult> getMonthlySales(int year);

    // 개인 결제 내역 조회
    List<PersonalPaymentItem> findPersonalPaymentList(Long userId, Status status, int page, int size);
    // 페이지네이션용
    long countPersonalPaymentList(Long userId, Status status);

    // 관리자 시스템 결제 내역 조회
    List<AdminPaymentItem> findAdminPaymentList(Status status, int page, int size);

    long countAdminPaymentList(Status status);
    // 월별 + 플랜별 분포
    List<MonthlyPlanDistributionResult> getMonthlyPlanDistribution(int year);
}
