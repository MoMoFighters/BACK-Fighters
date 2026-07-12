package com.wanted.momocity.payment.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.payment.application.command.PaymentPrepareCommand;
import com.wanted.momocity.payment.application.usecase.PaymentCommandUseCase;
import com.wanted.momocity.payment.application.usecase.PaymentQueryUseCase;
import com.wanted.momocity.payment.domain.model.PaymentPrepareResult;
import com.wanted.momocity.payment.presentation.api.common.PaymentResponseCode;
import com.wanted.momocity.payment.presentation.api.common.PaymentResponseMessage;
import com.wanted.momocity.payment.presentation.api.request.PaymentPrepareRequest;
import com.wanted.momocity.payment.presentation.api.response.PaymentPrepareResponse;
import com.wanted.momocity.user.presentation.api.request.NicknameRequest;
import com.wanted.momocity.user.presentation.api.response.NicknameRegisterResponse;
import com.wanted.momocity.user.presentation.api.response.UserResponseCode;
import com.wanted.momocity.user.presentation.api.response.UserResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v3/payment")
@Tag(name="Payment - 결제 및 결제 정보 관리")
public class PaymentController {

    private final PaymentCommandUseCase paymentCommandUseCase;
    private final PaymentQueryUseCase paymentQueryUseCase;

    @PostMapping("/prepare")
    @Operation(
            summary = "본 결제 전 확인 단게",
            description = "사용자가 결제 해야하는 금액을 1차적으로 db에 저장하고 추후에 실제 결제가 일어날 때" +
                    "이때 저장해둔 값과 실제 결제값이 일치하는지 확인"
    )
    public ResponseEntity<ApiResponse<PaymentPrepareResponse>> paymentPrepare(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PaymentPrepareRequest request
    ){

        PaymentPrepareResult result = paymentCommandUseCase.paymentPrepare(new PaymentPrepareCommand(userDetails.getUserId(),request.plan()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        PaymentResponseCode.PAYMENT_READY,
                        PaymentResponseMessage.PAYMENT_READY,
                        new PaymentPrepareResponse(result.price(),result.createdAt(),result.paymentId())
                ));
    }


}
