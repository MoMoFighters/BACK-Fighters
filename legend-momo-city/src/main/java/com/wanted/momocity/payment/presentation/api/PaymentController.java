package com.wanted.momocity.payment.presentation.api;

import com.wanted.momocity.payment.application.usecase.PaymentCommandUseCase;
import com.wanted.momocity.payment.application.usecase.PaymentQueryUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v3/payment")
@Tag(name="Payment - 결제 및 결제 정보 관리")
public class PaymentController {

    private final PaymentCommandUseCase paymentCommandUseCase;
    private final PaymentQueryUseCase paymentQueryUseCase;



}
