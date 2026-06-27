package com.wanted.momocity.review.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.review.domain.model.Review;
import com.wanted.momocity.review.domain.model.ReviewStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
// JPA가 엔티티 저장할 때 시간 값을 자동으로 넣어주게 하는 설정
@EntityListeners(AuditingEntityListener.class)
// review 테이블에 “한 유저가 같은 강의에 리뷰를 한 번만 쓸 수 있다”는 DB 규칙을 거는 설정
@Table(
        name = "review",
        uniqueConstraints = { // unique 제약조건을 추가
                @UniqueConstraint( // 실제 unique 제약조건 하나를 정의
                        name = "uk_review_user_lecture", // DB에 만들어질 unique 제약조건 이름
                        columnNames = {"user_id", "lecture_id"}
                )
        }
)
@NoArgsConstructor
public class ReviewJpaEntity{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreatedDate // 엔티티 생성 시 createdAt 자동입력
    @Column(name = "created_at", nullable = false, updatable = false) // create 컬럼만 매핑
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status = ReviewStatus.ACTIVE;

    // 도메인 review를 jpa entity로 변환
    public static ReviewJpaEntity from(Review review) {
        // 저장할 JPA Entity 객체 생성
        ReviewJpaEntity entity = new ReviewJpaEntity();

        entity.id = review.getId();
        entity.userId = review.getUserId();
        entity.lectureId = review.getLectureId();
        entity.rating = review.getRating();
        entity.content = review.getContent();
        entity.status = ReviewStatus.ACTIVE;
        return entity;
    }

    public Review toDomain() {
        return Review.reconstitute(
                id,
                userId,
                lectureId,
                rating,
                content,
                createdAt
        );
    }

    public void softDelete() {
        this.status = ReviewStatus.DELETED;
    }
}
