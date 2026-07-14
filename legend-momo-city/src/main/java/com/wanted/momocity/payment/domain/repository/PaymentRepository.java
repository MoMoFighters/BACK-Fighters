package com.wanted.momocity.payment.domain.repository;

import com.wanted.momocity.payment.domain.model.MonthlySalesResult;
import com.wanted.momocity.payment.domain.model.Payment;
import com.wanted.momocity.payment.domain.model.Status;

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
}
