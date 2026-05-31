package com.wanted.momocity.user.presentation.api;

import com.wanted.momocity.auth.application.port.BlacklistPort;
import com.wanted.momocity.auth.application.port.RedisRefreshTokenPort;
import com.wanted.momocity.auth.application.port.TokenProviderPort;
import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.user.application.command.NicknameRegisterCommand;
import com.wanted.momocity.user.application.command.UpdateUserInfoCommand;
import com.wanted.momocity.user.application.usecase.UserCommandUsecase;
import com.wanted.momocity.user.application.usecase.UserQueryUsecase;
import com.wanted.momocity.user.presentation.api.request.NicknameRequest;
import com.wanted.momocity.user.presentation.api.request.UpdateUserInfoRequest;
import com.wanted.momocity.user.presentation.api.response.*;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name="User - 사용자 정보 관리", description = "user 정보를 다루기 위한 User api 관련 컨트롤러")
public class UserController {

    private final UserCommandUsecase userCommandUsecase;
    private final UserQueryUsecase userQueryUsecase;

    private final BlacklistPort blacklistPort;
    private final TokenProviderPort tokenProviderPort;
    private final RedisRefreshTokenPort redisRefreshTokenPort;


    @GetMapping("/user/detail")
    @Operation(
            summary = "회원 1명의 정보 조회",
            description = "마이페이지에서 사용자에게 제시될 정보 조회"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "닉네임 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 닉네임 형식 (@Valid 실패)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "닉네임 중복")
    })
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값 (@Valid 실패)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "닉네임 중복")
    })
    public ResponseEntity<ApiResponse<UserInfoUpdateResponse>> updateUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody @Valid UpdateUserInfoRequest request){

        userCommandUsecase.updateUserInfo(new UpdateUserInfoCommand(
                userDetails.getUserId(),request.profileImageUrl(),request.nickname(),request.currentPassword(),request.password()
        ));

        boolean isPwdChanged = request.password() != null;

        // 비밀번호 변경되면 있는 토큰 다 만료시키고 액세스 블랙리스트 처리 - 로그아웃이랑 동일
        if (isPwdChanged) {
            String accessToken = bearerToken.substring(7);
            long remaining = tokenProviderPort.getRemainingMillis(accessToken);
            if (remaining > 0) {
                // 액세스 토큰 블랙리스트에 추가
                blacklistPort.addBlacklist(accessToken, remaining);
            }
            // 리프레시 토큰 삭제
            redisRefreshTokenPort.deleteByUserId(String.valueOf(userDetails.getUserId()));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        UserResponseCode.SUCCESS,
                        UserResponseMessage.USER_INFO_UPDATE_SUCCESS,
                        new UserInfoUpdateResponse(isPwdChanged)
                ));
    }

    @PostMapping("/user/nickname/check")
    @Operation(summary = "닉네임 중복 확인")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용 가능한 닉네임"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 닉네임 형식 (@Valid 실패)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "닉네임 중복")
    })
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

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "관리자 회원 목록 조회",
            description = "파라미터 없으면 탈퇴회원 제외 전체 조회. role=STUDENT/TEACHER 또는 status=DELETED 로 필터링")
    public ResponseEntity<ApiResponse<UserQueryUsecase.AdminUserListResult>> getUserList(
            @Parameter(description = "회원 역할 필터 (STUDENT / TEACHER), 없으면 전체", example = "TEACHER")
            @RequestParam(required = false) String role,
            @Parameter(description = "탈퇴회원 조회 시 DELETED 입력", example = "DELETED")
            @RequestParam(required = false) String status,
            @Parameter(description = "페이지 번호 (1-base)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.SUCCESS,
                UserResponseMessage.VIEW_SUCCESS,
                userQueryUsecase.getAdminUserList(role, status, page, size)
        ));
    }

}
