package com.wanted.momocity.enrollment.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.enrollment.application.usecase.EnrollmentQueryUsecase;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.user.presentation.api.response.UserResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name="Building - 사용자 건물 관리 ", description = "건물 정보를 다루기 위한 컨트롤러 ")
public class BuildingController {

    private final EnrollmentQueryUsecase enrollmentQueryUsecase;


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
                UserResponseMessage.VIEW_BUILDING_INFO,
                enrollmentQueryUsecase.userBuildingInfo(userDetails.getUserId())
        ));
    }

    // 친구 마을 건물 조회
    @GetMapping("/user/{userId}/buildings")
    @Operation(
            summary = "친구 마을 건물 정보 조회",
            description = "친구 마을 방문 시 해당 사용자의 건물 정보를 조회한다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "친구 건물 정보 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "본인 Id로 본인 마을 조회"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<List<EnrollmentQueryUsecase.RenderingBuildingsView>>> getFriendBuildings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ) {
        // 로그인한 사용자 Id
        Long loginUserId = userDetails.getUserId();

        // 친구 건물 목록 조회
        List<EnrollmentQueryUsecase.RenderingBuildingsView> buildings =
                enrollmentQueryUsecase.friendBuildingInfo(
                        loginUserId,
                        userId
                );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                UserResponseMessage.VIEW_BUILDING_INFO,
                buildings
        ));
    }
}
