package com.wanted.momocity.enrollment.presentation.api;

import com.wanted.momocity.enrollment.application.command.CreateEnrollmentCommand;
import com.wanted.momocity.enrollment.application.usecase.EnrollmentCommandUseCase;
import com.wanted.momocity.enrollment.domain.model.Enrollment;
import com.wanted.momocity.enrollment.presentation.api.response.CreateEnrollmentResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// EnrollmentController는 수강신청 API 요청을 받는 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lectures")
@Tag(name = "Enrollment", description = "수강신청 API")
public class EnrollmentController {

    // 수강신청 생성 UseCase
    // 실제 비즈니스 로직은 EnrollmentCommandService가 처리
    private final EnrollmentCommandUseCase enrollmentCommandUseCase;


    @Operation(
            summary = "수강신청",
            description = "로그인한 학생이 특정 강의를 수강신청합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "수강신청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 건물 위치값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "학생 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 수강신청한 강의")
    })
    // 수강신청 API
    @PostMapping("/{lectureId}/enrollments")
    public ResponseEntity<ApiResponse<CreateEnrollmentResponse>> createEnrollment(
            Authentication authentication,
            @PathVariable Long lectureId,
            // 건물 없을 때 사용할 위치 값, 있으면 사용 X
            @RequestParam(required = false) Long position
    ) {
        // Authorization 토큰에서 꺼낸 로그인 사용자 email
        Long studentId = Long.parseLong(authentication.getName());

        // 수강신청에 필요한 값을 Command로 묶는다.
        CreateEnrollmentCommand command = new CreateEnrollmentCommand(
                studentId,
                lectureId,
                position
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
