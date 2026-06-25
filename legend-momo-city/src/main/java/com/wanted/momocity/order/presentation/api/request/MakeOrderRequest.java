package com.wanted.momocity.order.presentation.api.request;

import com.wanted.momocity.order.domain.model.Reason;

public record MakeOrderRequest(

      // 누가에 대한 것은 토큰에서 받아올 거임
        Reason reason,
        String itemName // 뭐를 샀는지


) {
}
