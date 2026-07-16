package com.wanted.momocity.study.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  Spring Data JPA 가 구현체를 자동으로 생성
 *  -> Domain 을 모르고 JpaEntity 만 다룸
 * */

public interface MonthlyStudyRecordJpaRepository extends JpaRepository<MonthlyStudyRecordJpaEntity, Long> {

    // 특정 유저 + 특정 년월 record 단건 조회 - 이벤트 리스너가 누적할 대상을 찾을 때 사용
    Optional<MonthlyStudyRecordJpaEntity> findByUserIdAndYearMonth(Long userId, String yearMonth);

    // 여러 유저의 특정 년월 record를 한 번에 조회 (방 월별 랭킹 조회 시 N+1 방지)
    List<MonthlyStudyRecordJpaEntity> findAllByUserIdInAndYearMonth(List<Long> userIds, String yearMonth);

}
