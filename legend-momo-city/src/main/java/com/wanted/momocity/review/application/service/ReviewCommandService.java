package com.wanted.momocity.review.application.service;

import com.wanted.momocity.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.momocity.lecture.domain.exception.LectureNotFoundException;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import com.wanted.momocity.review.application.command.ReviewCommand;
import com.wanted.momocity.review.application.usecase.ReviewCommandUseCase;
import com.wanted.momocity.review.domain.exception.DuplicateReviewException;
import com.wanted.momocity.review.domain.exception.ReviewAccessDeniedException;
import com.wanted.momocity.review.domain.exception.ReviewNotFoundException;
import com.wanted.momocity.review.domain.model.Review;
import com.wanted.momocity.review.domain.model.ReviewStatus;
import com.wanted.momocity.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 수강평 등록 서비스 로직
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReviewCommandService implements ReviewCommandUseCase {

    private final ReviewRepository reviewRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;

    // 등록 API는 응답 데이터가 필요 없으므로 반환하지 않습니다.
    @Override
    public void createReview(
            ReviewCommand.CreateReviewCommand command
    ) {
        // 수강평 등록 처리 시간 기록
        long startTime = System.currentTimeMillis();
        // 수강평 등록 시작 로그
        log.info("수강평 등록 시작 - userId={}, lectureId{}",
                command.userId(),
                command.lectureId()
        );

        // 강의 존재 여부 확인
        lectureRepository.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        // 로그인 한 사용자가 해당 강의 신청 확인
        boolean enrolled = enrollmentRepository.existsByUserIdAndLectureId(
                command.userId(),
                command.lectureId()
        );

        // 신청한 강의가 아니면 작성 못한다
        if (!enrolled){
            throw new ReviewAccessDeniedException("신청하지 않은 강의입니다.");
        }

        // 로그인 한 사용자가 강의평을 이미 작성했는지 확인하기
        boolean alreadyReviewed = reviewRepository.existsByUserIdAndLectureIdAndStatus(
                command.userId(),
                command.lectureId(),
                ReviewStatus.ACTIVE
        );
        // 이미 수강평 작성했을 경우 중복 방지
        if (alreadyReviewed) {
            throw new DuplicateReviewException("이미 수강평을 작성한 강의입니다.");
        }

        // 검증 후 수강평 도메인 객체 생성
        Review review = Review.create(
                command.userId(),
                command.lectureId(),
                command.rating(),
                command.content()
        );

        // 수강평 DB 저장
        Review savedReview;

        try {
            // DB에 수강평을 저장합니다.
            savedReview = reviewRepository.save(review);
        } catch (DataIntegrityViolationException exception) {
            // 동시에 중복 요청이 들어와 DB unique 제약조건이 발생한 경우 409 예외로 변환합니다.
            throw new DuplicateReviewException("이미 수강평을 작성한 강의입니다.");
        }

        // 수강평 등록 처리 걸린 시간
        long elapsedTime = System.currentTimeMillis() - startTime;

        // 수강평 완료 로그
        log.info("수강평 등록 완료 : reviewId={}, userId={}, lectureID={}, elapsedTime={}",
                savedReview.getId(),
                savedReview.getUserId(),
                savedReview.getLectureId(),
                elapsedTime
        );
    }

    // ReviewCommandUseCase 인터페이스의 메서드를 구현한다는 표시
    @Override
    public void deleteReview(Long reviewId) {

        long startTime = System.currentTimeMillis();
        log.info("수강평 삭제 시작 - reviewId={}", reviewId);

        // 삭제할 리뷰가 DB에 존재하지 않는지 확인
        if (!reviewRepository.existsById(reviewId)) {
            log.warn("수강평 삭제 실패 - 수강평 없음, reviewId={}", reviewId);
            throw new ReviewNotFoundException("수강평을 찾을 수 없습니다.");
        }

        reviewRepository.softDeleteById(reviewId);

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("수강평 삭제 완료 - reviewId={}, elapsedTime={}ms", reviewId, elapsedTime);
        // 리뷰 ID 기준으로 수강평 삭제 실행

    }
}
