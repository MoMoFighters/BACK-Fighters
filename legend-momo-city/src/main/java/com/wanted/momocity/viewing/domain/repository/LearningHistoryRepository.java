package com.wanted.momocity.viewing.domain.repository;

import com.wanted.momocity.viewing.domain.model.LearningHistory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LearningHistoryRepository {

    // 저장 (신규 생성 + 수정)
    LearningHistory save (LearningHistory learningHistory);

    // 특정 챕터의 시청 기록 조회
    Optional<LearningHistory> findByUserIdAndChapterId(
            Long userId, Long chapterId
    );

    // 특정 강의의 전체 챕터 시청 기록 조회
    List<LearningHistory> findByUserIdAndLectureId (
            Long userId, Long lectureId
    );

    // 가장 최근 시청 기록 조회 (현재 챕터 파악용)
    Optional<LearningHistory> findLatestByUserIdAndLectureId (
            Long userId, Long lectureId
    );

    // 오늘 수강한 챕터 목록 조회
    List<LearningHistory> findByUserIdAndDate(Long userId, LocalDate date);

    // 카테고리 상관없이 가장 최근 시청 기록 조회
    Optional<LearningHistory> findLatestByUserId(Long userId);

    // 카테고리별 가장 최근 시청 기록 조회
    Optional<LearningHistory> findLatestByUserIdAndLectureIds(
            Long userId, List<Long> lectureIds
    );

}
