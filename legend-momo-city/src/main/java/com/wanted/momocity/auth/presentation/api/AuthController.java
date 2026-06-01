package com.wanted.momocity.auth.presentation.api;

import com.wanted.momocity.auth.application.command.*;
import com.wanted.momocity.auth.application.port.TokenProviderPort;
import com.wanted.momocity.auth.domain.exception.MissingProofException;
import com.wanted.momocity.auth.domain.exception.MissingTokenException;
import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.auth.application.usecase.*;
import com.wanted.momocity.auth.presentation.api.request.*;
import com.wanted.momocity.auth.presentation.api.response.*;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
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
@RequestMapping("/api/v1/auth")
@Tag(name="Auth - 인증·인가 ", description = "인증·인가를 위한 Auth api 관련 컨트롤러")
public class AuthController {

    private final AuthQueryUsecase authQueryUsecase;
    private final AuthCommandUsecase authCommandUsecase;

    private final NewTokenUsecase newTokenUsecase;

    private final TokenProviderPort tokenProviderPort;
    private final S3UploadPort s3UploadPort;


    @PostMapping("/signup/student")
    @Operation(
            summary = "학생 자체 회원가입",
            description = "해당 api를 통해 회원가입 한 사람의 role을 STUDENT로 하여 user테이블에 추가하는 메서드"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "학생 회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값 (@Valid 실패)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이메일 중복")
    })
    public ResponseEntity<ApiResponse<Void>> studentSignup (
            @Valid @RequestBody StudentSignupRequest request){

        authCommandUsecase.signup(new StudentSignupCommand(request.email(),request.password(),request.name()));

        return ResponseEntity.status(HttpStatus.CREATED) //201
                .body(ApiResponse.created(
                        AuthResponseCode.CREATED,
                        AuthResponseMessage.STUDENT_CREATED,
                        null
                ));
    }


    @PostMapping("/signup/teacher")
    @Operation(
            summary = "강사 자체 회원가입",
            description = "해당 api를 통해 회원가입 한 사람의 role을 TEACHER로, status는 PENDING으로 하여 user테이블에 추가하는 메서드"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "강사 회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값 (@Valid 실패)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이메일 중복")
    })
    public ResponseEntity<ApiResponse<Void>> teacherSignup (
            @Valid @ModelAttribute TeacherSignupRequest request){

        if (request.proof() == null || request.proof().isEmpty()) {
            throw new MissingProofException("증빙 자료는 필수 제출입니다.");
        }

        String proofUrl = s3UploadPort.upload(request.proof());  // 여기서 선언하고 값 넣어줌

        authCommandUsecase.signup(new TeacherSignupCommand(request.email(),request.password(),request.name(),request.category(),proofUrl));

        return ResponseEntity.status(HttpStatus.CREATED) //201
                .body(ApiResponse.created(
                        AuthResponseCode.CREATED,
                        AuthResponseMessage.TEACHER_CREATED,
                        null
                ));
    }


    @PostMapping("/login")
    @Operation(
            summary = "자체 로그인",
            description = "해당 api를 통해 로그인 하게 되면 토큰이 발급된다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공 및 토큰 발급"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값 (@Valid 실패)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request){

        LoginResponse result =authCommandUsecase.login(new LoginCommand(request.email(),request.password()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        AuthResponseCode.SUCCESS,
                        AuthResponseMessage.LOGIN_SUCCESS,
                        new LoginResponse(result.accessToken(), result.refreshToken(),result.status() ,result.expiresIn())
                ));

    }


    @GetMapping("/login/completed")
    @Operation(
            summary = "로그인 성공 시 페이지 로딩을 위한 사용자 정보 전달",
            description = "로그인 한 사용자의 role, is_tempPWD, nickname 을 보내 그에 맞는 페이지를 랜더링한다"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)")
    })
    public ResponseEntity<ApiResponse<LoginCompletedResponse>> loginCompleted(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        LoginCompletedResponse result = authQueryUsecase.getInfo(userDetails.getUsername()); // id

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        AuthResponseCode.SUCCESS,
                        AuthResponseMessage.LOGIN_COMPLETED,
                        new LoginCompletedResponse(result.role(), result.is_tempPwd(), result.nickname())
                ));
    }


    @PostMapping("/email/send")
    @Operation(
            summary = "회원가입 할 때 이메일로 인증코드 발송",
            description = "이메일 중복 확인 및 본인 인증을 위한 이메일 인증 코드 발송 - "
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증코드 발송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 이메일 형식"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입된 이메일")
    })
    public ResponseEntity<ApiResponse<EmailSendResponse>> emailSend(
            @Valid @RequestBody EmailSendRequest request){

        EmailSendResponse result = authCommandUsecase.emailSend(new EmailSendCommand(request.email()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        AuthResponseCode.SUCCESS,
                        AuthResponseMessage.EMAIL_SEND_SUCCESS,
                        new EmailSendResponse(result.expiresIn())
                ));

    }

    @PostMapping("/email/verify")
    @Operation(
            summary = "인증코드 값 인증",
            description = "서버가 메일로 보낸 값과 사용자가 보낸 값이 일치하는지 인증"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증코드 일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증코드 불일치 또는 만료")
    })
    public ResponseEntity<ApiResponse<EmailVerifyResponse>> emailVerify(
            @Valid @RequestBody EmailVerifyRequest request){

        authQueryUsecase.emailVerify(new EmailVerifyCommand(request.email(), request.code()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        AuthResponseCode.SUCCESS,
                        AuthResponseMessage.EMAIL_VERIFY_SUCCESS,
                        null
                ));

    }


    @PostMapping("/password/temp")
    @Operation(
            summary = "임시 비밀번호 발급하여 이메일로 전송",
            description = "랜덤 숫자 8자리를 만들어 이메일로 전송해주고 db의 비밀번호를 해당 랜덤 값으로 변경하여 임시로그인 가능하게 함." +
                    "임시비밀번호의 유효 시간은 3분으로 3분 안에 마이페이지에서 비밀번호를 변경하여야 한다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "임시 비밀번호 발급 및 전송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 이메일 형식"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가입되지 않은 이메일")
    })
    public ResponseEntity<ApiResponse<Void>> tempPasswordSend(
            @Valid @RequestBody EmailSendRequest request){

        authCommandUsecase.sendTempPassword(new EmailSendCommand(request.email()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        AuthResponseCode.SUCCESS,
                        AuthResponseMessage.TEMP_PASSWORD_CREATED,
                        null
                ));
    }


    @PostMapping("/kakaologin")
    @Operation(summary = "카카오로그인을 위한 api")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "카카오 로그인 성공 및 토큰 발급"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 인가 코드"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "카카오 인증 실패")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(
            @Valid @RequestBody SocialLoginRequest request){

        LoginResponse result = authCommandUsecase.socialLogin(new SocialLoginCommand("KAKAO",request.code()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        AuthResponseCode.SUCCESS,
                        AuthResponseMessage.LOGIN_SUCCESS,
                        new LoginResponse(result.accessToken(), result.refreshToken(),result.status() ,result.expiresIn())
                ));
    }


    @PostMapping("/googlelogin")
    @Operation(summary = "구글로그인을 위한 api")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "구글 로그인 성공 및 토큰 발급"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 인가 코드"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "구글 인증 실패")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(
            @Valid @RequestBody SocialLoginRequest request){

        LoginResponse result = authCommandUsecase.socialLogin(new SocialLoginCommand("GOOGLE",request.code()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        AuthResponseCode.SUCCESS,
                        AuthResponseMessage.LOGIN_SUCCESS,
                        new LoginResponse(result.accessToken(), result.refreshToken(),result.status() ,result.expiresIn())
                ));
    }


    @PostMapping("/logout")
    @Operation(summary = "로그아웃을 위한 api" ,
            description = "redis 에서 refresh 토큰 값 제거를 통해 로그아웃 진행")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "access 토큰 또는 refresh 토큰 없음")
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(name = "Authorization", description = "Bearer 액세스 토큰", required = true)
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @Parameter(name = "Refresh-Token", description = "리프레시 토큰", required = true)
            @RequestHeader(value = "Refresh-Token", required = false) String refreshToken)
    // required = true 면 헤더 없으면 Spring이 기본 400을 던지지만
    // 커스텀 메시지 보여주고 싶어서
    // 헤더 없으면 null로 들어옴 → 내 if문 실행 → 커스텀 메시지 출력되도록
    {

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new MissingTokenException("AccessToken이 없습니다.");
        }
        if (refreshToken == null) {
            throw new MissingTokenException("RefreshToken이 없습니다.");
        }

        // Authorization: Bearer {accessToken} 에서 앞부분 떼고 액세스 토큰만 가져옴
        String accessToken = bearerToken.replace("Bearer ", "");

        authCommandUsecase.logout(new LogoutCommand(accessToken,refreshToken));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        AuthResponseCode.SUCCESS,
                        AuthResponseMessage.LOGOUT_SUCCESS,
                        null
                ));
    }


    @PostMapping("/newtoken")
    @Operation(summary = "새로운 액세스 토큰 발급해주는 api",
            description = "사용자가 연장 버튼 누르면 새로운 api를 발급해줌")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "새로운 액세스 토큰 발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "RefreshToken 없음 또는 만료")
    })
    public ResponseEntity<ApiResponse<NewTokenResponse>> newToken(
            @Parameter(name = "Authorization", description = "Bearer 액세스 토큰", required = true)
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @Parameter(name = "Refresh-Token", description = "리프레시 토큰", required = true)
            @RequestHeader(value = "Refresh-Token", required = false) String refreshToken)
    // required = true 면 헤더 없으면 Spring이 기본 400을 던지지만
    // 커스텀 메시지 보여주고 싶어서
    // 헤더 없으면 null로 들어옴 → 내 if문 실행 → 커스텀 메시지 출력되도록
    {

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new MissingTokenException("AccessToken이 없습니다.");
        }
        if (refreshToken == null) {
            throw new MissingTokenException("RefreshToken이 없습니다.");
        }

        String oldAccessToken = bearerToken.replace("Bearer ", "");
        String newAccessToken = newTokenUsecase.refreshAccessToken(refreshToken, oldAccessToken);


        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        AuthResponseCode.SUCCESS,
                        AuthResponseMessage.NEW_TOKEN_CREATED,
                        new NewTokenResponse(newAccessToken ,tokenProviderPort.getAccessTokenValidityMilliseconds() / 1000 )
                ));
    }


}
