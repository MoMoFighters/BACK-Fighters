package com.wanted.momocity.community.application.usecase;

import com.wanted.momocity.community.application.command.PostContentCommand;
import com.wanted.momocity.community.application.result.CommentCreateResult;
import com.wanted.momocity.community.application.result.LikeResult;
import com.wanted.momocity.community.application.result.PostCreateResult;
import com.wanted.momocity.community.application.result.ReplyCreateResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/*
 * comment.
 *  게시글 쓰기 작업 전용 UseCase
 *  - 게시글 생성, 수정, 삭제, 컨텐츠 업로드 / 수정
 *  - 좋아요, 댓글, 대댓글 포함
 * */

public interface PostCommandUseCase {

    // 게시글 생성
    PostCreateResult createPost(Long userId, String title, String category);

    // 게시글 이미지 업로드 (POST)
    String uploadImage(MultipartFile image);

    // 게시글 콘텐츠 업로드 (POST)
    void uploadContents(Long userId, Long postId, List<PostContentCommand> contents);

    // 게시글 제목/카테고리 수정
    void updatePost(Long userId, Long postId, String title, String category);

    // 게시글 콘텐츠 수정 (PUT)
    void updateContents(Long userId, Long postId, List<PostContentCommand> contents);

    // 게시글 삭제
    void deletePost(Long userId, Long postId);

    // 좋아요
    LikeResult likePost(Long userId, Long postId);

    // 좋아요 취소
    LikeResult unlikePost(Long userId, Long postId);

    // 댓글 작성
    CommentCreateResult createComment(Long userId, Long postId, String content);

    // 댓글 삭제
    void deleteComment(Long userId, Long postId, Long commentId);

    // 대댓글 작성
    ReplyCreateResult createReply(Long userId, Long postId, Long commentId, String content);

    // 대댓글 삭제
    void deleteReply(Long userId, Long postId, Long commentId, Long replyId);

}
