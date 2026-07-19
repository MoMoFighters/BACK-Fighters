package com.wanted.momocity.study.domain.repository;

import com.wanted.momocity.study.domain.model.DailyStudyRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  DailyStudyRecord 도메인 저장소 인터페이스
 *  - infrastructure 를 모르고 도메인 계층에서만 사용
 *  - 구현체 : DailyStudyRecordRepositoryAdapter
 * */

public interface DailyStudyRecordRepository {

    // record 저장 (생성, 수정) - 이벤트 리스너가 accumulate() 호출 후 저장
    DailyStudyRecord save(DailyStudyRecord record);

    // 특정 유저 + 특정 날짜 record 단건 조회
    // 없으면 Optional.empty() -> 이벤트 리스너가 create()로 신규 생성
    Optional<DailyStudyRecord> findByUserIdAndStudyDate(Long userId, LocalDate studyDate);

    // 특정 유저의 1년치 잔디 데이터 조회 ("GET /records/yearly" 용, 마이페이지 진입 시)
    List<DailyStudyRecord> findAllByUserIdAndYear(Long userId, int year);

    // 여러 유저의 특정 날짜 record를 한 번에 조회 (방 일별 랭킹 조회 시 N+1 방지)
    List<DailyStudyRecord> findAllByUserIdsAndStudyDate(List<Long> userIds, LocalDate studyDate);

    // 특정 유저 + 날짜 누적시간 증가 (있으면 UPDATE, 없으면 INSERT)
    void incrementSeconds(Long userId, LocalDate studyDate, int seconds);

}