package com.wanted.cleanarchitecture.enrollment.presentation.api;

import com.wanted.cleanarchitecture.enrollment.application.command.EnrollCourseCommand;
import com.wanted.cleanarchitecture.enrollment.application.usecase.EnrollmentCommandUseCase;
import com.wanted.cleanarchitecture.enrollment.application.usecase.EnrollmentQueryUseCase;
import com.wanted.cleanarchitecture.enrollment.presentation.api.request.EnrollCourseRequest;
import com.wanted.cleanarchitecture.enrollment.presentation.api.response.EnrollCourseResponse;
import com.wanted.cleanarchitecture.global.presentation.api.common.ApiResponse;
import com.wanted.cleanarchitecture.global.presentation.api.common.ApiResponseCode;
import com.wanted.cleanarchitecture.global.presentation.api.common.ApiResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
@Tag(name = "Enrollment", description = "공개된 강의에 대한 수강 신청과 수강 상태 조회를 처리하는 Enrollment Context API")
public class EnrollmentController {

    private final EnrollmentCommandUseCase enrollmentCommandUseCase;
    private final EnrollmentQueryUseCase enrollmentQueryUseCase;

    public EnrollmentController(EnrollmentCommandUseCase enrollmentCommandUseCase, EnrollmentQueryUseCase enrollmentQueryUseCase) {
        this.enrollmentCommandUseCase = enrollmentCommandUseCase;
        this.enrollmentQueryUseCase = enrollmentQueryUseCase;
    }

    @GetMapping("/{enrollmentId}")
    @Operation(
            summary = "수강 신청 단건 조회",
            description = "수강 신청 ID로 Enrollment 상태를 조회한다."
    )
    public ResponseEntity<ApiResponse<EnrollmentQueryUseCase.EnrollmentView>> getEnrollment(
            @Parameter(description = "조회할 수강 신청 ID", example = "1")
            @PathVariable Long enrollmentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                enrollmentQueryUseCase.handle(enrollmentId)
        ));
    }

    @PostMapping
    @Operation(
            summary = "수강 신청",
            description = "사용자가 공개된 강의에 수강 신청한다. 강의 공개 여부는 CourseCatalogPort를 통해 Catalog Context에 조회한다."
    )
    public ResponseEntity<ApiResponse<EnrollCourseResponse>> enroll(@Valid @RequestBody EnrollCourseRequest request) {
        Long enrollmentId = enrollmentCommandUseCase.handle(new EnrollCourseCommand(request.userId(), request.courseId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.ENROLLMENT_CREATED,
                        ApiResponseMessage.ENROLLMENT_CREATED,
                        new EnrollCourseResponse(enrollmentId)
                ));
    }
}
