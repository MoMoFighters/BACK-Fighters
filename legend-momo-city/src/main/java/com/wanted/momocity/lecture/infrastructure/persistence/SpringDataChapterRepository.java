package com.wanted.momocity.lecture.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

// SpringDataChapterRepository는 Spring Data JPA 전용 Repository
public interface SpringDataChapterRepository extends JpaRepository<ChapterJpaEntity, Long> {

    // 특정 강의에 등록된 챕터 개수를 조회
    int countByLectureId(Long lectureId);

    // 같은 강의 안에서 동일한 orderNo가 이미 있는지 확인
    boolean existsByLectureIdAndOrderNo(Long lectureId, int orderNo);
}