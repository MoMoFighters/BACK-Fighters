package com.wanted.momocity.user.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.user.application.command.NicknameRegisterCommand;
import com.wanted.momocity.user.application.command.UpdateUserInfoCommand;
import com.wanted.momocity.user.application.usecase.UserCommandUsecase;
import com.wanted.momocity.user.application.usecase.UserQueryUsecase;
import com.wanted.momocity.user.presentation.api.request.NicknameRequest;
import com.wanted.momocity.user.presentation.api.request.UpdateUserInfoRequest;
import com.wanted.momocity.user.presentation.api.response.NicknameRegisterResponse;
import com.wanted.momocity.user.presentation.api.response.UserResponseCode;
import com.wanted.momocity.user.presentation.api.response.UserResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name="user controller", description = "user 정보를 다루기 위한 User api 관련 컨트롤러")
public class UserController {

    private final UserCommandUsecase userCommandUsecase;
    private final UserQueryUsecase userQueryUsecase;


    @GetMapping("/user/detail")
    @Operation(
            summary = "회원 1명의 정보 조회",
            description = "마이페이지에서 사용자에게 제시될 정보 조회"
    )
    public ResponseEntity<ApiResponse<UserQueryUsecase.UserDetailView>> getUserDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails){

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.SUCCESS,
                UserResponseMessage.VIEW_SUCCESS,
                userQueryUsecase.userDetail(userDetails.getUserId())
        ));
    }


    @PatchMapping("/user/register/nickname")
    @Operation(summary = "사용자의 닉네임 등록을 위한 api")
    public ResponseEntity<ApiResponse<NicknameRegisterResponse>> registerNickname(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid NicknameRequest request){

        String nickname = userCommandUsecase.registerNickname(new NicknameRegisterCommand(userDetails.getUserId(),request.nickname()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        UserResponseCode.SUCCESS,
                        nickname+UserResponseMessage.NICKNAME_REGISTERED,
                        new NicknameRegisterResponse(nickname)
                ));
    }


    @PatchMapping("/user/update")
    @Operation(summary = "사용자 정보 수정",
                description = "프로필 이미지(모듈4부터), 닉네임, 비밀번호 변경 가능")
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UpdateUserInfoRequest request){

        userCommandUsecase.updateUserInfo(new UpdateUserInfoCommand(
                userDetails.getUserId(),request.profileImageUrl(),request.nickname(),request.currentPassword(),request.password()
        ));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        UserResponseCode.SUCCESS,
                        UserResponseMessage.USER_INFO_UPDATE_SUCCESS,
                    null
                ));
    }

    @PostMapping("/user/nickname/check")
    @Operation(summary = "닉네임 중복 확인")
    public ResponseEntity<ApiResponse<Void>> checkNickname(
            @RequestBody @Valid NicknameRequest request) {

        userQueryUsecase.checkNickname(request.nickname());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        UserResponseCode.SUCCESS,
                        UserResponseMessage.NICKNAME_AVAILABLE,
                        null
                ));
    }


    @GetMapping("/user/buildings")
    @Operation(
            summary = "로그인 후 학생의 메인페이지 렌더링을 위한 정보 전달",
            description = "액세스 토큰을 받아 카테고리, 포지션, 레벨 세 값을 응답에 전달한다")
    public ResponseEntity<ApiResponse<UserQueryUsecase.RenderingBuildingsView>>renderingBuildings(
            @AuthenticationPrincipal CustomUserDetails userDetails ){

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.SUCCESS,
                UserResponseMessage.VIEW_BUILDING_INFO,
                userQueryUsecase.userBuildingInfo(userDetails.getUserId())
        ));
    }


}
