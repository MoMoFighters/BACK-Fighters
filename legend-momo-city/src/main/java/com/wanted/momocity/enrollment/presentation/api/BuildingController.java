package com.wanted.momocity.enrollment.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.enrollment.application.usecase.EnrollmentQueryUsecase;
import com.wanted.momocity.enrollment.application.usecase.FriendCityVisitUseCase;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name="Building - 사용자 건물 관리 ", description = "건물 정보를 다루기 위한 컨트롤러 ")
public class BuildingController {

    private final EnrollmentQueryUsecase enrollmentQueryUsecase;
    private final FriendCityVisitUseCase friendCityVisitUseCase;

    @GetMapping("/user/buildings")
    @Operation(
            summary = "로그인 후 학생의 메인페이지 렌더링을 위한 정보 전달",
            description = "액세스 토큰을 받아 카테고리, 포지션, 레벨 세 값을 응답에 전달한다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "메인페이지 렌더링 정보 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
    })
    public ResponseEntity<ApiResponse<List<EnrollmentQueryUsecase.RenderingBuildingsView>>> renderingBuildings(
            @AuthenticationPrincipal CustomUserDetails userDetails ){

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "사용자 빌딩 정보가 조회되었습니다.",
                enrollmentQueryUsecase.userBuildingInfo(userDetails.getUserId())
        ));
    }

    // 친구 마을 건물 조회
    @PostMapping("/user/{userId}/buildings")
    @Operation(
            summary = "버스를 이용한 친구 도시 방문",
            description = "친구 관계를 검증하고 버스 요금 1포인트를 차감한 뒤 친구 도시 정보를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "친구 도시 방문 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "본인 도시 요청 또는 포인트 부족"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "친구 또는 사용자를 찾을 수 없음"
            )
    })
    public ResponseEntity<ApiResponse<EnrollmentQueryUsecase.FriendBuildingsView>> visitFriendCity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ) {
        // 로그인 사용자 ID를 버스 요금 지불자로 사용
        Long loginUserId = userDetails.getUserId();

        // 친구 관계 검증, 포인트 차감, 사용 이력 저장 후 도시 정보 받기
        EnrollmentQueryUsecase.FriendBuildingsView friendBuildings =
                friendCityVisitUseCase.visitFriendCity(
                        loginUserId,
                        userId
                );

        // 방문한 친구의 닉네임과 건물 목록을 응답
        return ResponseEntity.ok(
                ApiResponse.success(
                        ApiResponseCode.SUCCESS,
                        "버스를 이용해 친구 도시에 방문했습니다.",
                        friendBuildings
                )
        );
    }
}
