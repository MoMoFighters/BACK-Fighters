package com.wanted.momocity.community.infrastructure.adapter;

import com.wanted.momocity.community.domain.exception.CommunityNotFoundException;
import com.wanted.momocity.community.infrastructure.persistence.CommentJpaRepository;
//import com.wanted.momocity.report.application.port.CommentContentPort;
import com.wanted.momocity.report.application.port.CommentContentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/*
 * comment.
 *  CommentContentPort 구현체
 *  -
 *  report BC 에서 선언한 인터페이스
 *  -> commentId 로 댓글 텍스트 반환
 *  -> community BC 에서 어댑터 구현
 */
@Component
@RequiredArgsConstructor
public class CommentContentAdapter implements CommentContentPort {

    private final CommentJpaRepository commentJpaRepository;

    /*
     * comment.
     *  댓글 텍스트 조회
     *  -> commentId 로 댓글 단건 조회
     *  -> 소프트딜리트 제외
     *  -> content 반환
     */
    
    @Override
    public String getContentById(Long commentId) {
        return commentJpaRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CommunityNotFoundException("댓글을 찾을 수 없습니다."))
                .getContent();
    }

}