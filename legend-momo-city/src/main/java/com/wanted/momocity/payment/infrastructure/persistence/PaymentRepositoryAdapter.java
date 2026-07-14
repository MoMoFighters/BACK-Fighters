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

}
