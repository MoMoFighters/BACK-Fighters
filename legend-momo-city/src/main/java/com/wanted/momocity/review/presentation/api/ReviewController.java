package com.wanted.momocity.review.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.review.application.command.ReviewCommand;
import com.wanted.momocity.review.application.query.ReviewQuery;
import com.wanted.momocity.review.application.usecase.ReviewCommandUseCase;
import com.wanted.momocity.review.application.usecase.ReviewQueryUseCase;
import com.wanted.momocity.review.presentation.api.request.CreateReviewRequest;
import com.wanted.momocity.review.presentation.api.response.CreateReviewResponse;
import com.wanted.momocity.review.presentation.api.response.CreateReviewSuccessResponse;
import com.wanted.momocity.review.presentation.api.response.ReviewListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// 수강평 Controller
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lectures")
@Tag(name = "Review", description = "수강평 등록, 삭제, 목록 조회 API")
public class ReviewController {

    private final ReviewCommandUseCase reviewCommandUseCase;
    private final ReviewQueryUseCase reviewQueryUseCase;

    @Operation(
            summary = "수강평 등록",
            description = "로그인한 학생이 수강 중인 강의에 수강평을 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "수강평 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수강평 등록 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의 또는 사용자 정보를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 ACTIVE 상태의 수강평을 작성한 강의")
    })
    @PostMapping("/{lectureId}/reviews")
    public ResponseEntity<CreateReviewSuccessResponse> createReview(
            // 로그인한 사용자 정보를 가져오기
            @AuthenticationPrincipal CustomUserDetails userDetails,
            // 강의 Id
            @PathVariable Long lectureId,
            // 별점, 내용
            @Valid@RequestBody CreateReviewRequest request
    ) {
        Long userId = userDetails.getUserId();

        ReviewCommand.CreateReviewCommand command = new ReviewCommand.CreateReviewCommand(
                lectureId,
                userId,
                request.rating(),
                request.content()
        );

        // 수강평 등록 usecase 호출
        reviewCommandUseCase.createReview(command);

        // 201 성공 코드 및 응답
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateReviewSuccessResponse.created());
    }

    // 강의 삭제
    @Operation(
            summary = "수강평 삭제",
            description = "관리자가 수강평을 삭제합니다. 실제 row를 삭제하지 않고 DELETED 상태로 변경합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수강평 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "삭제 가능한 ACTIVE 수강평을 찾을 수 없음")
    })
    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // 관리자만 삭제 가능
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId // URL에서 reviewId 추출
    ) {
        reviewCommandUseCase.deleteReview(reviewId); // 서비스에 삭제 요청 위임

        return ResponseEntity.ok(CreateReviewSuccessResponse.deleted()); // 삭제 성공 응답 반환
    }

    // 강의 수강평 목록 조회
    @Operation(
            summary = "수강평 목록 조회",
            description = "특정 강의의 ACTIVE 상태 수강평 목록을 최신순으로 페이지 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수강평 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 페이지 또는 조회 조건"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음")
    })
    @GetMapping("/{lectureId}/reviews")
    public ResponseEntity<ApiResponse<ReviewListResponse>> getReviews(
            @PathVariable Long lectureId,
            @RequestParam(defaultValue = "1") @Min(1) int page, // 요청한 페이지 번호, 기본값 = 0
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size // 한 페이지 요청 수강평 개수, 기본값 = 10

    ) {
        // 서비스로 넘길 수강평 목록 조회 query 생성
        ReviewQuery.GetReviewListQuery query = new ReviewQuery.GetReviewListQuery(
                lectureId,
                page,
                size
        );

        // 수강평 목록 조회 UseCase 호출
        ReviewListResponse response = reviewQueryUseCase.getReviews(query);

        return ResponseEntity.ok(
                ApiResponse.success(
                        ApiResponseCode.SUCCESS,
                        "수강평 목록 조회에 성공했습니다.",
                        response
                )
        );
    }
}
