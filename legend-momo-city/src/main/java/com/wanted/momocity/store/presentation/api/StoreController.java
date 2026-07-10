package com.wanted.momocity.store.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.store.application.usecase.StoreCommandUsecase;
import com.wanted.momocity.store.application.usecase.StoreQueryUsecase;
import com.wanted.momocity.store.domain.model.StoreListResult;
import com.wanted.momocity.store.presentation.api.common.StoreResponseCode;
import com.wanted.momocity.store.presentation.api.common.StoreResponseMessage;
import com.wanted.momocity.store.presentation.api.response.StoreListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/store")
@Tag(name = "상점 관리", description = "포인트를 활용한 상점에서의 상품 구매 및 내역 관리")
public class StoreController {

    private final StoreQueryUsecase storeQueryUsecase;
    private final StoreCommandUsecase storeCommandUsecase;

    @GetMapping("/product/list")
    @Operation(summary = "상점에 있는 모든 항목 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)")
    })
    public ResponseEntity<ApiResponse<StoreListResponse>> getStoreProductList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 번호 (1-base)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "12") int size
    ){

        StoreListResult result = storeQueryUsecase.getProductList(userDetails.getUserId(),page, size);
        StoreListResponse response = StoreListResponse.from(result);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        StoreResponseCode.LIST_FOUND_SUCCESS,
                        StoreResponseMessage.PRODUCT_LIST_FETCHED,
                        response
                ));
    }
}
