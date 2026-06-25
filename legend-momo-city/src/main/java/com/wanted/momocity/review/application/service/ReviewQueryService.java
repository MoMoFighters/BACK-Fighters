package com.wanted.momocity.review.application.service;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.domain.exception.UserNotFoundException;
import com.wanted.momocity.lecture.domain.exception.LectureNotFoundException;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import com.wanted.momocity.review.application.query.ReviewQuery;
import com.wanted.momocity.review.application.usecase.ReviewQueryUseCase;
import com.wanted.momocity.review.infrastructure.persistence.ReviewJpaEntity;
import com.wanted.momocity.review.infrastructure.persistence.ReviewJpaRepository;
import com.wanted.momocity.review.presentation.api.response.ReviewListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.lang.model.element.NestingKind;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryService implements ReviewQueryUseCase {

    // 수강평 DB 조회 담당
    private final ReviewJpaRepository reviewJpaRepository;
    // 강의 존재 여부 확인
    private final LectureRepository lectureRepository;
    // 리뷰 작성자 조회
    private final LoadUserPort loadUserPort;

    @Override
    public ReviewListResponse getReviews(ReviewQuery.GetReviewListQuery query) {
        // 요청한 lectureId에 해당하는 강의가 있는지 확인
        lectureRepository.findById(query.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));


        Pageable pageable = PageRequest.of(query.page(), query.size());

        // 최신순으로 강의평 조회
        Page<ReviewJpaEntity> reviewPage = reviewJpaRepository.findAllByLectureIdOrderByCreatedAtDesc(
                query.lectureId(),
                pageable
        );

        // 수강평 목록 조회 응답
        return new ReviewListResponse(reviewPage.getContent().stream() // 현재 페이지의 수강평 목록을 스트림으로 변환
                .map(this::toReviewItemResponse) // ReviewJpaEntity를 ReviewItemResponse로 변환
                .toList(), // 변환된 수강평 응답 목록을 List로 수집
                query.page(), // 현재 페이지 번호
                query.size(), // 한 페이지 크기
                reviewPage.getTotalElements(), // 전체 수강평 개수
                reviewPage.getTotalPages() // 전체 페이지 수
        );
    }

    // 수강 Entity
    private ReviewListResponse.ReviewItemResponse toReviewItemResponse(ReviewJpaEntity review) {
        // 리뷰 작성자 Id로 사용자 정보 조회
        var user = loadUserPort.findById(review.getUserId()) // 리뷰 작성자 ID로 사용자 정보 조회
                .orElseThrow(() -> new UserNotFoundException("수강평 작성자를 찾을 수 없습니다.")); // 작성자 정보가 없으면 예외 발생

        // 수강평 1개 응답
        return  new ReviewListResponse.ReviewItemResponse(review.getUserId(), // 수강평 작성자 ID
                user.getNickname(), // 수강평 작성자 닉네임
                review.getId(), // 수강평 ID
                review.getCreatedAt(), // 수강평 작성 시간
                review.getContent(), // 수강평 작성 내용
                review.getRating(), // 수강평 별점
                user.getProfileImageUrl() // 수강평 작성자 프로필 이미
        );
    }
}
