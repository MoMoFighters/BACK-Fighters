package com.wanted.momocity.study.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.study.domain.model.DailyStudyRecord;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/*
 * comment.
 *  DB 테이블(daily_study_record)과 1:1 매핑되는 JPA 클래스
 *  -> Domain Model (DailyStudyRecord) 을 모르고 DB 컬럼 구조만 표현
 *  -> 변환은 DailyStudyRecordRepositoryAdapter 가 담당
 * */

@Getter
@Entity
@Table(name = "daily_study_record")
@NoArgsConstructor
public class DailyStudyRecordJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;

    @Column(name = "total_seconds", nullable = false)
    private int totalSeconds;

    // Domain -> JpaEntity 변환 (저장용)
    public static DailyStudyRecordJpaEntity from(DailyStudyRecord domain) {
        DailyStudyRecordJpaEntity entity = new DailyStudyRecordJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.studyDate = domain.getStudyDate();
        entity.totalSeconds = domain.getTotalSeconds();
        return entity;
    }

    // JpaEntity -> Domain 변환 (조회용)
    public DailyStudyRecord toDomain() {
        return DailyStudyRecord.reconstitute(
                id, userId, studyDate, totalSeconds, getCreatedAt(), getUpdatedAt()
        );
    }

}
