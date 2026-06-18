package com.wanted.momocity.viewing.infrastructure.adapter;

import com.wanted.momocity.viewing.domain.model.LearningHistory;
import com.wanted.momocity.viewing.domain.repository.LearningHistoryRepository;
import com.wanted.momocity.viewing.infrastructure.persistence.LearningHistoryJpaEntity;
import com.wanted.momocity.viewing.infrastructure.persistence.LearningHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/*
* comment.
*  domain.repository 인터페이스 <- 구현 -> JpaRepository 연결
*  Domain 은 이 클래스를 모르고, LearningHistoryRepository 인터페이스만 앎
*  -
*  저장 : Domain -> JpaEntity (from()) -> DB 저장
*  조회 : DB 조회 -> JpaEntity -> Domain (toDomain())
* */

@Component
@RequiredArgsConstructor
// implements : domain.repository 인터페이스 구현
public class LearningHistoryRepositoryAdapter implements LearningHistoryRepository {

    private final LearningHistoryJpaRepository jpaRepository;

    @Override
    public LearningHistory save(LearningHistory learningHistory) {
        // from() : 저장 전 Domain -> JpaEntity 변환
        LearningHistoryJpaEntity entity = LearningHistoryJpaEntity.from(learningHistory);
        // toDomain() : 조회 후 JpaEntity -> Domain 변환
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<LearningHistory> findByUserIdAndChapterId(Long userId, Long chapterId) {
        return jpaRepository
                .findByUserIdAndChapterId(userId,chapterId)
                // .map() : Optional/stream 안에서 변환할 때 사용
                // :: : 메서드 참조 (람다 축약형)
                .map(LearningHistoryJpaEntity::toDomain);
    }

    @Override
    public List<LearningHistory> findByUserIdAndLectureId(Long userId, Long lectureId) {
        return jpaRepository
                .findByUserIdAndLectureId(userId, lectureId)
                .stream()
                .map(LearningHistoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<LearningHistory> findLatestByUserIdAndLectureId(Long userId, Long lectureId) {
        return jpaRepository
                .findTopByUserIdAndLectureIdOrderByUpdatedAtDesc(userId, lectureId)
                .map(LearningHistoryJpaEntity::toDomain);
    }

    @Override
    public List<LearningHistory> findByUserIdAndDate(Long userId, LocalDate date) {

        // last_watched_at 이 해당 날짜인 시청 기록 조회
        // -> 날짜의 시작(00:00:00) - 끝(23:59:59) 범위로 조회
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return jpaRepository
                .findByUserIdAndLastWatchedAtBetween(userId, start, end)
                .stream()
                .map(LearningHistoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<LearningHistory> findLatestByUserId(Long userId) {
        return jpaRepository
                .findTopByUserIdOrderByUpdatedAtDesc(userId)
                .map(LearningHistoryJpaEntity::toDomain);
    }

    @Override
    public Optional<LearningHistory> findLatestByUserIdAndLectureIds(Long userId, List<Long> lectureIds) {
        return jpaRepository
                .findTopByUserIdAndLectureIdInOrderByUpdatedAtDesc(userId, lectureIds)
                .map(LearningHistoryJpaEntity::toDomain);
    }
}
