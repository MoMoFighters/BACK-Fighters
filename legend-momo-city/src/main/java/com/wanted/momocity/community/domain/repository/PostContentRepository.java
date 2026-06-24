package com.wanted.momocity.community.domain.repository;

import com.wanted.momocity.community.domain.model.PostContent;

import java.time.LocalDateTime;
import java.util.List;

/*
* comment.
*  PostContent 도메인 저장소 인터페이스
*  - infrastructure 를 모르고 도메인 계층에서만 사용
*  - 구현체 : PostContentRepositoryAdapter
* */

public interface PostContentRepository {

    // 콘텐츠 저장
    PostContent save(PostContent postContent);

    // 콘텐츠 목록 저장
    List<PostContent> saveAll(List<PostContent> postContents);

    // 게시글 콘텐츠 전체 조회 (orderNo 기준 정렬)
    List<PostContent> findAllByPostId(Long postId);

    // 게시글 콘텐츠 전체 소프트딜리트
    void deleteAllByPostId(Long postId);

}
