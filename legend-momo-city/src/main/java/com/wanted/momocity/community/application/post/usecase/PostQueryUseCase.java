package com.wanted.momocity.community.application.post.usecase;

import com.wanted.momocity.community.domain.model.PostCategory;
import com.wanted.momocity.community.presentation.api.response.*;

/*
* comment.
*  게시글 읽기 작업 전용 UseCase 인터페이스
*  - 게시글 목록 조회, 단건 조회
*  - 마이페이지, 상대방 페이지, 대시보드
* */

public interface PostQueryUseCase {

    // 게시글 목록 조회 (카테고리 필터링, 커서 기반 페이지네이션)
    PostListResponse getPosts(Long userId, PostCategory category, Long cursor, int size);

    // 게시글 단건 조회 (contents 포함, comments 미포함)
    PostDetailResponse getPost(Long userId, Long PostId);

    // 마이페이지 - 내 게시글 목록 (커서 기반 페이지네이션)
    UserPostListResponse getMyPosts(Long userId, Long cursor, int size);

    // 상대방 페이지 - 상대방 게시글 목록 (커서 기반 페이지네이션)
    UserPostListResponse getUserPosts(Long targetUserId, Long cursor, int size);

    // 대시보드 - 내 게시글 통계
    DashboardResponse getDashboard(Long userId);

    // 게시글 키워드 검색 (커서 기반 페이지네이션)
    UserPostListResponse searchPosts(String keyword, PostCategory category, Long cursor, int size);

    // 연관 게시글 추천 (같은 카테고리 인기글 + 같은 작성자 최신글)
    PostRecommendationResponse getRecommendations(Long postId);

}
