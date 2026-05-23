package com.wanted.momocity.auth.presentation.api;

import com.wanted.momocity.auth.application.command.StudentSignupCommand;
import com.wanted.momocity.auth.application.usecase.StudentSignupCommandUsecase;
import com.wanted.momocity.auth.presentation.api.request.StudentSignupRequest;
import com.wanted.momocity.auth.presentation.api.response.SignupResponseCode;
import com.wanted.momocity.auth.presentation.api.response.SignupResponseMessage;
import com.wanted.momocity.auth.presentation.api.response.StudentSignupResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name="signup", description = "자체 회원가입을 위한 Auth api")
public class AuthController {

    private final StudentSignupCommandUsecase studentSignupCommandUsecase;

    public AuthController(StudentSignupCommandUsecase studentSignupCommandUsecase) {
        this.studentSignupCommandUsecase = studentSignupCommandUsecase;
    }


    @PostMapping("/signup/student")
    @Operation(
            summary = "학생 자체 회원가입",
            description = "해당 api를 통해 회원가입 한 사람의 role을 STUDENT로 하여 user테이블에 추가하는 메서드"
    )
    public ResponseEntity<ApiResponse<StudentSignupResponse>> studentSignup (
            @Valid @RequestBody StudentSignupRequest request){

        studentSignupCommandUsecase.signup(new StudentSignupCommand(request.email(),request.password(),request.name()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        SignupResponseCode.CREATED,
                        SignupResponseMessage.CREATED,
                        null
                ));
    }

}
