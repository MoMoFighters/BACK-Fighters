package com.wanted.momocity.enrollment.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.enrollment.application.query.EnrollmentQuery.GetEnrollmentProgressQuery;
import com.wanted.momocity.enrollment.application.usecase.EnrollmentQueryUsecase;
import com.wanted.momocity.enrollment.presentation.api.response.EnrollmentProgressResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/enrollments")
public class EnrollmentProgressController {

    private final EnrollmentQueryUsecase enrollmentQueryUsecase;

    // 강의 진척도 (건물)
    @GetMapping("/progress")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<EnrollmentProgressResponse>> getProgress(
            // 로그인한 사용자 정보를 주입받습니다.
            @AuthenticationPrincipal CustomUserDetails userDetails,
            // 카테고리가 없으면 전체 수강 강의 조회
            @RequestParam(required = false) String category
    ) {
        // 로그인한 사용자의 Id를 꺼낸다.
        Long userId = userDetails.getUserId();

        // Controller에서 받은 값을 Application 계층으로 넘기기 위한 Query 객체로 묶는다.
        GetEnrollmentProgressQuery query = new GetEnrollmentProgressQuery(
                userId,
                category
        );
        // UseCase를 호출해 학습 진척도 응답 데이터를 조회한다.
        EnrollmentProgressResponse response = enrollmentQueryUsecase.getProgress(query);

        // 공통 성공 응답 형식으로 반환
        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "강의 진척도 조회에 성공했습니다.",
                response
        ));


    }
}
