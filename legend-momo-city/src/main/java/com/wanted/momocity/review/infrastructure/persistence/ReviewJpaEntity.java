package com.wanted.momocity.review.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.review.domain.model.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
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
