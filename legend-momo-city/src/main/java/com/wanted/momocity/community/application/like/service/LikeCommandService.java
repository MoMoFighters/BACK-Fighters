package com.wanted.momocity.community.application.like.service;

import com.wanted.momocity.community.application.like.usecase.LikeCommandUseCase;
import com.wanted.momocity.community.application.post.port.UserInfoPort;
import com.wanted.momocity.community.application.like.result.LikeResult;
import com.wanted.momocity.community.domain.event.PostLikedEvent;
import com.wanted.momocity.community.domain.exception.CommunityAccessDeniedException;
import com.wanted.momocity.community.domain.exception.CommunityNotFoundException;
import com.wanted.momocity.community.domain.model.Post;
import com.wanted.momocity.community.domain.model.PostLike;
import com.wanted.momocity.community.domain.repository.PostLikeRepository;
import com.wanted.momocity.community.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * comment.
 *  좋아요 쓰기 작업 UseCase 구현체
 *  -> 좋아요, 좋아요 취소
 */

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LikeCommandService implements LikeCommandUseCase {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserInfoPort userInfoPort;
    private final ApplicationEventPublisher eventPublisher;

    // 좋아요
    @Override
    public LikeResult likePost(Long userId, Long postId) {

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 삭제된 게시글 좋아요 방지
        if (post.isDeleted()) {
            throw new CommunityAccessDeniedException("삭제된 게시글에는 좋아요를 누를 수 없습니다.");
        }

        // 중복 좋아요 방지
        postLikeRepository.findByPostIdAndUserId(postId, userId)
                .ifPresent(like -> {
                    throw new CommunityAccessDeniedException("이미 좋아요를 눌렀습니다.");
                });

        // 좋아요 저장 + likeCount 증가
        postLikeRepository.save(PostLike.create(postId, userId));
        post.increaseLikeCount();
        postRepository.save(post);

        // 본인 게시글 좋아요 시 알림 제외
        if (!post.getUserId().equals(userId)) {
            String likerName = userInfoPort.findById(userId)
                    .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."))
                    .getNickname();
            eventPublisher.publishEvent(
                    new PostLikedEvent(postId, post.getUserId(), userId, likerName));
        }

        log.info("[Community] 좋아요 완료 | userId={}, postId={}", userId, postId);
        return new LikeResult(postId, post.getLikeCount(), true);
    }

    // 좋아요 취소
    @Override
    public LikeResult unlikePost(Long userId, Long postId) {

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 좋아요 여부 확인
        PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new CommunityNotFoundException("좋아요를 누르지 않았습니다."));

        // 좋아요 삭제 + likeCount 감소
        postLikeRepository.delete(postLike);
        post.decreaseLikeCount();
        postRepository.save(post);

        log.info("[Community] 좋아요 취소 완료 | userId={}, postId={}", userId, postId);
        return new LikeResult(postId, post.getLikeCount(), false);
    }

}
