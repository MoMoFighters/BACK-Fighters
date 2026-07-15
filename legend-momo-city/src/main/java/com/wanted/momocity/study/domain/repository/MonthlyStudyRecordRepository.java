package com.wanted.momocity.study.domain.repository;

import com.wanted.momocity.study.domain.model.MonthlyStudyRecord;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  MonthlyStudyRecord 도메인 저장소 인터페이스
 *  - infrastructure 를 모르고 도메인 계층에서만 사용
 *  - 구현체 : MonthlyStudyRecordRepositoryAdapter
 * */

public interface MonthlyStudyRecordRepository {

    // record 저장 (생성, 수정) - 이벤트 리스너가 accumulate() 호출 후 저장
    MonthlyStudyRecord save(MonthlyStudyRecord record);

    // 특정 유저 + 특정 년월 record 단건 조회
    // 없으면 Optional.empty() -> 이벤트 리스너가 create()로 신규 생성
    Optional<MonthlyStudyRecord> findByUserIdAndYearMonth(Long userId, YearMonth yearMonth);

    // 여러 유저의 특정 년월 record를 한 번에 조회 (방 월별 랭킹 조회 시 N+1 방지)
    List<MonthlyStudyRecord> findAllByUserIdsAndYearMonth(List<Long> userIds, YearMonth yearMonth);

}