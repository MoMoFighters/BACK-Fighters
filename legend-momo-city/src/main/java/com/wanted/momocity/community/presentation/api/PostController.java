package com.wanted.momocity.community.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.community.application.post.command.PostContentCommand;
import com.wanted.momocity.community.application.post.result.PostCreateResult;
import com.wanted.momocity.community.application.post.usecase.PostCommandUseCase;
import com.wanted.momocity.community.application.post.usecase.PostQueryUseCase;
import com.wanted.momocity.community.domain.exception.CommunityAccessDeniedException;
import com.wanted.momocity.community.domain.model.PostCategory;
import com.wanted.momocity.community.presentation.api.common.CommunityResponseCode;
import com.wanted.momocity.community.presentation.api.request.*;
import com.wanted.momocity.community.presentation.api.response.*;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/*
 * comment.
 *  게시글 HTTP 요청 처리
 *  비즈니스 로직 없음, HTTP 반환만 담당
 */

@Tag(name = "Community", description = "Community 도메인 - 게시글 CRUD 및 댓글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/posts")
public class PostController {

    private final PostCommandUseCase postCommandUseCase;
    private final PostQueryUseCase postQueryUseCase;

    // 게시글 작성
    // POST /api/v2/posts
    @Operation(summary = "게시글 작성", description = "제목과 카테고리로 게시글을 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PostCreateResponse>> createPost(
            @RequestBody @Valid CreatePostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        PostCreateResult result = postCommandUseCase.createPost(userId, request.title(), request.category(),request.thumbnailUrl());

        return ResponseEntity.status(201).body(ApiResponse.created(
                CommunityResponseCode.POST_CREATED,
                "게시글이 작성되었습니다.",
                new PostCreateResponse(result.postId())
        ));
    }

