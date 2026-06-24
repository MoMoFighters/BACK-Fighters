package com.wanted.momocity.review.domain.model;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import lombok.Getter;

import java.time.LocalDateTime;

// Review는 수강평 도메인 모델입니다.
@Getter
public class Review {

    // 수강평 ID
    private final Long id;

    // 수강평을 작성한 사용자 ID.
    private final Long userId;

    // 수강평이 작성된 강의 ID
    private final Long lectureId;

    // 사용자가 등록한 별점
    private final int rating;

    // 사용자가 작성한 수강평 내용
    private final String content;

    // 수강평 생성 날짜
    private final LocalDateTime createdAt;

    // 외부에서 검증 없이 객체를 만들 수 없도록 생성자를 private으로 막습니다.
    private Review(
            Long id,
            Long userId,
            Long lectureId,
            int rating,
            String content,
            LocalDateTime createdAt
    ) {
        // 사용자 ID는 필수값이므로 null이면 예외를 발생시킵니다.
        if (userId == null) {
            throw new DomainRuleViolationException("사용자 ID는 필수입니다.");
        }

        // 강의 ID는 필수값이므로 null이면 예외를 발생시킵니다.
        if (lectureId == null) {
            throw new DomainRuleViolationException("강의 ID는 필수입니다.");
        }

        // 별점은 1점 이상 5점 이하만 허용합니다.
        if (rating < 1 || rating > 5) {
            throw new DomainRuleViolationException("별점은 1점 이상 5점 이하만 가능합니다.");
        }

        // 수강평 내용은 필수값이므로 null 또는 공백이면 예외를 발생시킵니다.
        if (content == null || content.isBlank()) {
            throw new DomainRuleViolationException("수강평 내용은 필수입니다.");
        }

        this.id = id;
        this.userId = userId;
        this.lectureId = lectureId;
        this.rating = rating;
        this.content = content.trim();
        this.createdAt = createdAt;
    }

    // 새 수강평을 생성할 때 사용하는 메서드입니다.
    public static Review create(
            Long userId,
            Long lectureId,
            int rating,
            String content
    ) {
        // 새 수강평은 아직 DB에 저장되지 않았으므로 id, createdAt, updatedAt은 null로 둡니다.
        return new Review(
                null,
                userId,
                lectureId,
                rating,
                content,
                null
        );
    }

    // DB에서 조회한 수강평 데이터를 도메인 객체로 복원할 때 사용하는 메서드입니다.
    public static Review reconstitute(
            Long id,
            Long userId,
            Long lectureId,
            int rating,
            String content,
            LocalDateTime createdAt
    ) {
        // DB에 저장되어 있던 값을 도메인 객체로 다시 구성합니다.
        return new Review(
                id,
                userId,
                lectureId,
                rating,
                content,
                createdAt
        );
    }
}