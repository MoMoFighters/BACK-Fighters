package com.wanted.momocity.payment.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.payment.application.command.CancelCommand;
import com.wanted.momocity.payment.application.command.PaymentPrepareCommand;
import com.wanted.momocity.payment.application.command.PaymentVerifyCommand;
import com.wanted.momocity.payment.application.usecase.PaymentCommandUseCase;
import com.wanted.momocity.payment.application.usecase.PaymentQueryUseCase;
import com.wanted.momocity.payment.domain.model.*;
import com.wanted.momocity.payment.presentation.api.common.PaymentResponseCode;
import com.wanted.momocity.payment.presentation.api.common.PaymentResponseMessage;
import com.wanted.momocity.payment.presentation.api.request.CancelRequest;
import com.wanted.momocity.payment.presentation.api.request.PaymentPrepareRequest;
import com.wanted.momocity.payment.presentation.api.request.PaymentVerifyRequest;
import com.wanted.momocity.payment.presentation.api.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 준비 완료 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값 검증 실패 - plan 누락/잘못된 값  또는 다운그레이드 시도 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 이용 중인 플랜  또는 이미 진행 중인 결제 존재 ")
    })
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


    @PostMapping("/verify")
    @Operation(summary = "결제 후 확인")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 검증 완료 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제 건을 찾을 수 없음 또는 결제 시도 자체가 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 처리된 결제 건 또는 결제 금액 불일치 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "결제 취소 처리 실패, 수동 확인 필요 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "포트원 API 호출 실패 ")
    })
    public ResponseEntity<ApiResponse<PaymentVerifyResponse>> paymentVerify(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PaymentVerifyRequest request
    ){
        PaymentVerifyResult result = paymentCommandUseCase.paymentVerify(new PaymentVerifyCommand(userDetails.getUserId(),request.paymentId()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        PaymentResponseCode.PAYMENT_VERIFIED,
                        PaymentResponseMessage.PAYMENT_VERIFIED,
                        new PaymentVerifyResponse(result.membershipUntil())
                ));
    }

    @PatchMapping("/cancel")
    @Operation(summary = "환불기능",
            description = "결제 후 3일 이내이면 환불 + basis 전환 / 3일 후면 플랜 기간 유지 + 기간 종료 시 basic으로")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "환불 완료 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 결제 건이 아님 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제 건을 찾을 수 없음 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "환불 가능 기간 초과 또는 이미 환불된 건 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "포트원 취소 처리 실패, 수동 확인 필요 ")
    })
    public ResponseEntity<ApiResponse<Void>> cancel (
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CancelRequest request
    ){

        paymentCommandUseCase.cancel(new CancelCommand(userDetails.getUserId(),request.paymentId()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        PaymentResponseCode.SUBSCRIBE_CANCEL,
                        PaymentResponseMessage.SUBSCRIBE_CANCEL,
                        null
                ));
    }

    @GetMapping("/sales/total")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "전체 총매출 계산")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "월별 총매출 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    public ResponseEntity<ApiResponse<TotalSalesResponse>> getTotalSales(){
        long totalSales = paymentQueryUseCase.getTotalSales();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        PaymentResponseCode.FETCH_SUCCESS,
                        PaymentResponseMessage.FETCH_SUCCESS,
                        new TotalSalesResponse(totalSales)
                ));
    }

    @GetMapping("/sales/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "월별 총매출 계산")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "월별 총매출 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    public ResponseEntity<ApiResponse<List<MonthlySalesResponse>>> getMonthlySales(
            @RequestParam int year
    ) {
        List<MonthlySalesResult> result = paymentQueryUseCase.getMonthlySales(year);
        List<MonthlySalesResponse> response = result.stream()
                .map(MonthlySalesResponse::from)
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        PaymentResponseCode.FETCH_SUCCESS,
                        PaymentResponseMessage.FETCH_SUCCESS,
                        response
                ));
    }

    @GetMapping("/user/list")
    @Operation(summary = "회원 개인의 메인페이지에서 보는 결제 내역")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "개인 결제 기록 조회 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료"),
    })
    public ResponseEntity<ApiResponse<PersonalPaymentListResponse>> getPersonalPaymentList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Status status,
            @Parameter(description = "페이지 번호 (1-base)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "10") int size
    ){
        PersonalPaymentListResult result = paymentQueryUseCase.getPersonalPaymentList(userDetails.getUserId(), status, page, size);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        PaymentResponseCode.FETCH_SUCCESS,
                        PaymentResponseMessage.FETCH_SUCCESS,
                        PersonalPaymentListResponse.from(result)
                ));
    }

    @GetMapping("/admin/list")
    @Operation(summary = "관리자가 보는 시스템 결제 내역")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 내역 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    public ResponseEntity<ApiResponse<AdminPaymentListResponse>> getAdminPaymentList(
            @RequestParam(required = false) Status status,
            @Parameter(description = "페이지 번호 (1-base)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "10") int size
    ) {
        AdminPaymentListResult result = paymentQueryUseCase.getAdminPaymentList(status, page, size);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        PaymentResponseCode.FETCH_SUCCESS,
                        PaymentResponseMessage.FETCH_SUCCESS,
                        AdminPaymentListResponse.from(result)
                ));

    }
}





