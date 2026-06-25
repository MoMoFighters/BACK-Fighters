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
public class ReviewController {

    private final ReviewCommandUseCase reviewCommandUseCase;
    private final ReviewQueryUseCase reviewQueryUseCase;

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
    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // 관리자만 삭제 가능
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId // URL에서 reviewId 추출
    ) {
        reviewCommandUseCase.deleteReview(reviewId); // 서비스에 삭제 요청 위임

        return ResponseEntity.ok(CreateReviewSuccessResponse.deleted()); // 삭제 성공 응답 반환
    }

    // 강의 수강평 목록 조회
    @GetMapping("/{lectureId}/reviews")
    public ResponseEntity<ApiResponse<ReviewListResponse>> getReviews(
            @PathVariable Long lectureId,
            @RequestParam(defaultValue = "0") @Min(0) int page, // 요청한 페이지 번호, 기본값 = 0
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
