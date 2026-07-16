package com.wanted.momocity.community.application.comment.service;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.community.application.comment.usecase.CommentCommandUseCase;
import com.wanted.momocity.community.application.post.port.UserInfoPort;
import com.wanted.momocity.community.domain.event.CommentCreatedEvent;
import com.wanted.momocity.community.domain.event.ReplyCreatedEvent;
import com.wanted.momocity.community.domain.exception.CommunityAccessDeniedException;
import com.wanted.momocity.community.domain.exception.CommunityNotFoundException;
import com.wanted.momocity.community.domain.model.Comment;
import com.wanted.momocity.community.domain.model.Post;
import com.wanted.momocity.community.domain.repository.CommentRepository;
import com.wanted.momocity.community.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * comment.
 *  댓글 / 대댓글 쓰기 작업 UseCase 구현체
 *  -> 댓글 작성/삭제, 대댓글 작성/삭제
 */

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentCommandService implements CommentCommandUseCase {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserInfoPort userInfoPort;
    private final ApplicationEventPublisher eventPublisher;

    // 댓글 작성
    @Override
    public void createComment(Long userId, Long postId, String content) {

        // 게시글 조회 (존재 여부 + 삭제 여부 확인)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 삭제된 게시글 댓글 방지
        if (post.isDeleted()) {
            throw new CommunityAccessDeniedException("삭제된 게시글에는 댓글을 작성할 수 없습니다.");
        }

        // 댓글 생성 및 저장
        Comment comment = Comment.create(postId, userId, content);
        Comment saved = commentRepository.save(comment);

        // 댓글 작성자 정보 조회 (알림용)
        User user = userInfoPort.findById(userId)
                .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));

        // 본인 게시글 댓글 시 알림 제외
        if (!post.getUserId().equals(userId)) {
            eventPublisher.publishEvent(
                    new CommentCreatedEvent(postId, post.getUserId(), userId, user.getNickname()));
        }

        log.info("[Community] 댓글 작성 완료 | userId={}, postId={}, commentId={}",
                userId, postId, saved.getId());
    }

    // 댓글 삭제 (소프트딜리트)
    @Override
    public void deleteComment(Long userId, Long postId, Long commentId) {

        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommunityNotFoundException("댓글을 찾을 수 없습니다."));

        // 해당 게시글 소속 댓글인지 검증
        if (!comment.getPostId().equals(postId)) {
            throw new CommunityAccessDeniedException("해당 게시글의 댓글이 아닙니다.");
        }

        // 본인 댓글인지 검증
        validateAuthor(comment.getUserId(), userId);
        comment.delete();
        commentRepository.delete(comment);

        log.info("[Community] 댓글 삭제 완료 | commentId={}", commentId);
    }

    // 대댓글 작성
    @Override
    public void createReply(Long userId, Long postId, Long commentId, String content) {

        // 부모 댓글 조회
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommunityNotFoundException("댓글을 찾을 수 없습니다."));

        // 부모 댓글이 해당 게시글 소속인지 검증
        if (!parentComment.getPostId().equals(postId)) {
            throw new CommunityAccessDeniedException("해당 게시글의 댓글이 아닙니다.");
        }

        // 대댓글에 대댓글 방지
        if (parentComment.isReply()) {
            throw new CommunityAccessDeniedException("대댓글에는 답글을 달 수 없습니다.");
        }

        // 삭제된 댓글 대댓글 방지
        if (parentComment.isDeleted()) {
            throw new CommunityAccessDeniedException("삭제된 댓글에는 대댓글을 작성할 수 없습니다.");
        }

        // 게시글 조회 (삭제 여부 + postOwnerId 알림용) → 저장 전에 수행
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 삭제된 게시글 대댓글 방지
        if (post.isDeleted()) {
            throw new CommunityAccessDeniedException("삭제된 게시글에는 대댓글을 작성할 수 없습니다.");
        }

        // 대댓글 생성 및 저장
        Comment reply = Comment.createReply(postId, userId, commentId, content);
        Comment saved = commentRepository.save(reply);

        // 대댓글 작성자 정보 조회 (알림용)
        User user = userInfoPort.findById(userId)
                .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));

        // 본인 게시글 또는 본인 댓글에 대댓글 시 알림 제외
        if (!userId.equals(post.getUserId()) && !userId.equals(parentComment.getUserId())) {
            eventPublisher.publishEvent(new ReplyCreatedEvent(
                    postId,
                    post.getUserId(),
                    parentComment.getUserId(),
                    userId,
                    user.getNickname()
            ));
        }

        log.info("[Community] 대댓글 작성 완료 | userId={}, commentId={}, replyId={}",
                userId, commentId, saved.getId());
    }

    // 대댓글 삭제 (소프트딜리트)
    @Override
    public void deleteReply(Long userId, Long postId, Long commentId, Long replyId) {

        // 대댓글 조회
        Comment reply = commentRepository.findById(replyId)
                .orElseThrow(() -> new CommunityNotFoundException("대댓글을 찾을 수 없습니다."));

        // 해당 게시글 소속 대댓글인지 검증
        if (!reply.getPostId().equals(postId)) {
            throw new CommunityAccessDeniedException("해당 게시글의 대댓글이 아닙니다.");
        }

        // 본인 대댓글인지 검증
        validateAuthor(reply.getUserId(), userId);
        reply.delete();
        commentRepository.delete(reply);

        log.info("[Community] 대댓글 삭제 완료 | replyId={}", replyId);
    }

    // 작성자 검증 (본인만 수정/삭제 가능)
    private void validateAuthor(Long ownerId, Long userId) {
        if (!ownerId.equals(userId)) {
            throw new CommunityAccessDeniedException("본인만 수정/삭제할 수 있습니다.");
        }
    }

}
