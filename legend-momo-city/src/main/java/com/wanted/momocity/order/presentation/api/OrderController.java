package com.wanted.momocity.order.presentation.api;

import com.wanted.momocity.order.application.usecase.OrderCommandUsecase;
import com.wanted.momocity.order.application.usecase.OrderQueryUsecase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
@Tag(name = "상품 구매 관련", description = "상품 구매 및 상품 구매 내역 조회")
public class OrderController {

    private final OrderCommandUsecase orderCommandUsecase;
    private final OrderQueryUsecase orderQueryUsecase;
}
