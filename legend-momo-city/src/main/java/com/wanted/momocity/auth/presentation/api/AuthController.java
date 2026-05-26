package com.wanted.momocity.auth.presentation.api;

import com.wanted.momocity.auth.application.command.*;
import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.auth.application.usecase.*;
import com.wanted.momocity.auth.presentation.api.request.*;
import com.wanted.momocity.auth.presentation.api.response.*;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name="signup", description = "자체 회원가입을 위한 Auth api")
public class AuthController {

    private final StudentSignupUsecase studentSignupUsecase;
    private final TeacherSignupUseCase teacherSignupUseCase;
    private final LoginUsecase loginUsecase;
    private final LoginCompletedUsecase loginCompletedUsecase;
    private final EmailSendUsecase emailSendUsecase;
    private final EmailVerifyUsecase emailVerifyUsecase;
    private final TempPasswordUsecase tempPasswordUsecase;

    private final S3UploadPort s3UploadPort;


    @PostMapping("/signup/student")
    @Operation(
            summary = "학생 자체 회원가입",
            description = "해당 api를 통해 회원가입 한 사람의 role을 STUDENT로 하여 user테이블에 추가하는 메서드"
    )
    public ResponseEntity<ApiResponse<Void>> studentSignup (
            @Valid @RequestBody StudentSignupRequest request){

        studentSignupUsecase.signup(new StudentSignupCommand(request.email(),request.password(),request.name()));

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
    public ResponseEntity<ApiResponse<Void>> teacherSignup (
            @Valid @ModelAttribute TeacherSignupRequest request){

        String proofUrl = s3UploadPort.upload(request.proof());  // 여기서 선언하고 값 넣어줌

        teacherSignupUseCase.signup(new TeacherSignupCommand(request.email(),request.password(),request.name(),request.category(),proofUrl));

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
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request){

        LoginResponse result =loginUsecase.login(new LoginCommand(request.email(),request.password()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        AuthResponseCode.SUCCESS,
                        AuthResponseMessage.LOGIN_SUCCESS,
                        new LoginResponse(result.accessToken(), result.refreshToken(), result.expiresIn())
                ));

    }


    @GetMapping("/login/completed")
    @Operation(
            summary = "로그인 성공 시 페이지 로딩을 위한 사용자 정보 전달",
            description = "로그인 한 사용자의 role, is_tempPWD, nickname 을 보내 그에 맞는 페이지를 랜더링한다"
    )
    public ResponseEntity<ApiResponse<LoginCompletedResponse>> loginCompleted(
            @AuthenticationPrincipal UserDetails userDetails) {

        LoginCompletedResponse result = loginCompletedUsecase.getInfo(userDetails.getUsername()); // email

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        AuthResponseCode.SUCCESS,
                        AuthResponseMessage.LOGIN_COMPLETED,
                        new LoginCompletedResponse(result.role(), result.is_tempPwd(), result.nickname())
                ));
    }


    @PostMapping("/email/send")
    @Operation(
            summary = "이메일로 인증코드 발송",
            description = "이메일 중복 확인 및 본인 인증을 위한 이메일 인증 코드 발송"
    )
    public ResponseEntity<ApiResponse<EmailSendResponse>> emailSend(
            @Valid @RequestBody EmailSendRequest request){

        EmailSendResponse result = emailSendUsecase.emailSend(new EmailSendCommand(request.email()));

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
    public ResponseEntity<ApiResponse<EmailVerifyResponse>> emailVerify(
            @Valid @RequestBody EmailVerifyRequest request){

       emailVerifyUsecase.emailVerify(new EmailVerifyCommand(request.email(), request.code()));

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
    public ResponseEntity<ApiResponse<Void>> tempPasswordSend(
        @Valid @RequestBody EmailSendRequest request){

        tempPasswordUsecase.sendTempPassword(new EmailSendCommand(request.email()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        AuthResponseCode.SUCCESS,
                        AuthResponseMessage.TEMP_PASSWORD_CREATED,
                        null
                ));
    }


}
