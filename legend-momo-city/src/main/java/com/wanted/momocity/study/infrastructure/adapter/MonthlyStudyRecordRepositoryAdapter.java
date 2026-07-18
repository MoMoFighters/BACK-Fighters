package com.wanted.momocity.study.infrastructure.adapter;

import com.wanted.momocity.study.domain.model.MonthlyStudyRecord;
import com.wanted.momocity.study.domain.repository.MonthlyStudyRecordRepository;
import com.wanted.momocity.study.infrastructure.persistence.MonthlyStudyRecordJpaEntity;
import com.wanted.momocity.study.infrastructure.persistence.MonthlyStudyRecordJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  MonthlyStudyRecordRepository 인터페이스 구현체
 *  -> domain.repository 인터페이스 <- 구현 -> JpaRepository 연결
 * */

@Component
@RequiredArgsConstructor
public class MonthlyStudyRecordRepositoryAdapter implements MonthlyStudyRecordRepository {

    private final MonthlyStudyRecordJpaRepository monthlyStudyRecordJpaRepository;

    // record 저장 (생성, 수정)
    @Override
    public MonthlyStudyRecord save(MonthlyStudyRecord record) {
        return monthlyStudyRecordJpaRepository.save(MonthlyStudyRecordJpaEntity.from(record)).toDomain();
    }

    // 특정 유저 + 특정 년월 record 단건 조회
    @Override
    public Optional<MonthlyStudyRecord> findByUserIdAndYearMonth(Long userId, YearMonth yearMonth) {
        return monthlyStudyRecordJpaRepository.findByUserIdAndYearMonth(userId, yearMonth.toString())
                .map(MonthlyStudyRecordJpaEntity::toDomain);
    }

    // 여러 유저의 특정 년월 record를 한 번에 조회 (방 월별 랭킹용, N+1 방지)
    @Override
    public List<MonthlyStudyRecord> findAllByUserIdsAndYearMonth(List<Long> userIds, YearMonth yearMonth) {
        return monthlyStudyRecordJpaRepository.findAllByUserIdInAndYearMonth(userIds, yearMonth.toString())
                .stream()
                .map(MonthlyStudyRecordJpaEntity::toDomain)
                .toList();
    }

    // 원자적 증분 upsert를 그대로 JpaRepository에 위임 (YearMonth -> String 변환은 여기서 담당)
    @Override
    public void incrementSeconds(Long userId, YearMonth yearMonth, int seconds) {
        monthlyStudyRecordJpaRepository.incrementSeconds(userId, yearMonth.toString(), seconds);
    }

}
