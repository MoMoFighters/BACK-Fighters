package com.wanted.momocity.streak.infrastructure.adapter;

import com.wanted.momocity.streak.domain.model.Streak;
import com.wanted.momocity.streak.domain.repository.StreakRepository;
import com.wanted.momocity.streak.infrastructure.persistence.StreakJpaEntity;
import com.wanted.momocity.streak.infrastructure.persistence.StreakJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/*
* comment.
*  domain.repository 인터페이스 구현체
*  -> Domain 은 이 클래스를 모르고 StreakRepository 인터페이스만 앎
*  -
*  저장 : Domain -> JpaEntity (from()) -> DB 저장
*  조회 : DB 조회 -> JpaEntity -> Domain (toDomain())
* */

@Component
@RequiredArgsConstructor
public class StreakRepositoryAdapter implements StreakRepository {

    private final StreakJpaRepository jpaRepository;

    /*
     * save
     * -> 신규 생성 또는 수정
     * -> from() 으로 Domain -> JpaEntity 변환 후 저장
     * -> toDomain() 으로 JpaEntity -> Domain 변환 후 반환
     */

    @Override
    public Streak save(Streak streak) {
        return jpaRepository
                .save(StreakJpaEntity.from(streak))
                .toDomain();
    }

    @Override
    // 특정 날짜 잔디 조회 -> 오늘 잔디가 있는지 확인용
    public Optional<Streak> findByUserIdAndStreakDate(Long userId, LocalDate streakDate) {
        return jpaRepository
                .findByUserIdAndStreakDate(userId, streakDate)
                .map(StreakJpaEntity::toDomain);
    }

    @Override
    // 월간 잔디 조회 -> 메인 페이지 진입 시 한달치 잔디 조회
    public List<Streak> findUserIdAndStreakDateBetween(Long userId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository
                .findByUserIdAndStreakDateBetween(userId, startDate, endDate)
                .stream()
                .map(StreakJpaEntity::toDomain)
                .toList();
    }

    @Override
    // 연간 잔디 조회 -> 마이페이지 해당 년도 전체 잔디 조회
    public List<Streak> findByUserIdAndYear(Long userId, int year) {
        return jpaRepository
                .findByUserIdAndYear(userId, year)
                .stream()
                .map(StreakJpaEntity::toDomain)
                .toList();
    }

}
