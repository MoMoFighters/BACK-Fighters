package com.wanted.momocity.community.application.post.usecase;

import com.wanted.momocity.community.application.post.command.PostContentCommand;
import com.wanted.momocity.community.application.post.result.PostCreateResult;
import com.wanted.momocity.community.domain.model.PostCategory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/*
 * comment.
 *  게시글 쓰기 작업 전용 UseCase 인터페이스
 *  - 게시글 생성, 수정, 삭제, 컨텐츠 업로드 / 수정
 * */

public interface PostCommandUseCase {

    // 게시글 생성
    PostCreateResult createPost(Long userId, String title, PostCategory category, String thumbNailUrl);

    // 게시글 이미지 업로드 (POST)
    String uploadImage(MultipartFile image);

    // 게시글 콘텐츠 업로드 (POST)
    void uploadContents(Long userId, Long postId, String thumbnailUrl, List<PostContentCommand> contents);

    // 게시글 제목/카테고리 수정
    void updatePost(Long userId, Long postId, String title, PostCategory category);

    // 게시글 콘텐츠 수정 (PUT)
    void updateContents(Long userId, Long postId, String thumbnailUrl, List<PostContentCommand> contents);

    // 게시글 삭제
    void deletePost(Long userId, Long postId);

}
