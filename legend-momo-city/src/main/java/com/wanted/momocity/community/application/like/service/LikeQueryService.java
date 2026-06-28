package com.wanted.momocity.community.application.like.service;

import com.wanted.momocity.community.application.like.usecase.LikeQueryUseCase;
import com.wanted.momocity.community.application.post.port.UserInfoPort;
import com.wanted.momocity.community.domain.exception.CommunityNotFoundException;
import com.wanted.momocity.community.domain.model.PostLike;
import com.wanted.momocity.community.domain.repository.PostLikeRepository;
import com.wanted.momocity.community.presentation.api.response.PostLikeListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * comment.
 *  좋아요 읽기 작업 UseCase 구현체
 *  -> 좋아요 누른 사용자 목록 조회
 */

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeQueryService implements LikeQueryUseCase {

    private final PostLikeRepository postLikeRepository;
    private final UserInfoPort userInfoPort;

    // 좋아요 누른 사용자 목록 조회
    @Override
    public PostLikeListResponse getLikes(Long postId) {

        // 게시글 좋아요 누른 사용자 목록 전체 조회
        List<PostLike> likes = postLikeRepository.findAllByPostId(postId);

        // 좋아요 누른 사용자 정보 변환 (userId 기준 유저 정보 조회)
        List<PostLikeListResponse.LikeUserItem> users = likes.stream()
                .map(like -> {
                    // 좋아요 누른 사용자 정보 조회
                    var user = userInfoPort.findById(like.getUserId())
                            .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));
                    return new PostLikeListResponse.LikeUserItem(
                            user.getId(),
                            user.getName(),
                            user.getProfileImageUrl(),
                            user.getRole().name()
                    );
                })
                .toList();

        log.info("[Community] 좋아요 목록 조회 완료 | postId={}, totalCount={}", postId, likes.size());

        return new PostLikeListResponse(likes.size(), users);
    }

}
