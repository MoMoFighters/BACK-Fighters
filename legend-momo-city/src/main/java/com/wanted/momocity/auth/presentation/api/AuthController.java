package com.wanted.momocity.auth.presentation.api;

import com.wanted.momocity.auth.application.command.StudentSignupCommand;
import com.wanted.momocity.auth.application.command.TeacherSignupCommand;
import com.wanted.momocity.auth.application.usecase.StudentSignupUsecase;
import com.wanted.momocity.auth.application.usecase.TeacherSignupUseCase;
import com.wanted.momocity.auth.presentation.api.request.StudentSignupRequest;
import com.wanted.momocity.auth.presentation.api.request.TeacherSignupRequest;
import com.wanted.momocity.auth.presentation.api.response.SignupResponseCode;
import com.wanted.momocity.auth.presentation.api.response.SignupResponseMessage;
import com.wanted.momocity.auth.presentation.api.response.StudentSignupResponse;
import com.wanted.momocity.auth.presentation.api.response.TeacherSignupResponse;
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

    private final StudentSignupUsecase studentSignupUsecase;
    private final TeacherSignupUseCase teacherSignupUseCase;

    public AuthController(StudentSignupUsecase studentSignupUsecase, TeacherSignupUseCase teacherSignupUseCase) {
        this.studentSignupUsecase = studentSignupUsecase;
        this.teacherSignupUseCase = teacherSignupUseCase;
    }


    @PostMapping("/signup/student")
    @Operation(
            summary = "학생 자체 회원가입",
            description = "해당 api를 통해 회원가입 한 사람의 role을 STUDENT로 하여 user테이블에 추가하는 메서드"
    )
    public ResponseEntity<ApiResponse<StudentSignupResponse>> studentSignup (
            @Valid @RequestBody StudentSignupRequest request){

        studentSignupUsecase.signup(new StudentSignupCommand(request.email(),request.password(),request.name()));

        return ResponseEntity.status(HttpStatus.CREATED) //201
                .body(ApiResponse.created(
                        SignupResponseCode.CREATED,
                        SignupResponseMessage.STUDENT_CREATED,
                        null
                ));
    }


    @PostMapping("/signup/teacher")
    @Operation(
            summary = "강사 자체 회원가입",
            description = "해당 api를 통해 회원가입 한 사람의 role을 TEACHER로, status는 PENDING으로 하여 user테이블에 추가하는 메서드"
    )
    public ResponseEntity<ApiResponse<TeacherSignupResponse>> teacherSignup (
            @Valid @RequestBody TeacherSignupRequest request){

        teacherSignupUseCase.signup(new TeacherSignupCommand(request.email(),request.password(),request.name(),request.category(),request.proof()));

        return ResponseEntity.status(HttpStatus.CREATED) //201
                .body(ApiResponse.created(
                        SignupResponseCode.CREATED,
                        SignupResponseMessage.TEACHER_CREATED,
                        null
                ));
    }

}
