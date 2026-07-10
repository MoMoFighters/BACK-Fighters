package com.wanted.momocity.order.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.order.application.command.MakeOrderCommand;
import com.wanted.momocity.order.application.usecase.OrderCommandUsecase;
import com.wanted.momocity.order.application.usecase.OrderQueryUsecase;
import com.wanted.momocity.order.domain.model.OrderHistoryList;
import com.wanted.momocity.order.domain.model.ProfileItemPageResult;
import com.wanted.momocity.order.presentation.api.common.OrderResponseCode;
import com.wanted.momocity.order.presentation.api.common.OrderResponseMessage;
import com.wanted.momocity.order.presentation.api.request.MakeOrderRequest;
import com.wanted.momocity.order.presentation.api.response.OrderHistoryResponse;
import com.wanted.momocity.order.presentation.api.response.ProfileItemListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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


    @GetMapping("/list")
    @Operation(summary = "포인트 사용 내역 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)")
    })
    public ResponseEntity<ApiResponse<OrderHistoryResponse>> getOrderHistoryList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 번호 (1-base)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "5") int size){

        OrderHistoryList list = orderQueryUsecase.getOrderHistory(userDetails.getUserId(),page,size);
        OrderHistoryResponse response = OrderHistoryResponse.toResponse(list);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        OrderResponseCode.LIST_FETCHED,
                        OrderResponseMessage.LIST_FETCHED,
                        response
                ));

    }

    @GetMapping("/profile/list")
    @Operation(summary = "사용자가 사용 가능한  프사 목록",
            description = "전체 프사 목록 중 사용 가능한 것과 아닌 것을 구분하여 출력")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)")
    })
    public ResponseEntity<ApiResponse<ProfileItemListResponse>> getAvailableProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 번호 (1-base)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "8") int size
    ) {

        ProfileItemPageResult results = orderQueryUsecase.getAvailableProfile(userDetails.getUserId(), page, size);
        ProfileItemListResponse response = ProfileItemListResponse.toResponse(results);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        OrderResponseCode.LIST_FETCHED,
                        OrderResponseMessage.AVAILABLE_PROFILE_LIST_FETCHED,
                        response
                ));
    }
}


