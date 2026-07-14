package com.wanted.momocity.payment.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CancelRequest(
        @Schema(description = "prepare 응답으로 받은 결제 고유 ID")
        @NotBlank(message = "paymentId는 필수입니다.")
        String paymentId
) {
}
