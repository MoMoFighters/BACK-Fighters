package com.wanted.momocity.community.presentation.api.controller;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.community.application.like.usecase.LikeCommandUseCase;
import com.wanted.momocity.community.application.like.usecase.LikeQueryUseCase;
import com.wanted.momocity.community.application.like.result.LikeResult;
import com.wanted.momocity.community.presentation.api.common.CommunityResponseCode;
import com.wanted.momocity.community.presentation.api.response.LikeResponse;
import com.wanted.momocity.community.presentation.api.response.PostLikeListResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/*
 * comment.
 *  좋아요 HTTP 요청 처리
 *  비즈니스 로직 없음, HTTP 반환만 담당
 */

@Tag(name = "Like", description = "Community 도메인 - 좋아요 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/posts")
public class LikeController {

    private final LikeCommandUseCase likeCommandUseCase;
    private final LikeQueryUseCase likeQueryUseCase;

    // 좋아요
    // POST /api/v2/posts/{postId}/likes
    @Operation(summary = "좋아요", description = "게시글에 좋아요를 누릅니다.")
    @PostMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<LikeResponse>> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        LikeResult result = likeCommandUseCase.likePost(userId, postId);

        return ResponseEntity.status(201).body(ApiResponse.created(
                CommunityResponseCode.LIKE_CREATED,
                "좋아요를 눌렀습니다.",
                new LikeResponse(result.postId(), result.likeCount(), result.isLiked())
        ));
    }

    // 좋아요 취소
    // DELETE /api/v2/posts/{postId}/likes
    @Operation(summary = "좋아요 취소", description = "게시글 좋아요를 취소합니다.")
    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<LikeResponse>> unlikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        LikeResult result = likeCommandUseCase.unlikePost(userId, postId);

        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.LIKE_DELETED,
                "좋아요를 취소했습니다.",
                new LikeResponse(result.postId(), result.likeCount(), result.isLiked())
        ));
    }

    // 좋아요 누른 사용자 목록 조회
    // GET /api/v2/posts/{postId}/likes
    @Operation(summary = "좋아요 목록 조회", description = "게시글에 좋아요를 누른 사용자 목록을 조회합니다.")
    @GetMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<PostLikeListResponse>> getLikes(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.LIKE_LIST_FOUND,
                "좋아요 목록 조회에 성공했습니다.",
                likeQueryUseCase.getLikes(postId)
        ));
    }

}
