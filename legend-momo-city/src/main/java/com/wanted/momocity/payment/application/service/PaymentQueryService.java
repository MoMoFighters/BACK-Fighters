package com.wanted.momocity.payment.application.service;

import com.wanted.momocity.payment.application.usecase.PaymentQueryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentQueryService implements PaymentQueryUseCase {
}
