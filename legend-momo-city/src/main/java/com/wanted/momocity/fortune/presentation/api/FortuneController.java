package com.wanted.momocity.fortune.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.fortune.application.result.DrawFortuneResult;
import com.wanted.momocity.fortune.application.usecase.FortuneCommandUseCase;
import com.wanted.momocity.fortune.presentation.api.common.FortuneResponseCode;
import com.wanted.momocity.fortune.presentation.api.common.FortuneResponseMessage;
import com.wanted.momocity.fortune.presentation.api.response.FortuneResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/city/fortune")
@Tag(name = "Fortune - 오늘의 운세", description = "포인트를 사용한 오늘의 운세 관리")
public class FortuneController {

    private final FortuneCommandUseCase fortuneCommandUseCase;

    // 오늘의 운세 뽑기
    @PostMapping
    @Operation(
            summary = "오늘의 운세 뽑기",
            description = "오늘 처음 요청하면 5포인트를 차감하고, 재요청하면 같은 운세를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "오늘의 운세 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "운세 뽑기 포인트 부족"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "운세 데이터 조회 실패"
            )
    })
    public ResponseEntity<ApiResponse<FortuneResponse>> drawToday(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 로그인한 사용자 ID를 서비스에 전달하여 오늘의 운세를 조회
        DrawFortuneResult result =
                fortuneCommandUseCase.drawToday(userDetails.getUserId());

        // 서비스 결과를 외부 API 응답 DTO로 변환
        FortuneResponse response = FortuneResponse.from(result);

        return ResponseEntity.ok(ApiResponse.success(
                FortuneResponseCode.DRAW_SUCCESS,
                FortuneResponseMessage.DRAW_SUCCESS,
                response
        ));
    }
}