    // 게시글 이미지 업로드
    // POST /api/v2/posts/images
    @Operation(summary = "이미지 업로드", description = "게시글 이미지를 S3에 업로드하고 CloudFront URL을 반환합니다.")
    @PostMapping("/images")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @RequestPart MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String imageUrl = postCommandUseCase.uploadImage(image);
        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.IMAGE_UPLOADED,
                "이미지가 업로드되었습니다.",
                imageUrl
        ));
    }

    // 게시글 콘텐츠 업로드
    // POST /api/v2/posts/{postId}/contents
    @Operation(summary = "게시글 콘텐츠 업로드", description = "게시글 콘텐츠를 업로드합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업로드 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    @PostMapping("/{postId}/contents")
    public ResponseEntity<ApiResponse<Void>> uploadContents(
            @PathVariable Long postId,
            @RequestBody @Valid UploadContentsRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();

        List<PostContentCommand> commands = request.contents().stream()
                .map(c -> new PostContentCommand(
                        c.type(), c.content(), c.imageUrl()
                ))
                .toList();

        postCommandUseCase.uploadContents(userId, postId, request.thumbnailUrl(), commands);

        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.POST_CONTENT_UPLOADED,
                "게시글 콘텐츠가 업로드되었습니다."
        ));
    }

    // 게시글 목록 조회
    // GET /api/v2/posts?category=STUDY&page=0&size=10
    @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PostListResponse>> getPosts(
            @RequestParam(required = false) PostCategory category,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal (errorOnInvalidType = false) CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;

        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.POST_LIST_FOUND,
                "게시글 목록 조회에 성공했습니다.",
                postQueryUseCase.getPosts(userId, category, cursor, size)
        ));
    }

    // 게시글 단건 조회
    // GET /api/v2/posts/{postId}
    @Operation(summary = "게시글 단건 조회", description = "게시글을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(
            @PathVariable Long postId,
            @RequestParam(required = false) PostCategory category,
            @AuthenticationPrincipal (errorOnInvalidType = false) CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;

        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.POST_FOUND,
                "게시글 조회에 성공했습니다.",
                postQueryUseCase.getPost(userId, postId)
        ));
    }

    // 게시글 제목/카테고리 수정
    // PATCH /api/v2/posts/{postId}
    @Operation(summary = "게시글 수정", description = "게시글 제목과 카테고리를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @PathVariable Long postId,
            @RequestBody @Valid UpdatePostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        postCommandUseCase.updatePost(userId, postId, request.title(), request.category());

        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.POST_UPDATED,
                "게시글이 수정되었습니다."
        ));
    }

    // 게시글 콘텐츠 수정
    // PUT /api/v2/posts/{postId}/contents
    @Operation(summary = "게시글 콘텐츠 수정", description = "게시글 콘텐츠를 전체 교체합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    @PutMapping("/{postId}/contents")
    public ResponseEntity<ApiResponse<Void>> updateContents(
            @PathVariable Long postId,
            @RequestBody @Valid UploadContentsRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();

        List<PostContentCommand> commands = request.contents().stream()
                .map(c -> new PostContentCommand(
                        c.type(), c.content(), c.imageUrl()
                ))
                .toList();

        postCommandUseCase.updateContents(userId, postId, request.thumbnailUrl(), commands);

        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.POST_CONTENT_UPDATED,
                "게시글 콘텐츠가 수정되었습니다."
        ));
    }

    // 게시글 삭제
    // DELETE /api/v2/posts/{postId}
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        postCommandUseCase.deletePost(userId, postId);

        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.POST_DELETED,
                "게시글이 삭제되었습니다."
        ));
    }

    // 마이페이지 - 내 게시글 목록 기반
    // GET api/v2/posts/me?cursor={lastPostId}&size=10
    @Operation(summary = "마이페이지 게시글 목록", description = "내 게시글 목록을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserPostListResponse>> getMyPosts(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.POST_LIST_FOUND,
                "내 게시글 목록 조회에 성공했습니다.",
                postQueryUseCase.getMyPosts(userId, cursor, size)
        ));
    }

    // 상대방 페이지 - 상대방 게시글 목록
    // GET api/v2/posts/users/{targetUserId}?cursor={lastPostId}&size=10
    @Operation(summary = "상대방 게시글 목록", description = "상대방 게시글 목록을 조회합니다.")
    @GetMapping("/users/{targetUserId}")
    public ResponseEntity<ApiResponse<UserPostListResponse>> getUserPosts(
            @PathVariable Long targetUserId,
            @RequestParam(required = false)  Long cursor,
            @RequestParam(defaultValue = "10")  int size,
            @AuthenticationPrincipal (errorOnInvalidType = false) CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;

        // 본인 페이지 접근 방지
        if (targetUserId.equals(userId)) {
            throw new CommunityAccessDeniedException("본인 페이지는 마이페이지에서 확인해주세요.");
        }

        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.POST_LIST_FOUND,
                "게시글 목록 조회에 성공했습니다.",
                postQueryUseCase.getUserPosts(targetUserId, cursor, size)
        ));
    }

    // 대시보드 - 내 게시글 통계
    // GET api/v2/posts/me/dashboard
    @Operation(summary = "대시보드", description = "내 게시글 통계를 조회합니다.")
    @GetMapping("/me/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.POST_LIST_FOUND,
                "대시보드 조회에 성공했습니다.",
                postQueryUseCase.getDashboard(userId)
        ));

    }

    // 상대방 대시보드
    // GET /api/v2/posts/users/{targetUserId}/dashboard
    @Operation(summary = "상대방 대시보드", description = "상대방 게시글 통계를 조회합니다.")
    @GetMapping("/users/{targetUserId}/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getUserDashboard(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal (errorOnInvalidType = false) CustomUserDetails userDetails
    ) {

        Long userId = userDetails != null ? userDetails.getUserId() : null;

        // 본인 페이지 접근 방지
        if (targetUserId.equals(userId)) {
            throw new CommunityAccessDeniedException("본인 페이지는 마이페이지에서 확인해주세요.");
        }

        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.POST_LIST_FOUND,
                "대시보드 조회에 성공했습니다.",
                postQueryUseCase.getDashboard(targetUserId)
        ));
    }

    // 게시글 키워드 검색
    // GET /api/v2/posts/search?keyword={keyword}&cursor={lastPostId}&size=10
    @Operation(summary = "게시글 검색", description = "키워드로 게시글을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<UserPostListResponse>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(required = false) PostCategory category,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal (errorOnInvalidType = false) CustomUserDetails userDetails
    ) {
        // 공백 검증
        if (keyword == null || keyword.isBlank()) {
            throw new DomainRuleViolationException("검색 키워드를 입력해주세요.");
        }

        // 최소 길이 검증
        if (keyword.trim().length() < 2) {
            throw new DomainRuleViolationException("검색 키워드는 2자 이상 입력해주세요.");
        }

        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.POST_SEARCH_FOUND,
                "게시글 검색에 성공했습니다.",
                postQueryUseCase.searchPosts(keyword, category, cursor, size)
        ));
    }

    // 연관 게시글 추천
    // GET /api/v2/posts/{postId}/recommendations
    @Operation(summary = "연관 게시글 추천", description = "같은 카테고리 인기글 + 작성자의 다른 글을 추천합니다.")
    @GetMapping("/{postId}/recommendations")
    public ResponseEntity<ApiResponse<PostRecommendationResponse>> getRecommendations(
            @PathVariable Long postId,
            @AuthenticationPrincipal (errorOnInvalidType = false) CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                CommunityResponseCode.POST_RECOMMENDATIONS_FOUND,
                "연관 게시글 조회에 성공했습니다.",
                postQueryUseCase.getRecommendations(postId)
        ));
    }

}