package com.wanted.momocity.user.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.user.application.usecase.UserCommandUsecase;
import com.wanted.momocity.user.application.usecase.UserQueryUsecase;
import com.wanted.momocity.user.presentation.api.response.UserResponseCode;
import com.wanted.momocity.user.presentation.api.response.UserResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
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

}
