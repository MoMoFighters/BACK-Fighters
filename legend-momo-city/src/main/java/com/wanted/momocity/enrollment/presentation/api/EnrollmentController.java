package com.wanted.momocity.enrollment.presentation.api;

import com.wanted.momocity.enrollment.application.command.CreateEnrollmentCommand;
import com.wanted.momocity.enrollment.application.usecase.EnrollmentCommandUseCase;
import com.wanted.momocity.enrollment.domain.model.Enrollment;
import com.wanted.momocity.enrollment.presentation.api.response.CreateEnrollmentResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// EnrollmentController는 수강신청 API 요청을 받는 컨트롤러
@RestController
@RequiredArgsConstructor
public class EnrollmentController {

    // 수강신청 생성 UseCase
    // 실제 비즈니스 로직은 EnrollmentCommandService가 처리
    private final EnrollmentCommandUseCase enrollmentCommandUseCase;

    // 수강신청 API
    @PostMapping("/api/v1/lectures/{lectureId}/enrollments")
    public ResponseEntity<ApiResponse<CreateEnrollmentResponse>> createEnrollment(
            Authentication authentication,
            @PathVariable Long lectureId
    ) {
        // Authorization 토큰에서 꺼낸 로그인 사용자 email
        String userEmail = authentication.getName();

        // 수강신청에 필요한 값을 Command로 묶는다.
        CreateEnrollmentCommand command = new CreateEnrollmentCommand(
                userEmail,
                lectureId
        );

        // 수강신청 비즈니스 로직을 실행
        Enrollment enrollment = enrollmentCommandUseCase.createEnrollment(command);

        // 수강신청 성공 응답을 201 Created로 반환
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        "수강신청이 완료되었습니다.",
                        CreateEnrollmentResponse.from(enrollment)
                ));
    }
}