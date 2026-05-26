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
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name="signup", description = "자체 회원가입을 위한 Auth api")
public class AuthController {

    private final StudentSignupUsecase studentSignupUsecase;
    private final TeacherSignupUseCase teacherSignupUseCase;
    private final LoginUsecase loginUsecase;
    private final EmailSendUsecase emailSendUsecase;
    private final EmailVerifyUsecase emailVerifyUsecase;
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



}
