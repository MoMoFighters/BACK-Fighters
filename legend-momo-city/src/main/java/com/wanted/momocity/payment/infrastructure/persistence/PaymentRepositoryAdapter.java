package com.wanted.momocity.payment.infrastructure.persistence;

import com.wanted.momocity.payment.domain.model.MonthlySalesResult;
import com.wanted.momocity.payment.domain.model.Payment;
import com.wanted.momocity.payment.domain.model.Status;
import com.wanted.momocity.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository("paymentAdapter")
@Transactional
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final SpringDataPaymentRepository springDataPaymentRepository;

    // 결제 정보 저장 - prepare
    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity saved = springDataPaymentRepository.save(PaymentJpaEntity.fromDomain(payment));
        return saved.toDomain();
    }


    @Override
    public Optional<Payment> findByPaymentId(String paymentId) {
        return springDataPaymentRepository.findByPaymentId(paymentId)
                .map(PaymentJpaEntity::toDomain);
    }

    // 이미 환불 한 건지 확인
    @Override
    public boolean existsByPaymentIdAndStatus(String paymentId, Status status) {
        return springDataPaymentRepository.existsByOriginalPaymentIdAndStatus(paymentId, status);
    }

    // 총매출
    @Override
    public long getTotalSales() {
        return springDataPaymentRepository.getTotalSales();
    }

    // 월별 총매출
    @Override
    public List<MonthlySalesResult> getMonthlySales(int year) {
        Map<Integer, Long> monthMap = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) monthMap.put(i, 0L);

        springDataPaymentRepository.getMonthlySales(year)
                .forEach(mc -> monthMap.put(mc.month(), mc.sales()));

        return monthMap.entrySet().stream()
                .map(e -> new MonthlySalesResult(e.getKey(), e.getValue()))
                .toList();
    }
}
