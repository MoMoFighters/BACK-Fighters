package com.wanted.momocity.review.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.review.application.command.ReviewCommand;
import com.wanted.momocity.review.application.usecase.ReviewCommandUseCase;
import com.wanted.momocity.review.presentation.api.request.CreateReviewRequest;
import com.wanted.momocity.review.presentation.api.response.CreateReviewResponse;
import com.wanted.momocity.review.presentation.api.response.CreateReviewSuccessResponse;
import jakarta.validation.Valid;
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
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // 관리자만 삭제 가능
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId // URL에서 reviewId 추출
    ) { // 메서드 시작
        reviewCommandUseCase.deleteReview(reviewId); // 서비스에 삭제 요청 위임

        return ResponseEntity.ok(CreateReviewSuccessResponse.deleted()); // 삭제 성공 응답 반환
    } // 메서드 종료
}
