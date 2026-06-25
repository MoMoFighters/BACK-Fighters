package com.wanted.momocity.order.presentation.api.request;

import com.wanted.momocity.order.domain.model.Reason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MakeOrderRequest(

        @Schema(description = "포인트 변동 사유")
        @NotNull(message = "reason 은 필수입니다")
        Reason reason,

        @Schema(description = "구매할 상품명")
        @NotNull(message = "구매할 상품을 선택해주세요.")
        String itemName // 뭐를 샀는지

) {
}
