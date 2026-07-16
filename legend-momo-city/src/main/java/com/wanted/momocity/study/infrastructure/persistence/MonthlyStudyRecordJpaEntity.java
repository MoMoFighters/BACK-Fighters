package com.wanted.momocity.study.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.study.domain.model.MonthlyStudyRecord;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

/*
 * comment.
 *  DB 테이블(monthly_study_record)과 1:1 매핑되는 JPA 클래스
 *  -> Domain Model (MonthlyStudyRecord) 을 모르고 DB 컬럼 구조만 표현
 *  -> 변환은 MonthlyStudyRecordRepositoryAdapter 가 담당
 * */


@Getter
@Entity
@Table(name = "monthly_study_record")
@NoArgsConstructor
public class MonthlyStudyRecordJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "year_month", nullable = false, columnDefinition = "CHAR(7)")
    private String yearMonth;

    @Column(name = "total_seconds", nullable = false)
    private int totalSeconds;

    // Domain -> JpaEntity 변환 (저장용) - YearMonth를 "YYYY-MM" 문자열로 변환
    public static MonthlyStudyRecordJpaEntity from(MonthlyStudyRecord domain) {
        MonthlyStudyRecordJpaEntity entity = new MonthlyStudyRecordJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.yearMonth = domain.getYearMonth().toString();
        entity.totalSeconds = domain.getTotalSeconds();
        return entity;
    }

    // JpaEntity -> Domain 변환 (조회용) - "YYYY-MM" 문자열을 YearMonth로 파싱
    public MonthlyStudyRecord toDomain() {
        return MonthlyStudyRecord.reconstitute(
                id, userId, YearMonth.parse(yearMonth), totalSeconds, getCreatedAt(), getUpdatedAt()
        );
    }

}
