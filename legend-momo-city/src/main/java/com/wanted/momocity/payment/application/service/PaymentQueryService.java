package com.wanted.momocity.payment.application.service;

import com.wanted.momocity.payment.application.usecase.PaymentQueryUseCase;
import com.wanted.momocity.payment.domain.model.MonthlySalesResult;
import com.wanted.momocity.payment.domain.model.PersonalPaymentItem;
import com.wanted.momocity.payment.domain.model.PersonalPaymentListResult;
import com.wanted.momocity.payment.domain.model.Status;
import com.wanted.momocity.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService implements PaymentQueryUseCase {

    private final PaymentRepository paymentRepository;

    @Override
    public long getTotalSales() {
        return paymentRepository.getTotalSales();
    }

    // 월별 매출 조회
    @Override
    public List<MonthlySalesResult> getMonthlySales(int year) {
        return paymentRepository.getMonthlySales(year);
    }

    // 개인 결제 내역 조회
    @Override
    public PersonalPaymentListResult getPersonalPaymentList(Long userId, Status status, int page, int size) {
        List<PersonalPaymentItem> payments = paymentRepository.findPersonalPaymentList(userId, status, page, size);
        long totalElements = paymentRepository.countPersonalPaymentList(userId, status);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new PersonalPaymentListResult(payments, page, size, totalElements, totalPages);
    }
}
