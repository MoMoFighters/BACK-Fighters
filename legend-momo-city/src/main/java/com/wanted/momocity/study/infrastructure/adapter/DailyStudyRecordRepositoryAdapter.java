package com.wanted.momocity.study.infrastructure.adapter;

import com.wanted.momocity.study.domain.model.DailyStudyRecord;
import com.wanted.momocity.study.domain.repository.DailyStudyRecordRepository;
import com.wanted.momocity.study.infrastructure.persistence.DailyStudyRecordJpaEntity;
import com.wanted.momocity.study.infrastructure.persistence.DailyStudyRecordJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  DailyStudyRecordRepository 인터페이스 구현체
 *  -> domain.repository 인터페이스 <- 구현 -> JpaRepository 연결
 * */

@Component
@RequiredArgsConstructor
public class DailyStudyRecordRepositoryAdapter implements DailyStudyRecordRepository {

    private final DailyStudyRecordJpaRepository dailyStudyRecordJpaRepository;

    // record 저장 (생성, 수정)
    @Override
    public DailyStudyRecord save(DailyStudyRecord record) {
        return dailyStudyRecordJpaRepository.save(DailyStudyRecordJpaEntity.from(record)).toDomain();
    }

    // 특정 유저 + 특정 날짜 record 단건 조회
    @Override
    public Optional<DailyStudyRecord> findByUserIdAndStudyDate(Long userId, LocalDate studyDate) {
        return dailyStudyRecordJpaRepository.findByUserIdAndStudyDate(userId, studyDate)
                .map(DailyStudyRecordJpaEntity::toDomain);
    }

    // 특정 유저의 1년치 잔디 데이터 조회 - year를 날짜 범위로 변환해서 조회
    @Override
    public List<DailyStudyRecord> findAllByUserIdAndYear(Long userId, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return dailyStudyRecordJpaRepository.findAllByUserIdAndDateRange(userId, startDate, endDate)
                .stream()
                .map(DailyStudyRecordJpaEntity::toDomain)
                .toList();
    }

    // 여러 유저의 특정 날짜 record를 한 번에 조회 (방 일별 랭킹용, N+1 방지)
    @Override
    public List<DailyStudyRecord> findAllByUserIdsAndStudyDate(List<Long> userIds, LocalDate studyDate) {
        return dailyStudyRecordJpaRepository.findAllByUserIdInAndStudyDate(userIds, studyDate)
                .stream()
                .map(DailyStudyRecordJpaEntity::toDomain)
                .toList();
    }

}
