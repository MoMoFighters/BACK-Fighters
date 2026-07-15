package com.wanted.momocity.payment.infrastructure.persistence;

import com.wanted.momocity.payment.domain.model.*;
import com.wanted.momocity.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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

    @Override
    public long getTotalSales() {
        return springDataPaymentRepository.getTotalSales();
    }

    // 월별 총매출
    @Override
    public List<MonthlySalesResult> getMonthlySales() {
        int currentYear = LocalDateTime.now().getYear();
        LocalDateTime startDate = LocalDateTime.of(currentYear, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(currentYear + 1, 1, 1, 0, 0, 0);

        Map<Integer, Long> monthMap = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) monthMap.put(i, 0L);

        springDataPaymentRepository.getMonthlySales(startDate, endDate)
                .forEach(mc -> monthMap.put(mc.month(), mc.sales()));

        return monthMap.entrySet().stream()
                .map(e -> new MonthlySalesResult(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public List<PersonalPaymentItem> findPersonalPaymentList(Long userId, Status status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return springDataPaymentRepository.findPersonalPaymentList(userId, status, pageable).stream()
                .map(entity -> new PersonalPaymentItem(
                        entity.getPrice(),
                        entity.getPlan(),
                        entity.getStatus(),
                        entity.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public long countPersonalPaymentList(Long userId, Status status) {
        return springDataPaymentRepository.countPersonalPaymentList(userId, status);
    }

    // 관리자의 서비스 결제 내역 조회
    @Override
    public List<AdminPaymentItem> findAdminPaymentList(Status status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return springDataPaymentRepository.findAdminPaymentList(status, pageable);
    }

    @Override
    public long countAdminPaymentList(Status status) {
        return springDataPaymentRepository.countAdminPaymentList(status);
    }

    // 월별 + 플랜별 분포
    @Override
    public List<MonthlyPlanDistributionResult> getMonthlyPlanDistribution() {
        int currentYear = LocalDateTime.now().getYear();
        LocalDateTime startDate = LocalDateTime.of(currentYear, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(currentYear + 1, 1, 1, 0, 0, 0);

        List<MonthlyPlanCount> monthlyPlan = springDataPaymentRepository.getMonthlyPlanDistribution(startDate, endDate);

        Map<Integer, Map<String, Long>> grouped = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) grouped.put(i, new HashMap<>());

        monthlyPlan.forEach(mc -> grouped.get(mc.month()).put(mc.plan().name(), mc.count()));

        return grouped.entrySet().stream()
                .map(e -> new MonthlyPlanDistributionResult(
                        e.getKey(),
                        e.getValue().getOrDefault(Plan.BASIC.name(), 0L),
                        e.getValue().getOrDefault(Plan.PLUS.name(), 0L),
                        e.getValue().getOrDefault(Plan.PRO.name(), 0L)
                ))
                .toList();
    }
}
