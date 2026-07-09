package com.wanted.momocity.review.infrastructure.adapter;

import com.wanted.momocity.report.application.port.ReviewContentPort;
import com.wanted.momocity.review.domain.exception.ReviewNotFoundException;
import com.wanted.momocity.review.domain.model.ReviewStatus;
import com.wanted.momocity.review.infrastructure.persistence.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
// report BC의 ReviewContentPort를 review BC에서 구현
public class ReviewContentAdapter implements ReviewContentPort {

    // review 테이블 조회를 위한 JPA Repository 주입
    private final ReviewJpaRepository reviewJpaRepository;

    /* comment.
        report BC 신고 상세 조회 기능에서 삭제 여부(isDeleted) 판단 근거로 사용 중.
        기존엔 findById()만 써서 삭제된 리뷰도 예외 없이 조회되던 버그가 있어
        review 담당자 협의 후 상태 필터를 추가함.
     */
    @Override
    public String getContentById(Long reviewId) {
        // ACTIVE 상태인 수강평만 reviewId로 조회 (신고 상세 조회에서 삭제 여부 판단 근거로 쓰임)
        return reviewJpaRepository.findByIdAndStatus(reviewId, ReviewStatus.ACTIVE)

                // 조회된 수강평 엔티티에서 content 값만 꺼냄
                .map(review -> review.getContent())
                // 없으면 수강평 없음 예외 발생
                .orElseThrow(() -> new ReviewNotFoundException("수강평을 찾을 수 없습니다."));
    }
}