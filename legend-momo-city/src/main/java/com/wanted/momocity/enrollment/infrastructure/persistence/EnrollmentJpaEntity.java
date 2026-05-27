package com.wanted.momocity.enrollment.infrastructure.persistence;

import com.wanted.momocity.enrollment.domain.model.Enrollment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * EnrollmentJpaEntity는 enrollment 테이블과 매핑되는 JPA Entity입니다.
 * 이 클래스는 DB 저장용 클래스입니다.
 */
@Getter
@Entity
@Table(name = "enrollment")
@NoArgsConstructor
public class EnrollmentJpaEntity {

    // enrollment 테이블의 기본키

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 수강신청한 사용자 ID입니다.
    // DB 컬럼명은 user_id입니다.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 수강신청한 강의 ID입니다.
    // DB 컬럼명은 lecture_id입니다.
    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    // 전체 진도율입니다.
    // 최초 수강신청 시 0으로 저장됩니다.
    @Column(name = "total_progress", nullable = false)
    private int totalProgress;

    // 완료한 챕터 수입니다.
    // 최초 수강신청 시 0으로 저장됩니다.
    @Column(name = "completed_count", nullable = false)
    private int completedCount;

    // 수강신청한 시간입니다.
    // DB 컬럼명은 enrolled_at입니다.
    @Column(name = "enrolled_at", nullable = false)
    private java.time.LocalDateTime enrolledAt;

    /*
     * 도메인 모델 Enrollment를 JPA Entity로 변환합니다.
     * 저장할 때 사용합니다.
     * EnrollmentRepositoryAdapter에서 호출하게 됩니다.
     */
    public static EnrollmentJpaEntity from(Enrollment enrollment) {
        EnrollmentJpaEntity entity = new EnrollmentJpaEntity();

        entity.id = enrollment.getId();
        entity.userId = enrollment.getUserId();
        entity.lectureId = enrollment.getLectureId();
        entity.totalProgress = enrollment.getTotalProgress();
        entity.completedCount = enrollment.getCompletedCount();
        entity.enrolledAt = enrollment.getEnrolledAt();

        return entity;
    }

    /**
     * JPA Entity를 도메인 모델 Enrollment로 변환합니다.
     *
     * DB에서 조회한 데이터를 비즈니스 계층으로 넘길 때 사용합니다.
     */
    public Enrollment toDomain() {
        return Enrollment.reconstitute(
                id,
                userId,
                lectureId,
                totalProgress,
                completedCount,
                enrolledAt
        );
    }
}