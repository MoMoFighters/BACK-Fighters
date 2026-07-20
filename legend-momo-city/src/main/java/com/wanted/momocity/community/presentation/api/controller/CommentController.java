package com.wanted.momocity.community.presentation.api.controller;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.community.application.comment.usecase.CommentCommandUseCase;
import com.wanted.momocity.community.application.comment.usecase.CommentQueryUseCase;
import com.wanted.momocity.community.presentation.api.common.CommunityResponseCode;
import com.wanted.momocity.community.presentation.api.request.CreateCommentRequest;
import com.wanted.momocity.community.presentation.api.response.PostCommentResponse;
import com.wanted.momocity.community.presentation.api.response.PostReplyResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/*
 * comment.
 *  댓글 / 대댓글 HTTP 요청 처리
 *  비즈니스 로직 없음, HTTP 반환만 담당
 */

@Tag(name = "Comment", description = "Community 도메인 - 댓글/대댓글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/posts")
public class CommentController {

    private final CommentCommandUseCase commentCommandUseCase;
    private final CommentQueryUseCase commentQueryUseCase;

    // 댓글 작성
    // POST /api/v2/posts/{postId}/comments
    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<Void>> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CreateCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        commentCommandUseCase.createComment(userId, postId, request.content());

        return ResponseEntity.status(201).body(ApiResponse.created(
                CommunityResponseCode.COMMENT_CREATED,
                "댓글이 작성되었습니다.",
                null
        ));
    }

    // 댓글 삭제
    // DELETE /api/v2/posts/{postId}/comments/{commentId}
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        commentCommandUseCase.deleteComment(userId, postId, commentId);

        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.COMMENT_DELETED,
                "댓글이 삭제되었습니다."
        ));
    }

    // 대댓글 작성
    // POST /api/v2/posts/{postId}/comments/{commentId}/replies
    @Operation(summary = "대댓글 작성", description = "댓글에 대댓글을 작성합니다.")
    @PostMapping("/{postId}/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<Void>> createReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody @Valid CreateCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        commentCommandUseCase.createReply(userId, postId, commentId, request.content());

        return ResponseEntity.status(201).body(ApiResponse.created(
                CommunityResponseCode.REPLY_CREATED,
                "대댓글이 작성되었습니다.",
                null
        ));
    }

    // 대댓글 삭제
    // DELETE /api/v2/posts/{postId}/comments/{commentId}/replies/{replyId}
    @Operation(summary = "대댓글 삭제", description = "대댓글을 삭제합니다.")
    @DeleteMapping("/{postId}/comments/{commentId}/replies/{replyId}")
    public ResponseEntity<ApiResponse<Void>> deleteReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @PathVariable Long replyId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        commentCommandUseCase.deleteReply(userId, postId, commentId, replyId);

        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.REPLY_DELETED,
                "대댓글이 삭제되었습니다."
        ));
    }

    // 댓글 목록 조회
    // GET /api/v2/posts/{postId}/comments
    @Operation(summary = "댓글 목록 조회", description = "게시글 댓글 목록을 조회합니다.")
    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<PostCommentResponse>> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal(errorOnInvalidType = false) CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;

        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.COMMENT_FOUND,
                "댓글 조회에 성공했습니다.",
                commentQueryUseCase.getComments(userId, postId, cursor, size)
        ));
    }

    // 대댓글 목록 조회
    // GET /api/v2/posts/{postId}/comments/{commentId}/replies
    @Operation(summary = "대댓글 목록 조회", description = "댓글의 대댓글 목록을 조회합니다.")
    @GetMapping("/{postId}/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<PostReplyResponse>> getReplies(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal(errorOnInvalidType = false) CustomUserDetails userDetails
    ) {

        Long userId = userDetails != null ? userDetails.getUserId() : null;

        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.COMMENT_FOUND,
                "대댓글 조회에 성공했습니다.",
                commentQueryUseCase.getReplies(userId, postId, commentId, cursor, size)
        ));

    }

}
