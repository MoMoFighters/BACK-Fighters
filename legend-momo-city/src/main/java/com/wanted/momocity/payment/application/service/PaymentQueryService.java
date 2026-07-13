package com.wanted.momocity.payment.application.service;

import com.wanted.momocity.payment.application.usecase.PaymentQueryUseCase;
import com.wanted.momocity.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
