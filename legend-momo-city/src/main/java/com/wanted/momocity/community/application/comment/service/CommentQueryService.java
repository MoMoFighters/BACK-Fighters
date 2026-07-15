package com.wanted.momocity.community.application.comment.service;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.community.application.comment.usecase.CommentQueryUseCase;
import com.wanted.momocity.community.application.post.port.UserInfoPort;
import com.wanted.momocity.community.domain.exception.CommunityAccessDeniedException;
import com.wanted.momocity.community.domain.exception.CommunityNotFoundException;
import com.wanted.momocity.community.domain.model.Comment;
import com.wanted.momocity.community.domain.model.Post;
import com.wanted.momocity.community.domain.repository.CommentRepository;
import com.wanted.momocity.community.domain.repository.PostRepository;
import com.wanted.momocity.community.presentation.api.response.CommentResponse;
import com.wanted.momocity.community.presentation.api.response.PostCommentResponse;
import com.wanted.momocity.community.presentation.api.response.PostReplyResponse;
import com.wanted.momocity.community.presentation.api.response.ReplyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * comment.
 *  댓글 / 대댓글 읽기 작업 UseCase 구현체
 *  -> 댓글 목록 조회, 대댓글 목록 조회
 */

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentQueryService implements CommentQueryUseCase {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserInfoPort userInfoPort;

    // 댓글 목록 조회
    @Override
    public PostCommentResponse getComments(Long userId, Long postId, Long cursor, int size) {

        // 게시글 조회 (작성자 userId 확인용 → isPostWriter 계산에 필요)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 전체 댓글 수 조회 (대댓글 제외)
        int totalCount = commentRepository.countByPostId(postId);

        // 커서 기반 댓글 목록 조회 (size + 1 개 조회 → 다음 페이지 존재 여부 확인용)
        List<Comment> comments = commentRepository.findByPostIdWithCursor(postId, cursor, size);

        // size + 1 번째 데이터 있으면 다음 페이지 존재 → 실제 반환은 size 개만
        boolean hasNext = comments.size() > size;
        List<Comment> pagedComments = hasNext ? new ArrayList<>(comments.subList(0, size)) : comments;

        // 다음 페이지 있으면 마지막 댓글 Id 반환, 없으면 null (마지막 페이지)
        Long nextCursor = hasNext ? pagedComments.get(pagedComments.size() - 1).getId() : null;

        // 댓글 Id 목록 추출 → 대댓글 일괄 조회용
        List<Long> commentIds = pagedComments.stream()
                .map(Comment::getId)
                .toList();

        // N+1 개선: 대댓글 한 번에 일괄 조회
        // → commentId 기준 Map<commentId, List<Comment>> 로 변환
        Map<Long, List<Comment>> repliesMap = commentRepository
                .findRepliesByCommentIds(commentIds)
                .stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        // 댓글 목록 → CommentResponse 변환
        List<CommentResponse> commentResponses = pagedComments.stream()
                .map(c -> {
                    // 댓글 작성자 정보 조회
                    User commentAuthor = userInfoPort.findById(c.getUserId())
                            .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));

                    // 대댓글 목록 조회 (repliesMap 에서 가져옴 → DB 추가 조회 없음)
                    List<Comment> allReplies = repliesMap.getOrDefault(c.getId(), List.of());

                    // 대댓글 5+1 개 기준으로 hasMoreReplies 판단
                    boolean hasMoreReplies = allReplies.size() > 5;
                    List<Comment> pagedReplies = hasMoreReplies
                            ? new ArrayList<>(allReplies.subList(0, 5))
                            : allReplies;

                    // 다음 대댓글 커서 계산
                    Long nextReplyCursor = hasMoreReplies
                            ? pagedReplies.get(pagedReplies.size() - 1).getId()
                            : null;

                    // 대댓글 → ReplyResponse 변환
                    List<ReplyResponse> replyResponses = pagedReplies.stream()
                            .map(r -> {
                                // 대댓글 작성자 정보 조회
                                User replyAuthor = userInfoPort.findById(r.getUserId())
                                        .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));
                                return new ReplyResponse(
                                        r.getId(),
                                        r.getUserId(),
                                        r.getContent(),
                                        replyAuthor.getNickname(),
                                        replyAuthor.getProfileImageUrl(),
                                        replyAuthor.getRole().name(),
                                        // 대댓글 작성자가 본인인지 확인
                                        r.getUserId().equals(userId),
                                        // 대댓글 작성자가 게시글 작성자인지 확인
                                        r.getUserId().equals(post.getUserId()),
                                        r.getCreatedAt()
                                );
                            })
                            .toList();

                    return new CommentResponse(
                            c.getId(),
                            c.getUserId(),
                            c.getContent(),
                            commentAuthor.getNickname(),
                            commentAuthor.getProfileImageUrl(),
                            commentAuthor.getRole().name(),
                            // 댓글 작성자가 본인인지 확인
                            c.getUserId().equals(userId),
                            // 댓글 작성자가 게시글 작성자인지 확인
                            c.getUserId().equals(post.getUserId()),
                            c.getCreatedAt(),
                            replyResponses,
                            hasMoreReplies,
                            nextReplyCursor
                    );
                })
                .toList();

        log.info("[Community] 게시글 댓글 조회 완료 | postId={}, totalCount={}", postId, totalCount);

        return new PostCommentResponse(totalCount, commentResponses, nextCursor);
    }

    // 대댓글 목록 조회
    @Override
    public PostReplyResponse getReplies(Long userId, Long postId, Long commentId, Long cursor, int size) {

        // 게시글 작성자 확인용 (isPostWriter 계산에 필요)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));


        // 댓글 조회 (해당 게시글의 댓글인지 검증)
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommunityNotFoundException("댓글을 찾을 수 없습니다."));

        // 해당 게시글의 댓글인지 검증
        if (!comment.getPostId().equals(postId)) {
            throw new CommunityAccessDeniedException("해당 게시글의 댓글이 아닙니다.");
        }

        // 최상위 댓글인지 검증 (대댓글에 대댓글 방지)
        if (comment.getParentId() != null) {
            throw new CommunityAccessDeniedException("대댓글에는 대댓글을 작성할 수 없습니다.");
        }

        // 전체 대댓글 수 조회 (COUNT 쿼리로 DB 레벨에서 집계)
        int totalCount = commentRepository.countRepliesByCommentId(commentId);

        // 커서 기반 대댓글 조회 (size + 1 개 조회 → 다음 페이지 존재 여부 확인용)
        List<Comment> replies = commentRepository
                .findRepliesByCommentIdWithCursor(commentId, cursor, size);

        // size + 1 번째 데이터 있으면 다음 페이지 존재 → 실제 반환은 size 개만
        boolean hasNext = replies.size() > size;
        List<Comment> pagedReplies = hasNext ? new ArrayList<>(replies.subList(0, size)) : replies;

        // 다음 페이지 있으면 마지막 대댓글 Id 반환, 없으면 null (마지막 페이지)
        Long nextCursor = hasNext ? pagedReplies.get(pagedReplies.size() - 1).getId() : null;

        // 대댓글 → ReplyResponse 변환
        List<ReplyResponse> replyResponses = pagedReplies.stream()
                .map(r -> {
                    // 대댓글 작성자 정보 조회
                    User replyAuthor = userInfoPort.findById(r.getUserId())
                            .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));
                    return new ReplyResponse(
                            r.getId(),
                            r.getUserId(),
                            r.getContent(),
                            replyAuthor.getNickname(),
                            replyAuthor.getProfileImageUrl(),
                            replyAuthor.getRole().name(),
                            // 대댓글 작성자가 본인인지 확인
                            r.getUserId().equals(userId),
                            // 대댓글 작성자가 게시글 작성자인지 확인
                            r.getUserId().equals(post.getUserId()),
                            r.getCreatedAt()
                    );
                })
                .toList();

        log.info("[Community] 대댓글 조회 완료 | commentId={}, totalCount={}", commentId, totalCount);

        return new PostReplyResponse(totalCount, replyResponses, nextCursor);
    }

}
