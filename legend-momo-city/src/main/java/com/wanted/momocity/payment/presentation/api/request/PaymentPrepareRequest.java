package com.wanted.momocity.payment.presentation.api.request;

import com.wanted.momocity.payment.domain.model.Plan;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PaymentPrepareRequest(

        @Schema(description = "구독 플랜 종류 (BASIC, PLUS, PRO)")
        @NotNull(message = "구독 플랜을 선택해주세요.")
        Plan plan
) {
}
