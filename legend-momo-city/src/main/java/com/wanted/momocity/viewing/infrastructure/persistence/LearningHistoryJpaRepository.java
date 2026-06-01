package com.wanted.momocity.viewing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/*
* comment.
*  Spring Data JPA 가 구현체를 자동으로 생성해주는 JPA 전용 인터페이스
*  Domain 을 모르고 JpaEntity 만 다룸
*  실제 DB 쿼리는 여기서 실행됨
* */

public interface LearningHistoryJpaRepository extends JpaRepository<LearningHistoryJpaEntity, Long> {

    // 특정 챕터 시청 기록 조회
    // 단건 조회는 결과가 없을 수 있어 Optional 사용
    Optional<LearningHistoryJpaEntity> findByUserIdAndChapterId (
            Long userId, Long chapterId
    );

    // 특정 강의 전체 챕터 시청 기록 조회
    List<LearningHistoryJpaEntity> findByUserIdAndLectureId(
            Long userId, Long lectureId
    );

    // updated_at 기준 최신 1개
    Optional<LearningHistoryJpaEntity> findTopByUserIdAndLectureIdOrderByUpdatedAtDesc (
            Long userId, Long lectureId
    );

}
