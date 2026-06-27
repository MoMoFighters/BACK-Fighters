package com.wanted.momocity.review.infrastructure.adapter;

import com.wanted.momocity.review.domain.model.Review;
import com.wanted.momocity.review.domain.repository.ReviewRepository;
import com.wanted.momocity.review.infrastructure.persistence.ReviewJpaEntity;
import com.wanted.momocity.review.infrastructure.persistence.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository // 이 클래스가 DB 저장소 역할을 하는 Spring Bean이라고 알려주는 어노테이션
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepository {

    // DB 접근 JPA Repository
    private final ReviewJpaRepository reviewJpaRepository;

    // 수강평 저장
    @Override
    public Review save(Review review) {
        ReviewJpaEntity entity = ReviewJpaEntity.from(review);

        ReviewJpaEntity savedEntity = reviewJpaRepository.saveAndFlush(entity);

        return savedEntity.toDomain();
    }

    // 수강평 작성 확인
    @Override
    public boolean existsByUserIdAndLectureId(
            Long userId,
            Long lectureId
    ) {
        // 중복 수강평 여부 확인
        return reviewJpaRepository.existsByUserIdAndLectureId(
                userId,
                lectureId
        );
    }

    // 리뷰 존재 여부 확인
    @Override
    public boolean existsById(Long reviewId) {
        return reviewJpaRepository.existsById(reviewId);
    }

    // 리뷰 삭제 (관리자)
    @Override
    public void deleteById(Long reviewId) {
        reviewJpaRepository.deleteById(reviewId);
    }
}
