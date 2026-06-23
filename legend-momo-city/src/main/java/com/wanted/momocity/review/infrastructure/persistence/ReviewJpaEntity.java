package com.wanted.momocity.review.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.review.domain.model.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "review")
@NoArgsConstructor
public class ReviewJpaEntity extends BaseTimeEntity {


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

    // 도메인 review를 jpa entity로 변환
    public static ReviewJpaEntity from(Review review) {
        // 저장할 JPA Entity 객체 생성
        ReviewJpaEntity entity = new ReviewJpaEntity();

        entity.id = review.getId();
        entity.userId = review.getUserId();
        entity.lectureId = review.getLectureId();
        entity.rating = review.getRating();
        entity.content = review.getContent();
        return entity;
    }

    public Review toDomain() {
        return Review.reconstitute(
                id,
                userId,
                lectureId,
                rating,
                content,
                getCreatedAt()
        );
    }
}
