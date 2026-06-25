package com.wanted.momocity.order.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.order.application.command.MakeOrderCommand;
import com.wanted.momocity.order.application.usecase.OrderCommandUsecase;
import com.wanted.momocity.order.application.usecase.OrderQueryUsecase;
import com.wanted.momocity.order.presentation.api.common.OrderResponseCode;
import com.wanted.momocity.order.presentation.api.common.OrderResponseMessage;
import com.wanted.momocity.order.presentation.api.request.MakeOrderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/v1/order")
@Tag(name="상품 구매", description = "상품 구매 및 구매 내역 관리")
public class OrderController {

    private final OrderQueryUsecase orderQueryUsecase;
    private final OrderCommandUsecase orderCommandUsecase;

    @PostMapping("/new")
    @Operation(summary="상품 구매",
                description = "상품 구매 시 user 테이블에 point 차감 + order_history에 행 내역 추가")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "구매 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "포인트 부족"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 보유한 상품")
    })
    public ResponseEntity<ApiResponse<Void>> makeOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid MakeOrderRequest request){

        orderCommandUsecase.makeOrder(new MakeOrderCommand(userDetails.getUserId(), request.reason(), request.itemName()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        OrderResponseCode.ORDER_SUCCESS,
                        OrderResponseMessage.ORDER_SUCCESS,
                        null
                ));
    }

    }
