package com.wanted.momocity.user.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.user.application.usecase.UserQueryUsecase;
import com.wanted.momocity.user.presentation.api.response.UserResponseCode;
import com.wanted.momocity.user.presentation.api.response.UserResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name="User Building - 사용자 건물 정보 관리 ", description = "사용자 개인의 건물 정보를 다루기 위한 User api 관련 컨트롤러 - 추후 모듈에서는 마이페이지 속 빌딩 정보 조회 추가 예정")
public class UserBuildingController {

    private final UserQueryUsecase userQueryUsecase;

    @GetMapping("/user/buildings")
    @Operation(
            summary = "로그인 후 학생의 메인페이지 렌더링을 위한 정보 전달",
            description = "액세스 토큰을 받아 카테고리, 포지션, 레벨 세 값을 응답에 전달한다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "메인페이지 렌더링 정보 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<UserQueryUsecase.RenderingBuildingsView>> renderingBuildings(
            @AuthenticationPrincipal CustomUserDetails userDetails ){

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.SUCCESS,
                UserResponseMessage.VIEW_BUILDING_INFO,
                userQueryUsecase.userBuildingInfo(userDetails.getUserId())
        ));
    }
}
